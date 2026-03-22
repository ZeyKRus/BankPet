package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.account.Account;
import com.github.zeykrus.bankpet.exception.IllegalAccountException;
import com.github.zeykrus.bankpet.exception.IllegalTransactionRequestException;
import com.github.zeykrus.bankpet.exception.InsufficientFundsException;
import com.github.zeykrus.bankpet.exception.WithdrawCASException;
import com.github.zeykrus.bankpet.interfaces.PeriodicOperation;
import com.github.zeykrus.bankpet.interfaces.ThrowingConsumer;
import com.github.zeykrus.bankpet.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Обработчик бизнес-логики банковских операций.
 * <p>
 * Класс stateless, потокобезопасен. Используется воркерами
 * {@link RequestProcessor} и {@link ExceptionProcessor}.
 * 
 *
 * <p>
 * Реализует три типа операций:
 * <ul>
 *   <li>пополнение счёта ({@link #deposit(TransactionRequest)})</li>
 *   <li>снятие средств ({@link #withdraw(TransactionRequest)})</li>
 *   <li>перевод между счетами ({@link #transfer(TransactionRequest)})</li>
 * </ul>
 * 
 *
 * <p>
 * Все операции логируются и сохраняются в историю.
 * В случае ошибки запрос отправляется в {@link ExceptionQueue}.
 * 
 */
public class ActionHandler {
    private static final Logger log = LoggerFactory.getLogger(ActionHandler.class);
    private final Map<OperationType, ThrowingConsumer<TransactionRequest>> requestHandler = new HashMap<>();
    private final HistoryManager history;
    private final ExceptionQueue exceptionQueue;

    /**
     * Создаёт обработчик операций.
     *
     * @param historyManager  менеджер для сохранения истории операций (не может быть null)
     * @param exceptionQueue  очередь для записи ошибочных запросов (не может быть null)
     * @throws NullPointerException если любой из аргументов null
     */
    public ActionHandler(HistoryManager historyManager, ExceptionQueue exceptionQueue) {
        requestHandler.put(OperationType.DEPOSIT, this::deposit);
        requestHandler.put(OperationType.WITHDRAW, this::withdraw);
        requestHandler.put(OperationType.TRANSFER, this::transfer);
        this.history = historyManager;
        this.exceptionQueue = exceptionQueue;
        log.info("Сервис инициализирован: {}", this.getClass().getSimpleName());
    }

    /**
     * Выполняет перевод средств между счетами.
     * <p>
     * Алгоритм:
     * <ol>
     *   <li>Проверяет корректность входных параметров (счета, сумма)</li>
     *   <li>Проверяет достаточность средств на счёте отправителя</li>
     *   <li>Списывает сумму со счёта отправителя ({@link Account#withdraw(long)})</li>
     *   <li>Зачисляет сумму на счёт получателя ({@link Account#deposit(long)})</li>
     * </ol>
     * 
     * <p>
     * В случае ошибки на любом этапе запись сохраняется в историю с success=false,
     * а исключение пробрасывается дальше.
     * 
     *
     * @param req запрос на перевод (содержит счета отправителя/получателя и сумму)
     * @throws InsufficientFundsException если на счёте отправителя недостаточно средств
     * @throws WithdrawCASException       если превышен лимит попыток CAS при снятии
     * @throws IllegalArgumentException   если:
     *                                   <ul>
     *                                     <li>счёт отправителя или получателя == null</li>
     *                                     <li>счета совпадают</li>
     *                                     <li>сумма перевода less 0</li>
     *                                   </ul>
     */
    public void transfer(TransactionRequest req) throws InsufficientFundsException, WithdrawCASException {
        log.debug("Обработка запроса трансфера: {}", req);
        Account accFrom = req.accFrom();
        Account accTo = req.accTo();
        long amount = req.amount();

        try {
            if (accFrom == null) throw new IllegalArgumentException("Счета списания не существует");
            if (accTo == null) throw new IllegalArgumentException("Счета пополнения не существует");
            if (accFrom == accTo) throw new IllegalArgumentException("Счета списания и счет пополнения один и тот же");
            if (amount <= 0) throw new IllegalArgumentException("Некорректная сумма перевода");
            if (!accFrom.canWithdraw(amount)) throw new InsufficientFundsException("На счете недостаточно средств для перевода",accFrom.notEnough(amount));

            accFrom.withdraw(amount);
            accTo.deposit(amount);
        } catch (Exception e) {
            log.debug("Ошибка обработки запроса трансфера: {}", req);
            history.addToHistory(req,false);
            throw e;
        }

        history.addToHistory(req,true);
    }

    /**
     * Выполняет пополнение счёта.
     * <p>
     * Проверяет корректность счёта и суммы, затем добавляет сумму на баланс.
     * 
     *
     * @param req запрос на пополнение (содержит счёт и сумму)
     * @throws IllegalArgumentException если:
     *                                   <ul>
     *                                     <li>счёт == null</li>
     *                                     <li>сумма пополнения less 0</li>
     *                                   </ul>
     */
    public void deposit(TransactionRequest req) {
        log.debug("Обработка запроса пополнения счета: {}", req);
        Account acc = req.accFrom();
        long amount = req.amount();

        try {
            if (acc == null) throw new IllegalArgumentException("Счета не существует");
            if (amount <= 0) throw new IllegalArgumentException("Некорректная сумма пополнения");

            acc.deposit(amount);
        } catch (Exception e) {
            log.debug("Ошибка обработки запроса пополнения счета: {}", req);
            history.addToHistory(req,false);
            throw e;
        }

        history.addToHistory(req,true);
    }

    /**
     * Выполняет снятие средств со счёта.
     * <p>
     * Проверяет корректность параметров и достаточность средств, затем списывает сумму.
     * 
     *
     * @param req запрос на снятие (содержит счёт и сумму)
     * @throws InsufficientFundsException если на счёте недостаточно средств
     * @throws WithdrawCASException       если превышен лимит попыток CAS при снятии
     * @throws IllegalArgumentException   если:
     *                                   <ul>
     *                                     <li>счёт == null</li>
     *                                     <li>сумма снятия less 0</li>
     *                                   </ul>
     */
    public void withdraw(TransactionRequest req) throws InsufficientFundsException, WithdrawCASException {
        log.debug("Обработка запроса снятия средств: {}", req);
        Account acc = req.accFrom();
        long amount = req.amount();

        try {
            if (acc == null) throw new IllegalArgumentException("Счета не существует");
            if (amount <= 0) throw new IllegalArgumentException("Некорректная сумма списания");

            acc.withdraw(amount);
        } catch (Exception e) {
            log.debug("Ошибка обработки запроса снятия средств: {}", req);
            history.addToHistory(req,false);
            throw e;
        }

        history.addToHistory(req,true);
    }

    /**
     * Возвращает историю операций для указанного счёта.
     *
     * @param acc счёт
     * @return список транзакций (может быть пустым)
     */
    public List<Transaction> getHistory(Account acc) {
        return history.getHistory(HistoryFilter.builder().acc(acc).build());
    }

    /**
     * Основной метод для обработки запроса из очереди.
     * <p>
     * Вызывается воркером {@link RequestProcessor}.
     * 
     *
     * @param req запрос на операцию (не может быть null)
     */
    public void handle(TransactionRequest req) {
        try {
            if (req == null) throw new IllegalTransactionRequestException("Некорректный запрос на транзакцию");
            requestHandler.get(req.operationType()).accept(req);
        } catch (InsufficientFundsException e) {
            log.warn("Бизнес-ошибка при обработке запроса: {}", e.getMessage());
            exceptionQueue.add(new ExceptionRecord(req,e));
        } catch (IllegalArgumentException | IllegalStateException |
                 IllegalAccountException e) {
            log.warn("Ошибка валидации: {}", e.getMessage());
            exceptionQueue.add(new ExceptionRecord(req,e));
        } catch (IllegalTransactionRequestException e) {
            log.warn("Ошибка запроса: {}", e.getMessage());
            exceptionQueue.add(new ExceptionRecord(req, e));
        } catch (Exception e) {
            log.warn("Неизвестная ошибка при выполнения запроса {} Сообщение: {}", req, e.getMessage());
            exceptionQueue.add(new ExceptionRecord(req,e));
        }
    }

    /**
     * Метод для повторной обработки запроса из очереди ошибок.
     * <p>
     * Отличается от {@link #handle(TransactionRequest)} тем, что
     * не отправляет ошибки в {@link ExceptionQueue} (это делает вызывающий код).
     * 
     *
     * @param req запрос на операцию
     * @throws Exception любое исключение, возникшее при выполнении
     */
    public void handleException(TransactionRequest req) throws Exception {
        if (req == null) throw new IllegalTransactionRequestException("Некорректный запрос на транзакцию");
        requestHandler.get(req.operationType()).accept(req);
    }
}

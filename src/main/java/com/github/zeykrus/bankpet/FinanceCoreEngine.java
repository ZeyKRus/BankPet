package com.github.zeykrus.bankpet;

import com.github.zeykrus.bankpet.account.Account;
import com.github.zeykrus.bankpet.exception.IllegalAccountException;
import com.github.zeykrus.bankpet.exception.IllegalTransactionRequestException;
import com.github.zeykrus.bankpet.exception.InsufficientFundsException;
import com.github.zeykrus.bankpet.interfaces.PeriodicOperation;
import com.github.zeykrus.bankpet.model.ExceptionRecord;
import com.github.zeykrus.bankpet.model.Transaction;
import com.github.zeykrus.bankpet.model.TransactionRequest;
import com.github.zeykrus.bankpet.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Оркестратор банковской системы.
 * <p>
 * Этот класс является фасадом для всей системы. Он управляет:
 * <ul>
 *   <li>обработкой запросов через {@link QueueProcessingService}</li>
 *   <li>обработкой ошибок через {@link ExceptionProcessingService}</li>
 *   <li>историей операций через {@link ActionHandler}</li>
 * </ul>
 * 
 *
 * <p>
 * Типичное использование:
 * <pre>
 * FinanceCoreEngine core = new FinanceCoreEngine();
 * Bank bank = core.createBank("MyBank");
 * core.startProcessingQueue(4);
 * core.startProcessingExceptions(2);
 * // ... операции
 * core.stopProcessingQueue();
 * core.stopProcessingExceptions();
 * </pre>
 * 
 */
public class FinanceCoreEngine {
    private static final Logger log = LoggerFactory.getLogger(FinanceCoreEngine.class);
    private final BankManager bankManager;
    private final ActionHandler actionHandler;
    private final QueueManager queueManager;
    private final ExceptionQueue exceptionQueue;
    private final ExceptionHandler exceptionHandler;
    private final QueueProcessingService queueProcessingService;
    private final ExceptionProcessingService exceptionProcessingService;


    public FinanceCoreEngine() {
        this.bankManager = new BankManager(this);
        this.queueManager = new QueueManager();
        this.exceptionQueue = new ExceptionQueue();
        this.actionHandler = new ActionHandler(new HistoryManager(), exceptionQueue);
        this.exceptionHandler = new ExceptionHandler(exceptionQueue,actionHandler);
        this.queueProcessingService = new QueueProcessingService(queueManager, actionHandler);
        this.exceptionProcessingService = new ExceptionProcessingService(exceptionQueue, exceptionHandler);
        log.info("Оркестратор инициализирован");
    }

    public FinanceCoreEngine(BankManager bankManager, ActionHandler actionHandler,
                             QueueManager queueManager, ExceptionQueue exceptionQueue,
                             ExceptionHandler exceptionHandler) {
        this.bankManager = bankManager;
        this.actionHandler = actionHandler;
        this.queueManager = queueManager;
        this.exceptionQueue = exceptionQueue;
        this.exceptionHandler = exceptionHandler;
        this.queueProcessingService = new QueueProcessingService(queueManager, actionHandler);
        this.exceptionProcessingService = new ExceptionProcessingService(exceptionQueue, exceptionHandler);
        log.info("Оркестратор инициализирован через расширенный конструктор");
    }

    public void newRequest(TransactionRequest req) {
        queueManager.add(req);
    }

    /**
     * Запускает обработку очереди запросов.
     * <p>
     * Создаётся фиксированный пул потоков, в котором запускаются воркеры
     * ({@link RequestProcessor}), каждый из которых:
     * <ul>
     *   <li>забирает запрос из {@link QueueManager}</li>
     *   <li>передаёт его в {@link ActionHandler}</li>
     *   <li>при ошибке отправляет в {@link ExceptionQueue}</li>
     * </ul>
     * 
     *
     * @param threadCount количество воркеров в пуле (должно быть > 0)
     * @throws IllegalArgumentException если threadCount less 0
     */
    public void startProcessingQueue(int threadCount) {
        queueProcessingService.start(threadCount);
    }

    /**
     * Останавливает обработку очереди запросов.
     * <p>
     * Процесс остановки:
     * <ol>
     *   <li>воркеры получают команду на завершение</li>
     *   <li>в очередь отправляются poison pill для каждого воркера</li>
     *   <li>пул потоков завершается с таймаутом 10 секунд</li>
     * </ol>
     * 
     */
    public void stopProcessingQueue() {
        queueProcessingService.shutdown();
    }

    /**
     * Запускает обработку очереди ошибок.
     * <p>
     * Создаётся пул воркеров ({@link ExceptionProcessor}), которые:
     * <ul>
     *   <li>забирают запись из {@link ExceptionQueue}</li>
     *   <li>передают её в {@link ExceptionHandler}</li>
     *   <li>при повторных ошибках возвращают запись обратно или отправляют в DLQ</li>
     * </ul>
     * 
     *
     * @param threadCount количество воркеров (должно быть > 0)
     */
    public void startProcessingExceptions(int threadCount) {
        exceptionProcessingService.start(threadCount);
    }

    /**
     * Останавливает обработку очереди ошибок.
     * <p>
     * Аналогично {@link #stopProcessingQueue()}, но для очереди ошибок.
     * 
     */
    public void stopProcessingExceptions() {
        exceptionProcessingService.shutdown();
    }

    /**
     * Выполняет периодические операции для всех счетов.
     * <p>
     * Для каждого счёта, реализующего {@link PeriodicOperation},
     * вызывается метод {@link PeriodicOperation#execute()}.
     * Это используется для начисления процентов по сберегательным и кредитным счетам.
     * 
     */
    public void executeAll() {
        log.info("Оркестратор выполняет метод executeAll()");
        List<Account> accounts = bankManager.getAllAccounts();
        accounts.stream()
                .filter(acc -> acc instanceof PeriodicOperation)
                .forEach(acc -> ((PeriodicOperation) acc).execute());
    }

    /**
     * Возвращает историю операций для указанного счёта.
     *
     * @param acc счёт
     * @return список транзакций (может быть пустым)
     */
    public List<Transaction> getHistory(Account acc) {
        return actionHandler.getHistory(acc);
    }

    /**
     * Создаёт новый банк.
     *
     * @param name название банка (уникальность не проверяется)
     * @return новый банк
     */
    public Bank createBank(String name) {
        return bankManager.generateNewBank(name);
    }

}

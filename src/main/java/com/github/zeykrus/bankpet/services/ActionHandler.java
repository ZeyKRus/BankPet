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

public class ActionHandler {
    private static final Logger log = LoggerFactory.getLogger(ActionHandler.class);
    private final Map<OperationType, ThrowingConsumer<TransactionRequest>> requestHandler = new HashMap<>();
    private final HistoryManager history;
    private final ExceptionQueue exceptionQueue;

    public ActionHandler(HistoryManager historyManager, ExceptionQueue exceptionQueue) {
        requestHandler.put(OperationType.DEPOSIT, this::deposit);
        requestHandler.put(OperationType.WITHDRAW, this::withdraw);
        requestHandler.put(OperationType.TRANSFER, this::transfer);
        this.history = historyManager;
        this.exceptionQueue = exceptionQueue;
        log.info("Сервис инициализирован: {}", this.getClass().getSimpleName());
    }

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

    public List<Transaction> getHistory(Account acc) {
        return history.getHistory(HistoryFilter.builder().acc(acc).build());
    }


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

    public void handleException(TransactionRequest req) throws Exception {
        if (req == null) throw new IllegalTransactionRequestException("Некорректный запрос на транзакцию");
        requestHandler.get(req.operationType()).accept(req);
    }
}

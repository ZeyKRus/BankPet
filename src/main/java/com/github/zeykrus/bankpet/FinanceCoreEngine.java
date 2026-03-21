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

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class FinanceCoreEngine {
    private final BankManager bankManager;
    private final ActionHandler actionHandler;
    private final QueueManager queueManager;
    private final ExceptionQueue exceptionQueue;
    private final ExceptionHandler exceptionHandler;
    private final QueueProcessingService queueProcessingService;
    private final ExceptionProcessingService exceptionProcessingService;

    private final int MAX_RETRIES = 5;




    public FinanceCoreEngine() {
        this.bankManager = new BankManager(this);
        this.queueManager = new QueueManager();
        this.exceptionQueue = new ExceptionQueue();
        this.actionHandler = new ActionHandler(new HistoryManager(), exceptionQueue);
        this.exceptionHandler = new ExceptionHandler(exceptionQueue,actionHandler);
        this.queueProcessingService = new QueueProcessingService(queueManager, actionHandler);
        this.exceptionProcessingService = new ExceptionProcessingService(exceptionQueue, exceptionHandler);
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
    }

    public void newRequest(TransactionRequest req) {
        queueManager.add(req);
    }

    public void startProcessingQueue(int threadCount) {
        queueProcessingService.start(threadCount);
    }

    public void stopProcessingQueue() {
        queueProcessingService.shutdown();
    }

    public void startProcessingExceptions(int threadCount) {
        exceptionProcessingService.start(threadCount);
    }

    public void stopProcessingExceptions() {
        exceptionProcessingService.shutdown();
    }


    public void executeAll() {
        List<Account> accounts = bankManager.getAllAccounts();
        accounts.stream()
                .filter(acc -> acc instanceof PeriodicOperation)
                .forEach(acc -> ((PeriodicOperation) acc).execute());
    }

    public List<Transaction> getHistory(Account acc) {
        return actionHandler.getHistory(acc);
    }

    public Bank createBank(String name) {
        return bankManager.generateNewBank(name);
    }

}

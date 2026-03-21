package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.exception.IllegalAccountException;
import com.github.zeykrus.bankpet.exception.IllegalTransactionRequestException;
import com.github.zeykrus.bankpet.exception.InsufficientFundsException;
import com.github.zeykrus.bankpet.model.ExceptionRecord;
import com.github.zeykrus.bankpet.model.TransactionRequest;

public class RequestProcessor implements Runnable {
    private final QueueManager queueManager;
    private final ActionHandler actionHandler;
    private volatile boolean running;

    public RequestProcessor(QueueManager queueManager, ActionHandler actionHandler) {
        this.actionHandler = actionHandler;
        this.queueManager = queueManager;
        this.running = true;
    }

    public void stop() {
        this.running = false;
    }

    @Override
    public void run() {
        while(running) {
            TransactionRequest task = null;
            try {
                task = queueManager.take();
                actionHandler.handle(task);
            } catch (InterruptedException e) {
                System.err.println("Ошибка в обработчике: " + e.getMessage());
                Thread.currentThread().interrupt();
                running = false;
            }
        }
    }
}

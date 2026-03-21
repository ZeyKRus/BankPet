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
    private volatile boolean readyForPoison;

    public RequestProcessor(QueueManager queueManager, ActionHandler actionHandler) {
        this.actionHandler = actionHandler;
        this.queueManager = queueManager;
        this.running = false;
        this.readyForPoison = false;
    }

    public void stop() {
        this.running = false;
        this.readyForPoison = true;
    }

    @Override
    public void run() {
        this.running = true;
        this.readyForPoison = false;
        while(running) {
            TransactionRequest task = null;
            try {
                task = queueManager.take();
                if (task == TransactionRequest.POISON) {
                    if (readyForPoison) {
                        Thread.currentThread().interrupt();
                        running = false;
                        break;
                    }
                } else {
                    actionHandler.handle(task);
                }
            } catch (InterruptedException e) {
                System.err.println("Ошибка в обработчике: " + e.getMessage());
                Thread.currentThread().interrupt();
                running = false;
                break;
            }
        }
    }
}

package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.exception.IllegalAccountException;
import com.github.zeykrus.bankpet.exception.IllegalTransactionRequestException;
import com.github.zeykrus.bankpet.exception.InsufficientFundsException;
import com.github.zeykrus.bankpet.model.ExceptionRecord;
import com.github.zeykrus.bankpet.model.TransactionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestProcessor implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(RequestProcessor.class);
    private final QueueManager queueManager;
    private final ActionHandler actionHandler;
    private volatile boolean running;
    private volatile boolean readyForPoison;

    public RequestProcessor(QueueManager queueManager, ActionHandler actionHandler) {
        log.debug("Создан поток");
        this.actionHandler = actionHandler;
        this.queueManager = queueManager;
        this.running = false;
        this.readyForPoison = false;
    }

    public void stop() {
        log.debug("Поток получил команду на остановку");
        this.running = false;
        this.readyForPoison = true;
    }

    @Override
    public void run() {
        log.debug("Поток запущен");
        this.running = true;
        this.readyForPoison = false;
        while(running) {
            TransactionRequest task = null;
            try {
                task = queueManager.take();
                log.trace("Поток получил задачу {}",task);
                if (task == TransactionRequest.POISON) {
                    log.trace("Поток получил POISON");
                    if (readyForPoison) {
                        log.debug("Поток останавливается из-за POISON");
                        Thread.currentThread().interrupt();
                        running = false;
                        break;
                    }
                } else {
                    log.trace("Поток обрабатывает задачу {}",task);
                    actionHandler.handle(task);
                }
            } catch (InterruptedException e) {
                log.warn("Ошибка в обработчике: {}", e.getMessage());
                Thread.currentThread().interrupt();
                running = false;
                break;
            }
        }
    }
}

package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.model.ExceptionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionProcessor implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(ExceptionProcessor.class);
    private final ExceptionQueue exceptionQueue;
    private final ExceptionHandler exceptionHandler;
    private volatile boolean running;
    private volatile boolean readyForPoison;

    public ExceptionProcessor(ExceptionQueue exceptionQueue, ExceptionHandler exceptionHandler) {
        log.debug("Создан поток");
        this.exceptionQueue = exceptionQueue;
        this.exceptionHandler = exceptionHandler;
        this.running = false;
        this.readyForPoison = false;
    }

    @Override
    public void run() {
        log.debug("Поток запущен");
        this.running = true;
        this.readyForPoison = false;
        while(running) {
            ExceptionRecord rec = null;
            try {
                rec = exceptionQueue.take();
                log.trace("Поток получил задачу {}",rec);
                if (rec == ExceptionRecord.POISON) {
                    log.trace("Поток получил POISON");
                    if (readyForPoison) {
                        log.debug("Поток останавливается из-за POISON");
                        Thread.currentThread().interrupt();
                        running = false;
                        break;
                    }
                } else {
                    log.trace("Поток обрабатывает задачу {}",rec);
                    exceptionHandler.handle(rec);
                }
            } catch (InterruptedException e) {
                log.warn("Ошибка в обработчике: {}", e.getMessage());
                Thread.currentThread().interrupt();
                running = false;
                break;
            }
        }
    }

    public void stop() {
        log.debug("Поток получил команду на остановку");
        this.running = false;
        this.readyForPoison = true;
    }
}

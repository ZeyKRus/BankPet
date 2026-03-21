package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.model.ExceptionRecord;

public class ExceptionProcessor implements Runnable {
    private final ExceptionQueue exceptionQueue;
    private final ExceptionHandler exceptionHandler;
    private volatile boolean running;

    public ExceptionProcessor(ExceptionQueue exceptionQueue, ExceptionHandler exceptionHandler) {
        this.exceptionQueue = exceptionQueue;
        this.exceptionHandler = exceptionHandler;
        running = false;
    }

    @Override
    public void run() {
        running = true;
        while(running) {
            ExceptionRecord rec = null;
            try {
                rec = exceptionQueue.take();
                exceptionHandler.handle(rec);
            } catch (InterruptedException e) {
                System.err.println("Ошибка в обработчике: " + e.getMessage());
                Thread.currentThread().interrupt();
                running = false;
            }
        }
    }

    public void stop() {
        this.running = false;
    }
}

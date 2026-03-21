package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.model.ExceptionRecord;

public class ExceptionProcessor implements Runnable {
    private final ExceptionQueue exceptionQueue;
    private final ExceptionHandler exceptionHandler;
    private volatile boolean running;
    private volatile boolean readyForPoison;

    public ExceptionProcessor(ExceptionQueue exceptionQueue, ExceptionHandler exceptionHandler) {
        this.exceptionQueue = exceptionQueue;
        this.exceptionHandler = exceptionHandler;
        this.running = false;
        this.readyForPoison = false;
    }

    @Override
    public void run() {
        this.running = true;
        this.readyForPoison = false;
        while(running) {
            ExceptionRecord rec = null;
            try {
                rec = exceptionQueue.take();
                if (rec == ExceptionRecord.POISON) {
                    if (readyForPoison) {
                        Thread.currentThread().interrupt();
                        running = false;
                        break;
                    }
                } else {
                    exceptionHandler.handle(rec);
                }
            } catch (InterruptedException e) {
                System.err.println("Ошибка в обработчике: " + e.getMessage());
                Thread.currentThread().interrupt();
                running = false;
                break;
            }
        }
    }

    public void stop() {
        this.running = false;
        this.readyForPoison = true;
    }
}

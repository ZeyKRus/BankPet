package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.model.ExceptionRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExceptionProcessingService {
    private final static int SOFT_SHUTDOWN_TIMEOUT_MS = 10; //Пока оставлено 10. Для реальной системы следует изменить на более подходящее (эмпирическим путем?)
    private final List<ExceptionProcessor> processors;
    private final ExceptionQueue exceptionQueue;
    private final ExceptionHandler exceptionHandler;
    private ExecutorService executor;
    private final AtomicBoolean running;

    public ExceptionProcessingService(ExceptionQueue exceptionQueue, ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        this.exceptionQueue = exceptionQueue;
        running = new AtomicBoolean(false);
        processors = new ArrayList<>();
    }

    public void start(int threadCount) {
        if (running.compareAndSet(false, true)) {
            executor = Executors.newFixedThreadPool(threadCount);
            for (int i = 0; i < threadCount; i++) {
                ExceptionProcessor proc = new ExceptionProcessor(exceptionQueue, exceptionHandler);
                processors.add(proc);
                executor.submit(proc);
            }
        }
    }

    public void shutdown() {
        if (running.compareAndSet(true, false)) {
            for (ExceptionProcessor proc : processors) proc.stop();
            for (ExceptionProcessor ignored : processors) exceptionQueue.add(ExceptionRecord.POISON);
            processors.clear();
            executor.shutdown();
            try {
                if (!executor.awaitTermination(SOFT_SHUTDOWN_TIMEOUT_MS, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
            executor = null;
        }
    }
}

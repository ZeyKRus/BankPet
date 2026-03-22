package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.model.TransactionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class QueueProcessingService {
    private static final Logger log = LoggerFactory.getLogger(QueueProcessingService.class);
    private static final int SOFT_SHUTDOWN_TIMEOUT_MS = 10; //Пока оставлено 10. Для реальной системы следует изменить на более подходящее (эмпирическим путем?)

    private final List<RequestProcessor> processors;
    private final QueueManager queueManager;
    private final ActionHandler actionHandler;
    private final AtomicBoolean running;
    private ExecutorService executor;

    public QueueProcessingService(QueueManager queueManager, ActionHandler actionHandler) {
        log.info("Сервис инициализирован: {}", this.getClass().getSimpleName());
        this.queueManager = queueManager;
        this.actionHandler = actionHandler;
        this.processors = new ArrayList<>();
        this.running = new AtomicBoolean(false);
    }

    public void start(int threadsCount) {
        log.info("Запуск сервиса: {}", this.getClass().getSimpleName());
        if (running.compareAndSet(false, true)) {
            executor = Executors.newFixedThreadPool(threadsCount);
            for (int i = 0; i < threadsCount; i++) {
                RequestProcessor proc = new RequestProcessor(queueManager, actionHandler);
                processors.add(proc);
                executor.submit(proc);
            }
        }
        log.info("Сервис запущен: {}", this.getClass().getSimpleName());
    }

    public void shutdown() {
        log.info("Сервис завершает работу: {}", this.getClass().getSimpleName());
        if (running.compareAndSet(true, false)) {
            for (RequestProcessor proc : processors) proc.stop();
            for (RequestProcessor ignored : processors) queueManager.add(TransactionRequest.POISON);
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
        log.info("Сервис завершил работу: {}", this.getClass().getSimpleName());
    }
}

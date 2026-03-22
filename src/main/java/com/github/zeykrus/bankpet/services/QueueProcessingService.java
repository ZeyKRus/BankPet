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

/**
 * Сервис для асинхронной обработки запросов с пулом воркеров.
 * <p>
 * Этот класс управляет пулом потоков, которые обрабатывают запросы из {@link QueueManager}.
 * Каждый воркер ({@link RequestProcessor}) в бесконечном цикле забирает запросы из очереди
 * и передаёт их в {@link ActionHandler} для выполнения бизнес-логики.
 * 
 *
 * <p>
 * Основные возможности:
 * <ul>
 *   <li>запуск указанного количества воркеров в фиксированном пуле потоков</li>
 *   <li>корректное завершение всех воркеров при остановке сервиса</li>
 *   <li>использование poison pill для гарантированного завершения потоков</li>
 *   <li>graceful shutdown с таймаутом и принудительной остановкой</li>
 * </ul>
 * 
 *
 * <p>
 * Жизненный цикл:
 * <ol>
 *   <li>Создание сервиса (конструктор)</li>
 *   <li>Запуск через {@link #start(int)} — создаётся пул потоков и запускаются воркеры</li>
 *   <li>Работа — воркеры обрабатывают запросы из очереди</li>
 *   <li>Остановка через {@link #shutdown()} — отправляются poison pill, пул завершается</li>
 * </ol>
 * 
 *
 * <p>
 * Механизм остановки:
 * <ol>
 *   <li>Всем воркерам выставляется флаг остановки</li>
 *   <li>В очередь добавляется poison pill для каждого воркера</li>
 *   <li>Пул потоков переводится в состояние {@link ExecutorService#shutdown()}</li>
 *   <li>Ожидание завершения всех потоков с таймаутом {@value #SOFT_SHUTDOWN_TIMEOUT_MS} секунд</li>
 *   <li>Если таймаут истёк — вызывается {@link ExecutorService#shutdownNow()}</li>
 * </ol>
 * 
 *
 * <p>
 * Сервис потокобезопасен и может быть запущен/остановлен только один раз.
 * Повторный вызов {@link #start(int)} после остановки проигнорируется.
 * 
 *
 * @see RequestProcessor
 * @see QueueManager
 * @see ActionHandler
 */
public class QueueProcessingService {
    private static final Logger log = LoggerFactory.getLogger(QueueProcessingService.class);

    /**
     * Таймаут ожидания завершения воркеров при остановке (в секундах).
     * <p>
     * После этого таймаута вызывается принудительная остановка через {@link ExecutorService#shutdownNow()}.
     * Значение подобрано эмпирически и может быть изменено в зависимости от нагрузки.
     * 
     */
    private static final int SOFT_SHUTDOWN_TIMEOUT_MS = 10; //Пока оставлено 10. Для реальной системы следует изменить на более подходящее (эмпирическим путем?)

    private final List<RequestProcessor> processors;
    private final QueueManager queueManager;
    private final ActionHandler actionHandler;
    private final AtomicBoolean running;
    private ExecutorService executor;

    /**
     * Создаёт сервис обработки запросов.
     *
     * @param queueManager   менеджер очереди запросов (не может быть null)
     * @param actionHandler  обработчик бизнес-логики (не может быть null)
     */
    public QueueProcessingService(QueueManager queueManager, ActionHandler actionHandler) {
        log.info("Сервис инициализирован: {}", this.getClass().getSimpleName());
        this.queueManager = queueManager;
        this.actionHandler = actionHandler;
        this.processors = new ArrayList<>();
        this.running = new AtomicBoolean(false);
    }

    /**
     * Запускает сервис с указанным количеством воркеров.
     * <p>
     * Создаётся фиксированный пул потоков ({@link Executors#newFixedThreadPool(int)}),
     * в котором запускаются воркеры {@link RequestProcessor}.
     * Каждый воркер будет обрабатывать запросы из очереди.
     * 
     * <p>
     * Повторный вызов после остановки сервиса игнорируется.
     * 
     *
     * @param threadsCount количество воркеров (должно быть > 0)
     * @throws IllegalArgumentException если threadsCount less 0
     */
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

    /**
     * Останавливает сервис и завершает все воркеры.
     * <p>
     * Процесс остановки:
     * <ol>
     *   <li>Всем воркерам выставляется флаг остановки</li>
     *   <li>В очередь добавляется poison pill для каждого воркера</li>
     *   <li>Пул потоков переводится в состояние {@link ExecutorService#shutdown()}</li>
     *   <li>Ожидание завершения в течение {@value #SOFT_SHUTDOWN_TIMEOUT_MS} секунд</li>
     *   <li>Если воркеры не завершились, вызывается {@link ExecutorService#shutdownNow()}</li>
     * </ol>
     * 
     * <p>
     * Повторный вызов после остановки сервиса игнорируется.
     * 
     */
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

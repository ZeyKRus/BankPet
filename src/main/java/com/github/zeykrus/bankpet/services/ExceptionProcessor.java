package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.model.ExceptionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Воркер для обработки ошибок из очереди.
 * <p>
 * Этот класс реализует {@link Runnable} и предназначен для выполнения в пуле потоков
 * ({@link ExceptionProcessingService}). Каждый воркер в бесконечном цикле забирает записи
 * из {@link ExceptionQueue} и передаёт их в {@link ExceptionHandler} для повторных попыток.
 * 
 *
 * <p> * Механизм остановки:
 * <ul>
 *   <li>используется флаг {@code running} для контроля выполнения цикла</li>
 *   <li>при получении poison pill ({@link ExceptionRecord#POISON}) и установленном флаге
 *       {@code readyForPoison} воркер завершается</li>
 *   <li>при прерывании потока ({@link InterruptedException}) воркер корректно завершается</li>
 * </ul>
 * 
 *
 * <p>
 * Воркер потокобезопасен и может использоваться одновременно с другими воркерами,
 * так как вся синхронизация обеспечивается на уровне {@link ExceptionQueue} и
 * {@link ExceptionHandler}.
 * 
 *
 * @see ExceptionQueue
 * @see ExceptionHandler
 * @see ExceptionProcessingService
 */
public class ExceptionProcessor implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(ExceptionProcessor.class);
    private final ExceptionQueue exceptionQueue;
    private final ExceptionHandler exceptionHandler;
    private volatile boolean running;
    private volatile boolean readyForPoison;

    /**
     * Создаёт воркер для обработки ошибок.
     *
     * @param exceptionQueue   очередь ошибок (не может быть null)
     * @param exceptionHandler обработчик ошибок (не может быть null)
     */
    public ExceptionProcessor(ExceptionQueue exceptionQueue, ExceptionHandler exceptionHandler) {
        log.debug("Создан поток");
        this.exceptionQueue = exceptionQueue;
        this.exceptionHandler = exceptionHandler;
        this.running = false;
        this.readyForPoison = false;
    }

    /**
     * Основной цикл обработки ошибок.
     * <p>
     * Алгоритм:
     * <ol>
     *   <li>Устанавливает флаги {@code running = true} и {@code readyForPoison = false}</li>
     *   <li>В бесконечном цикле:
     *     <ul>
     *       <li>забирает запись из очереди ({@link ExceptionQueue#take()})</li>
     *       <li>если получен poison pill и флаг {@code readyForPoison} установлен → завершается</li>
     *       <li>иначе передаёт запись в {@link ExceptionHandler#handle(ExceptionRecord)}</li>
     *     </ul>
     *   </li>
     *   <li>При получении {@link InterruptedException} корректно завершает работу</li>
     * </ol>
     * 
     * <p>
     * Воркер может быть остановлен двумя способами:
     * <ul>
     *   <li>через метод {@link #stop()} (устанавливает флаги, после чего poison pill завершит цикл)</li>
     *   <li>через прерывание потока (например, при {@link ExceptionProcessingService#shutdown()})</li>
     * </ul>
     * 
     */
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

    /**
     * Останавливает воркер.
     * <p>
     * Устанавливает флаги:
     * <ul>
     *   <li>{@code running = false} — выход из основного цикла</li>
     *   <li>{@code readyForPoison = true} — разрешает завершение при получении poison pill</li>
     * </ul>
     * 
     * <p>
     * После вызова этого метода воркер завершится, как только получит poison pill из очереди.
     * 
     */
    public void stop() {
        log.debug("Поток получил команду на остановку");
        this.running = false;
        this.readyForPoison = true;
    }
}

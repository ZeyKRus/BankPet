package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.exception.IllegalAccountException;
import com.github.zeykrus.bankpet.exception.IllegalTransactionRequestException;
import com.github.zeykrus.bankpet.exception.InsufficientFundsException;
import com.github.zeykrus.bankpet.model.ExceptionRecord;
import com.github.zeykrus.bankpet.model.TransactionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Воркер для обработки запросов из очереди.
 * <p>
 * Этот класс реализует {@link Runnable} и предназначен для выполнения в пуле потоков
 * ({@link QueueProcessingService}). Каждый воркер в бесконечном цикле забирает запросы
 * из {@link QueueManager} и передаёт их в {@link ActionHandler} для выполнения
 * бизнес-логики.
 * 
 *
 * <p>
 * Механизм остановки:
 * <ul>
 *   <li>используется флаг {@code running} для контроля выполнения цикла</li>
 *   <li>при получении poison pill ({@link TransactionRequest#POISON}) и установленном флаге
 *       {@code readyForPoison} воркер завершается</li>
 *   <li>при прерывании потока ({@link InterruptedException}) воркер корректно завершается</li>
 * </ul>
 * 
 *
 * <p>
 * Воркер потокобезопасен и может использоваться одновременно с другими воркерами,
 * так как вся синхронизация обеспечивается на уровне {@link QueueManager} и
 * {@link ActionHandler}.
 * 
 *
 * <p>
 * Приоритет обработки:
 * <ul>
 *   <li>запросы извлекаются из очереди в порядке приоритета (большая сумма → выше приоритет)</li>
 *   <li>приоритет определяется компаратором {@link TransactionRequest#compareTo(TransactionRequest)}</li>
 * </ul>
 * 
 *
 * @see QueueManager
 * @see ActionHandler
 * @see QueueProcessingService
 */
public class RequestProcessor implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(RequestProcessor.class);
    private final QueueManager queueManager;
    private final ActionHandler actionHandler;
    private volatile boolean running;
    private volatile boolean readyForPoison;

    /**
     * Создаёт воркер для обработки запросов.
     *
     * @param queueManager   менеджер очереди запросов (не может быть null)
     * @param actionHandler  обработчик бизнес-логики (не может быть null)
     */
    public RequestProcessor(QueueManager queueManager, ActionHandler actionHandler) {
        log.debug("Создан поток");
        this.actionHandler = actionHandler;
        this.queueManager = queueManager;
        this.running = false;
        this.readyForPoison = false;
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

    /**
     * Основной цикл обработки запросов.
     * <p>
     * Алгоритм:
     * <ol>
     *   <li>Устанавливает флаги {@code running = true} и {@code readyForPoison = false}</li>
     *   <li>В бесконечном цикле:
     *     <ul>
     *       <li>забирает запрос из очереди ({@link QueueManager#take()})</li>
     *       <li>если получен poison pill и флаг {@code readyForPoison} установлен → завершается</li>
     *       <li>иначе передаёт запрос в {@link ActionHandler#handle(TransactionRequest)}</li>
     *     </ul>
     *   </li>
     *   <li>При получении {@link InterruptedException} корректно завершает работу</li>
     * </ol>
     * 
     * <p>
     * Воркер может быть остановлен двумя способами:
     * <ul>
     *   <li>через метод {@link #stop()} (устанавливает флаги, после чего poison pill завершит цикл)</li>
     *   <li>через прерывание потока (например, при {@link QueueProcessingService#shutdown()})</li>
     * </ul>
     * 
     */
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

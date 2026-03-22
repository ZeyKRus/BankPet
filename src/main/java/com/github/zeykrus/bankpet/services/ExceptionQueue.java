package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.model.ExceptionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Потокобезопасная очередь для хранения ошибочных запросов.
 * <p>
 * Эта очередь является связующим звеном между {@link ActionHandler} и
 * {@link ExceptionProcessingService}. Сюда попадают запросы, которые не удалось
 * обработать с первого раза (недостаточно средств, ошибки валидации, сетевые ошибки и т.д.).
 * 
 *
 * <p>
 * Особенности:
 * <ul>
 *   <li>используется неограниченная очередь {@link LinkedBlockingQueue}</li>
 *   <li>потокобезопасна — может использоваться одновременно из разных потоков</li>
 *   <li>метод {@link #take()} блокирует поток до появления элемента</li>
 *   <li>в очередь также могут добавляться poison pill для остановки воркеров</li>
 * </ul>
 * 
 *
 * <p>
 * Жизненный цикл записи:
 * <ol>
 *   <li>{@link ActionHandler} добавляет запись через {@link #add(ExceptionRecord)}</li>
 *   <li>{@link ExceptionProcessor} извлекает запись через {@link #take()}</li>
 *   <li>Запись обрабатывается {@link ExceptionHandler}</li>
 *   <li>Если обработка неудачна, запись может быть возвращена в очередь</li>
 * </ol>
 * 
 *
 * @see ExceptionRecord
 * @see ActionHandler
 * @see ExceptionProcessingService
 */
public class ExceptionQueue {
    private static final Logger log = LoggerFactory.getLogger(ExceptionQueue.class);
    private final BlockingQueue<ExceptionRecord> queue = new LinkedBlockingQueue<>();

    /**
     * Добавляет запись в очередь.
     * <p>
     * Операция неблокирующая. Если очередь неограничена (как в данном случае),
     * всегда возвращает управление немедленно.
     * 
     *
     * @param e запись об ошибке (не может быть null)
     * @throws NullPointerException если e == null
     */
    public void add(ExceptionRecord e) {
        queue.add(e);
        log.trace("В очередь {} помещена запись {}",this,e);
    }

    /**
     * Извлекает запись из очереди, блокируя поток до появления элемента.
     * <p>
     * Этот метод используется воркерами {@link ExceptionProcessor} для получения
     * задач на обработку. Поток блокируется до тех пор, пока в очереди не появится
     * элемент или пока не будет прерван.
     * 
     *
     * @return запись об ошибке (никогда не возвращает null)
     * @throws InterruptedException если поток был прерван во время ожидания
     */
    public ExceptionRecord take() throws InterruptedException {
        log.trace("Запрос объекта из очереди ошибок {}",this);
        return queue.take();
    }

    public int size() { return queue.size(); }
}

package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.model.TransactionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Потокобезопасная очередь запросов с приоритетом.
 * <p>
 * Эта очередь является основным каналом для передачи запросов от клиентов
 * к обработчикам ({@link RequestProcessor}). Все запросы сортируются по приоритету,
 * который определяется суммой транзакции (чем больше сумма, тем выше приоритет).
 * 
 *
 * <p>
 * Особенности:
 * <ul>
 *   <li>используется {@link PriorityBlockingQueue} для автоматической сортировки</li>
 *   <li>сравнение запросов выполняется через {@link TransactionRequest#compareTo(TransactionRequest)}</li>
 *   <li>начальная ёмкость установлена в 11 элементов (может расширяться автоматически)</li>
 *   <li>потокобезопасна — может использоваться одновременно из разных потоков</li>
 * </ul>
 * 
 *
 * <p>
 * Приоритетная обработка:
 * <ol>
 *   <li>Запросы с большей суммой имеют более высокий приоритет</li>
 *   <li>Если суммы равны, порядок не гарантирован (зависит от реализации кучи)</li>
 * </ol>
 * 
 *
 * <p>
 * В очередь также могут добавляться специальные маркерные записи (poison pill)
 * для остановки воркеров ({@link TransactionRequest#POISON}).
 * 
 *
 * @author ZeyKRus
 * @since 1.0
 * @see TransactionRequest
 * @see RequestProcessor
 * @see QueueProcessingService
 */
public class QueueManager {
    private static final Logger log = LoggerFactory.getLogger(QueueManager.class);

    /**
     * Очередь запросов с приоритетом.
     * <p>
     * Начальная ёмкость: 11
     * Компаратор: {@link TransactionRequest#compareTo(TransactionRequest)} (сортировка по убыванию суммы)
     * 
     */
    private final BlockingQueue<TransactionRequest> transactionQueue = new PriorityBlockingQueue<>(11,TransactionRequest::compareTo);

    /**
     * Добавляет запрос в очередь.
     * <p>
     * Операция неблокирующая. Запрос автоматически размещается в очереди
     * согласно его приоритету.
     * 
     *
     * @param req запрос на операцию (не может быть null)
     * @throws NullPointerException если req == null
     */
    public void add(TransactionRequest req) {
        log.trace("В очередь {} помещена запись {}",this,req);
        transactionQueue.add(req);
    }

    /**
     * Извлекает запрос из очереди, блокируя поток до появления элемента.
     * <p>
     * Этот метод используется воркерами {@link RequestProcessor} для получения
     * задач на обработку. Поток блокируется до тех пор, пока в очереди не появится
     * элемент или пока не будет прерван.
     * 
     * <p>
     * Запрос извлекается с учётом приоритета — первым возвращается запрос с наибольшей суммой.
     * 
     *
     * @return запрос на операцию (никогда не возвращает null)
     * @throws InterruptedException если поток был прерван во время ожидания
     */
    public TransactionRequest take() throws InterruptedException {
        log.trace("Запрос объекта из очереди ошибок {}",this);
        return transactionQueue.take();
    }

    public int size() { return transactionQueue.size(); }

}

package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.model.ExceptionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * Обработчик ошибок с поддержкой повторных попыток (retry) и Dead Letter Queue (DLQ).
 * <p>
 * Этот класс реализует механизм надёжной обработки сбойных запросов:
 * <ul>
 *   <li>каждая ошибка сопровождается счётчиком попыток ({@link ExceptionRecord#getFailings()})</li>
 *   <li>при каждом обращении счётчик увеличивается</li>
 *   <li>если количество попыток достигает {@value #MAX_RETRIES}, запись отправляется в DLQ</li>
 *   <li>иначе запись возвращается в {@link ExceptionQueue} для повторной обработки</li>
 * </ul>
 * 
 *
 * <p>
 * Механизм работы:
 * <ol>
 *   <li>Воркер ({@link ExceptionProcessor}) извлекает запись из {@link ExceptionQueue}</li>
 *   <li>Вызывает {@link #handle(ExceptionRecord)}</li>
 *   <li>Если обработка через {@link ActionHandler} успешна → запись удаляется</li>
 *   <li>Если неудачна → запись возвращается в очередь (с увеличенным счётчиком) или уходит в DLQ</li>
 * </ol>
 * 
 *
 * <p>
 * Dead Letter Queue (DLQ) хранит записи, которые не удалось обработать после
 * {@value #MAX_RETRIES} попыток. Они доступны через {@link #getDeadLetterQueue()}
 * для последующего анализа или ручного вмешательства.
 * 
 *
 * @see ExceptionRecord
 * @see ExceptionQueue
 * @see ExceptionProcessingService
 */
public class ExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ExceptionHandler.class);

    /**
     * Максимальное количество попыток обработки ошибки.
     * <p>
     * После достижения этого лимита запись отправляется в Dead Letter Queue.
     * 
     */
    public static final int MAX_RETRIES = 5;
    private final ExceptionQueue exceptionQueue;
    private final ActionHandler actionHandler;
    private final List<ExceptionRecord> deadLetterQueue;

    /**
     * Создаёт обработчик ошибок.
     *
     * @param exceptionQueue  очередь для возврата записей на повторную обработку
     * @param actionHandler   обработчик бизнес-логики для выполнения запросов
     */
    public ExceptionHandler(ExceptionQueue exceptionQueue, ActionHandler actionHandler) {
        log.info("Сервис инициализирован: {}", this.getClass().getSimpleName());
        this.actionHandler = actionHandler;
        this.exceptionQueue = exceptionQueue;
        this.deadLetterQueue = new LinkedList<>();
    }

    /**
     * Обрабатывает ошибочную запись.
     * <p>
     * Алгоритм:
     * <ol>
     *   <li>Увеличивает счётчик попыток</li>
     *   <li>Если достигнут лимит {@value #MAX_RETRIES} → отправляет в DLQ</li>
     *   <li>Иначе пытается выполнить запрос через {@link ActionHandler}</li>
     *   <li>Если выполнение успешно → запись удаляется</li>
     *   <li>Если выполнение неудачно → запись возвращается в очередь для следующей попытки</li>
     * </ol>
     * 
     *
     * @param record запись об ошибке (содержит исходный запрос, исключение и счётчик попыток)
     */
    public void handle(ExceptionRecord record) {
        record.incrementFailings();
        log.debug("Обработка ошибки: {}", record);
        if (record.getFailings() >= MAX_RETRIES) {
            deadLetterQueue.add(record);
            log.warn("Новая повторяющаяся ошибка {} Требует внимания администратора",record);
        } else {
            //TODO добавить обработку исключений всех видов
            try {
                actionHandler.handleException(record.getReq());//Пока заглушка, чтобы не терялись задачи. Если будут опять ошибки - все равно улетит в DLQ
                log.trace("Ошибка успешно обработана: {}",record);
            } catch (Exception e) {
                exceptionQueue.add(record);
                log.trace("Исключение при обработке ошибки: {}",record);
            }
        }
    }

    /**
     * Возвращает список записей, которые не удалось обработать после {@value #MAX_RETRIES} попыток.
     * <p>
     * Dead Letter Queue предназначена для:
     * <ul>
     *   <li>диагностики проблем, требующих ручного вмешательства</li>
     *   <li>логирования критических ошибок</li>
     *   <li>возможного повторного запуска после устранения причины</li>
     * </ul>
     * 
     *
     * @return список записей в DLQ (может быть пустым)
     */
    public List<ExceptionRecord> getDeadLetterQueue() {
        return deadLetterQueue;
    }
}

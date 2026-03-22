package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.model.ExceptionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class ExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ExceptionHandler.class);
    public static final int MAX_RETRIES = 5;
    private final ExceptionQueue exceptionQueue;
    private final ActionHandler actionHandler;
    private final List<ExceptionRecord> deadLetterQueue;

    public ExceptionHandler(ExceptionQueue exceptionQueue, ActionHandler actionHandler) {
        log.info("Сервис инициализирован: {}", this.getClass().getSimpleName());
        this.actionHandler = actionHandler;
        this.exceptionQueue = exceptionQueue;
        this.deadLetterQueue = new LinkedList<>();
    }

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

    public List<ExceptionRecord> getDeadLetterQueue() {
        return deadLetterQueue;
    }
}

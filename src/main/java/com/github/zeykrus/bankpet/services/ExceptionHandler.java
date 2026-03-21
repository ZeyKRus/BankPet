package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.model.ExceptionRecord;

import java.util.LinkedList;
import java.util.List;

public class ExceptionHandler {
    private static final int MAX_RETRIES = 5;
    private final ExceptionQueue exceptionQueue;
    private final ActionHandler actionHandler;
    private final List<ExceptionRecord> deadLetterQueue;

    public ExceptionHandler(ExceptionQueue exceptionQueue, ActionHandler actionHandler) {
        this.actionHandler = actionHandler;
        this.exceptionQueue = exceptionQueue;
        this.deadLetterQueue = new LinkedList<>();
    }

    public void handle(ExceptionRecord record) {
        if (record.getFailings() >= MAX_RETRIES) {
            deadLetterQueue.add(record);
            System.err.println("Новая повторяющаяся ошибка, требуется внимание администратора");
        } else {
            //TODO добавить обработку исключений всех видов
            try {
                record.incrementFailings();
                actionHandler.handleException(record.getReq());//Пока заглушка, чтобы не терялись задачи. Если будут опять ошибки - все равно улетит в DLQ
            } catch (Exception e) {
                exceptionQueue.add(record);
            }
        }
    }

    public List<ExceptionRecord> getDeadLetterQueue() {
        return deadLetterQueue;
    }
}

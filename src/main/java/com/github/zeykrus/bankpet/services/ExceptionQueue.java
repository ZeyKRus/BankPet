package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.model.ExceptionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ExceptionQueue {
    private static final Logger log = LoggerFactory.getLogger(ExceptionQueue.class);
    private final BlockingQueue<ExceptionRecord> queue = new LinkedBlockingQueue<>();

    public void add(ExceptionRecord e) {
        queue.add(e);
        log.trace("В очередь {} помещена запись {}",this,e);
    }

    public ExceptionRecord take() throws InterruptedException {
        log.trace("Запрос объекта из очереди ошибок {}",this);
        return queue.take();
    }

    public int size() { return queue.size(); }
}

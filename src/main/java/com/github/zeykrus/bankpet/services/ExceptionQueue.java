package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.model.ExceptionRecord;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ExceptionQueue {
    private final BlockingQueue<ExceptionRecord> queue = new LinkedBlockingQueue<>();

    public void add(ExceptionRecord e) {
        queue.add(e);
    }

    public ExceptionRecord take() throws InterruptedException {
        return queue.take();
    }

    public int size() { return queue.size(); }
}

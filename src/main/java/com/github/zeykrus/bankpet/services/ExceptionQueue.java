package main.java.com.github.zeykrus.bankpet.services;

import main.java.com.github.zeykrus.bankpet.model.ExceptionRecord;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

public class ExceptionQueue {
    private final Queue<ExceptionRecord> queue = new LinkedList<>();;

    public void add(ExceptionRecord e) {
        queue.add(e);
    }

    public Optional<ExceptionRecord> poll() {
        return Optional.ofNullable(queue.poll());
    }
}

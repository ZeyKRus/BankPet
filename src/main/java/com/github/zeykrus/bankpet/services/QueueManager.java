package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.model.TransactionRequest;

import java.util.Optional;
import java.util.PriorityQueue;

public class QueueManager {
    private final PriorityQueue<TransactionRequest> transactionQueue = new PriorityQueue<>(TransactionRequest::compareTo);

    public void add(TransactionRequest req) {
        transactionQueue.add(req);
    }

    public Optional<TransactionRequest> poll() {
        return Optional.ofNullable(transactionQueue.poll());
    }

}

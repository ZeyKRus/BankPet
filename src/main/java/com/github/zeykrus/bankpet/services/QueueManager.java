package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.model.TransactionRequest;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class QueueManager {
    private final BlockingQueue<TransactionRequest> transactionQueue = new PriorityBlockingQueue<>(11,TransactionRequest::compareTo);

    public void add(TransactionRequest req) {
        transactionQueue.add(req);
    }

    public TransactionRequest take() throws InterruptedException {
        return transactionQueue.take();
    }

    public int size() { return transactionQueue.size(); }

}

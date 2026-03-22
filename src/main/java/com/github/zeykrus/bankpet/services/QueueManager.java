package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.model.TransactionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class QueueManager {
    private static final Logger log = LoggerFactory.getLogger(QueueManager.class);
    private final BlockingQueue<TransactionRequest> transactionQueue = new PriorityBlockingQueue<>(11,TransactionRequest::compareTo);

    public void add(TransactionRequest req) {
        log.trace("В очередь {} помещена запись {}",this,req);
        transactionQueue.add(req);
    }

    public TransactionRequest take() throws InterruptedException {
        log.trace("Запрос объекта из очереди ошибок {}",this);
        return transactionQueue.take();
    }

    public int size() { return transactionQueue.size(); }

}

package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.model.TransactionRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class QueueProcessingServiceTest {
    private QueueProcessingService service;
    private QueueManager queue;
    private ActionHandler mockHandler;

    @BeforeEach
    void setUp() {
        mockHandler = Mockito.mock(ActionHandler.class);
        queue = new QueueManager();
        service = new QueueProcessingService(queue, mockHandler);
    }

    @AfterEach
    void tearDown() {
        service.shutdown();
    }

    @Test
    void shouldProcessAfterStart() throws InterruptedException {
        TransactionRequest tr = new TransactionRequest(null, null, null, 0);
        queue.add(tr);

        CountDownLatch latch = new CountDownLatch(1);
        Mockito.doAnswer(invocationOnMock -> {
            latch.countDown();
            return null;
        }).when(mockHandler).handle(Mockito.any());

        service.start(1);

        Assertions.assertTrue(latch.await(2, TimeUnit.SECONDS));
        Mockito.verify(mockHandler).handle(tr);
    }

    @Test
    void shouldProcessMultipleTransactions() throws InterruptedException {
        int transactionCount = 5;
        TransactionRequest tr = new TransactionRequest(null, null, null, 0);
        for (int i = 0; i < transactionCount; i++) queue.add(tr);

        CountDownLatch latch = new CountDownLatch(transactionCount);
        Mockito.doAnswer(invocationOnMock -> {
            latch.countDown();
            return null;
        }).when(mockHandler).handle(Mockito.any());

        service.start(1);

        Assertions.assertTrue(latch.await(2, TimeUnit.SECONDS));
        Mockito.verify(mockHandler, Mockito.times(transactionCount)).handle(tr);
    }

    @Test
    void shouldProcessMultipleTransactionWithMultipleProcessors() throws InterruptedException {
        int transactionCount = 20;
        TransactionRequest tr = new TransactionRequest(null, null, null, 0);
        for (int i = 0; i < transactionCount; i++) queue.add(tr);

        CountDownLatch latch = new CountDownLatch(transactionCount);
        Mockito.doAnswer(invocationOnMock -> {
            latch.countDown();
            Thread.sleep(50); //Чтобы один поток не мог все обработать
            return null;
        }).when(mockHandler).handle(Mockito.any());

        service.start(transactionCount);

        Assertions.assertTrue(latch.await(1, TimeUnit.SECONDS));
        Mockito.verify(mockHandler, Mockito.times(transactionCount)).handle(tr);
    }

    @Test
    void shouldStopProcessingAfterShutdown() throws InterruptedException {
        int transactionCount = 30;
        TransactionRequest tr = new TransactionRequest(null, null, null, 0);
        for (int i = 0; i < transactionCount; i++) queue.add(tr);

        CountDownLatch latch = new CountDownLatch(transactionCount);
        CountDownLatch stopLatch = new CountDownLatch(1);
        Mockito.doAnswer(invocationOnMock -> {
            latch.countDown();
            stopLatch.countDown();
            //Thread.sleep(1);
            return null;
        }).when(mockHandler).handle(Mockito.any());

        service.start(1);

        Assertions.assertTrue(stopLatch.await(1,TimeUnit.SECONDS));

        service.shutdown();

        Assertions.assertFalse(latch.await(3, TimeUnit.SECONDS));
        Mockito.verify(mockHandler, Mockito.atMost((int)(transactionCount/1.2))).handle(tr);
    }

    @Test
    void shouldContinueProcessingAfterRestart() throws InterruptedException {
        int transactionCount = 30;
        TransactionRequest tr = new TransactionRequest(null, null, null, 0);
        for (int i = 0; i < transactionCount; i++) queue.add(tr);

        CountDownLatch latch = new CountDownLatch(transactionCount);
        CountDownLatch stopLatch = new CountDownLatch(1);
        Mockito.doAnswer(invocationOnMock -> {
            latch.countDown();
            stopLatch.countDown();
            return null;
        }).when(mockHandler).handle(Mockito.any());

        service.start(1);

        Assertions.assertTrue(stopLatch.await(1,TimeUnit.SECONDS));

        service.shutdown();
        Thread.sleep(500);
        service.start(1);

        Assertions.assertTrue(latch.await(3, TimeUnit.SECONDS));
        Mockito.verify(mockHandler, Mockito.times(transactionCount)).handle(tr);
    }
}

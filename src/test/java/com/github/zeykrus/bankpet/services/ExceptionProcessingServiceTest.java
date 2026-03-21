package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.model.ExceptionRecord;
import com.github.zeykrus.bankpet.model.TransactionRequest;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ExceptionProcessingServiceTest {
    private ExceptionProcessingService service;
    private ExceptionQueue exceptionQueue;
    private ExceptionHandler mockHandler;

    @BeforeEach
    void setUp() {
        exceptionQueue = new ExceptionQueue();
        mockHandler = Mockito.mock(ExceptionHandler.class);
        service = new ExceptionProcessingService(exceptionQueue, mockHandler);
    }

    @AfterEach
    void tearDown() {
        service.shutdown();
    }

    @Test
    void startShouldProcessRecordsAfterStart() throws InterruptedException {
        service.start(1);

        TransactionRequest tr = new TransactionRequest(null, null, null, 0);
        ExceptionRecord exc = new ExceptionRecord(tr,new RuntimeException("Test"));
        CountDownLatch latch = new CountDownLatch(1);
        Mockito.doAnswer(s -> {
            latch.countDown();
            return null;
        }).when(mockHandler).handle(Mockito.any());

        exceptionQueue.add(exc);

        latch.await(1, TimeUnit.SECONDS);
        Mockito.verify(mockHandler).handle(exc);
    }

    @Test
    void shouldProcessMultipleRecords() throws InterruptedException {
        service.start(1);

        int recordsCount = 5;
        CountDownLatch latch = new CountDownLatch(recordsCount);
        Mockito.doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(mockHandler).handle(Mockito.any());

        for (int i = 0; i < recordsCount; i++) {
            TransactionRequest tr = new TransactionRequest(null, null, null, 0);
            ExceptionRecord record = new ExceptionRecord(tr, new RuntimeException("Test" + i));
            exceptionQueue.add(record);
        }

        latch.await(2, TimeUnit.SECONDS);
        Mockito.verify(mockHandler, Mockito.times(recordsCount)).handle(Mockito.any());
    }

    @Test
    void shouldProcessMultipleRecordsWithMultipleThreads() throws InterruptedException {
        service.start(10);

        int recordsCount = 10;
        CountDownLatch latch = new CountDownLatch(recordsCount);
        Mockito.doAnswer(invocationOnMock -> {
            latch.countDown();
            Thread.sleep(100); //Якобы время на обработку запроса
            return null;
        }).when(mockHandler).handle(Mockito.any());

        for (int i = 0; i < recordsCount; i++) {
            TransactionRequest tr = new TransactionRequest(null, null, null, 0);
            ExceptionRecord record = new ExceptionRecord(tr, new RuntimeException("Test" + i));
            exceptionQueue.add(record);
        }

        latch.await(5, TimeUnit.SECONDS);
        Mockito.verify(mockHandler, Mockito.times(recordsCount)).handle(Mockito.any());
    }

    @Test
    void shouldNotProcessAnyAfterStopping() throws InterruptedException {
        service.start(1);
        service.shutdown();

        TransactionRequest tr = new TransactionRequest(null, null, null, 0);
        ExceptionRecord exc = new ExceptionRecord(tr,new RuntimeException("Test"));
        CountDownLatch latch = new CountDownLatch(1);
        Mockito.doAnswer(s -> {
            latch.countDown();
            return null;
        }).when(mockHandler).handle(Mockito.any());

        exceptionQueue.add(exc);

        latch.await(1, TimeUnit.SECONDS);
        Mockito.verify(mockHandler, Mockito.never()).handle(exc);
    }

    @Test
    void shouldFinishAfterInterrupt() throws InterruptedException {
        service.start(1);
        int atMost = 20;
        AtomicInteger call = new AtomicInteger(0);

        CountDownLatch latch = new CountDownLatch(5);
        Mockito.doAnswer(s -> {
            latch.countDown();
            call.incrementAndGet();
            return null;
        }).when(mockHandler).handle(Mockito.any());

        for (int i = 0; i < atMost; i++) {
            TransactionRequest tr = new TransactionRequest(null, null, null, 0);
            ExceptionRecord record = new ExceptionRecord(tr, new RuntimeException("Test" + i));
            exceptionQueue.add(record);
        }

        latch.await(2,TimeUnit.SECONDS);

        service.shutdown();

        Thread.sleep(1000);
        Mockito.verify(mockHandler, Mockito.atMost(atMost)).handle(Mockito.any());
    }

    @Test
    void startingAfterStopping() throws InterruptedException {
        service.start(3);
        int recordsCount = 20;

        CountDownLatch latch = new CountDownLatch(recordsCount / 2);
        CountDownLatch latchFinish = new CountDownLatch(recordsCount);
        Mockito.doAnswer(s -> {
            latch.countDown();
            latchFinish.countDown();
            return null;
        }).when(mockHandler).handle(Mockito.any());

        for (int i = 0; i < recordsCount; i++) {
            TransactionRequest tr = new TransactionRequest(null, null, null, 0);
            ExceptionRecord record = new ExceptionRecord(tr, new RuntimeException("Test" + i));
            exceptionQueue.add(record);
        }

        latch.await(2,TimeUnit.SECONDS);

        service.shutdown();
        Thread.sleep(100);
        service.start(2);

        latchFinish.await(2,TimeUnit.SECONDS);
        Mockito.verify(mockHandler, Mockito.times(recordsCount)).handle(Mockito.any());
    }
}

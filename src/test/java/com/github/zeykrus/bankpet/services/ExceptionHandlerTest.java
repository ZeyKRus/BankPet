package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.model.ExceptionRecord;
import com.github.zeykrus.bankpet.model.TransactionRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ExceptionHandlerTest {
    private ExceptionHandler handler;
    private ExceptionQueue queue;
    private ActionHandler mockActionHandler;

    @BeforeEach
    void setUp() {
        queue = new ExceptionQueue();
        mockActionHandler = Mockito.mock(ActionHandler.class);
        handler = new ExceptionHandler(queue, mockActionHandler);
    }

    @Test
    void shouldGetFromQueue() throws InterruptedException {
        TransactionRequest tr = new TransactionRequest(null, null, null, 0);
        ExceptionRecord exc = new ExceptionRecord(tr, new RuntimeException("Test"));
        queue.add(exc);

        handler.handle(queue.take());

        Assertions.assertEquals(0, queue.size(), "Ожидалась пустая очередь");
    }

    @Test
    void shouldRetryOnFailing() throws Exception {
        TransactionRequest tr = new TransactionRequest(null, null, null, 0);
        ExceptionRecord exc = new ExceptionRecord(tr, new RuntimeException("Test"));
        Mockito.doThrow(new RuntimeException("Retry")).when(mockActionHandler).handleException(tr);
        queue.add(exc);

        handler.handle(queue.take());

        Assertions.assertEquals(1, queue.size(), "Ожидалась заявка в очереди");
        Assertions.assertEquals(1, queue.take().getFailings(), "Ожидалась запись об ошибке, которая уже один раз не смогла повториться");
    }

    @Test
    void shouldMoveToDLQAfterMaximalFailings() throws Exception {
        TransactionRequest tr = new TransactionRequest(null, null, null, 0);
        ExceptionRecord exc = new ExceptionRecord(tr, new RuntimeException("Test"));
        Mockito.doThrow(new RuntimeException("Retry")).when(mockActionHandler).handleException(tr);
        queue.add(exc);

        for (int i = 0; i < ExceptionHandler.MAX_RETRIES; i++) {
            ExceptionRecord curr = queue.take();
            handler.handle(curr);
        }

        Assertions.assertEquals(0, queue.size(), "Ожидалась пустая очередь");
        Assertions.assertEquals(1, handler.getDeadLetterQueue().size(), "Ожидалась одна запись в DLQ");
    }
}

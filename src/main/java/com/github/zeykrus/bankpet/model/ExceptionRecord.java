package com.github.zeykrus.bankpet.model;

public class ExceptionRecord {
    public static final ExceptionRecord POISON;
    private final TransactionRequest req;
    private final Exception exception;
    private int failings;

    static {
        POISON = new ExceptionRecord(null, null);
        POISON.failings = -1;
    }

    public ExceptionRecord(TransactionRequest req, Exception exception)
    {
        this.req = req;
        this.exception = exception;
        this.failings = 0;
    }

    public void incrementFailings() {
        failings++;
    }

    public TransactionRequest getReq() {
        return req;
    }

    public Exception getException() {
        return exception;
    }

    public int getFailings() {
        return failings;
    }

    @Override
    public String toString() {
        return "Транзакция: " + req.toString() + " Исключение: " + exception.toString() + " Попытка: " + failings;
    }
}

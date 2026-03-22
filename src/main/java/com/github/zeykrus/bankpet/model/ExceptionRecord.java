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
        String reqString;
        String excString;
        if (req == null) reqString = "Null"; else reqString = req.toString();
        if (exception == null) excString = "Null"; else excString = exception.toString();
        return "Транзакция: " + reqString + " Исключение: " + excString + " Попытка: " + failings;
    }
}

package main.java.com.github.zeykrus.bankpet.model;

public record ExceptionRecord(TransactionRequest req, Exception exception) {
}

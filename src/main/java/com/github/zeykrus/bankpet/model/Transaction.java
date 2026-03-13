package com.github.zeykrus.bankpet.model;

import com.github.zeykrus.bankpet.account.Account;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

public record Transaction(
        LocalDateTime dateTime,
        Account accFrom,
        Account accTo,
        OperationType operationType,
        double amount,
        boolean success
) implements Comparable<Transaction> {
    public static Transaction fromRequest(LocalDateTime time, TransactionRequest req, boolean success) {
        return new Transaction(
                time,
                req.accFrom(),
                req.accTo(),
                req.operationType(),
                req.amount(),
                success
        );
    }

    @Override
    public int compareTo(@NotNull Transaction o) {
        return this.dateTime.compareTo(o.dateTime());
    }

    public int compareByAmount(@NotNull Transaction o) {
        return Double.compare(o.amount(),this.amount());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Double.compare(amount, that.amount) == 0 && success == that.success && Objects.equals(accTo, that.accTo) && Objects.equals(accFrom, that.accFrom) && Objects.equals(dateTime, that.dateTime) && operationType == that.operationType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dateTime, accFrom, accTo, operationType, amount, success);
    }
}

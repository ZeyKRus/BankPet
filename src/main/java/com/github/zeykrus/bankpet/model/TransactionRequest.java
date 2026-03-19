package com.github.zeykrus.bankpet.model;

import com.github.zeykrus.bankpet.account.Account;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record TransactionRequest(
        Account accFrom,
        Account accTo,
        OperationType operationType,
        long amount
) implements Comparable<TransactionRequest> {

    @Override
    public int compareTo(@NotNull TransactionRequest o) {
        return Double.compare(o.amount(),this.amount());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionRequest that = (TransactionRequest) o;
        return Double.compare(amount, that.amount) == 0 && Objects.equals(accTo, that.accTo) && Objects.equals(accFrom, that.accFrom) && operationType == that.operationType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accFrom, accTo, operationType, amount);
    }
};
package com.github.zeykrus.bankpet.model;

import com.github.zeykrus.bankpet.account.Account;

import java.time.LocalDateTime;
import java.util.Objects;

public class HistoryFilter {
    private final LocalDateTime from;
    private final LocalDateTime to;
    private final Account acc;
    private final OperationType type;

    private HistoryFilter(Builder builder) {
        this.from = builder.from;
        this.to = builder.to;
        this.acc = builder.acc;
        this.type = builder.type;
    }

    public static class Builder {
        private LocalDateTime from;
        private LocalDateTime to;
        private Account acc;
        private OperationType type;

        public Builder() {
            from = null;
            to = null;
            acc = null;
            type = null;
        }

        public Builder from(LocalDateTime from) {
            this.from = from;
            return this;
        }

        public Builder to(LocalDateTime to) {
            this.to = to;
            return this;
        }

        public Builder acc(Account acc) {
            this.acc = acc;
            return this;
        }

        public Builder type(OperationType type) {
            this.type = type;
            return this;
        }

        public HistoryFilter build() {
            return new HistoryFilter(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public LocalDateTime getFrom() {
        return from;
    }

    public LocalDateTime getTo() {
        return to;
    }

    public Account getAcc() {
        return acc;
    }

    public OperationType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistoryFilter that = (HistoryFilter) o;
        return Objects.equals(from, that.from) && Objects.equals(to, that.to) && Objects.equals(acc, that.acc) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, acc, type);
    }
}

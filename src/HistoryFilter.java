import java.time.LocalDateTime;

public class HistoryFilter {
    private final LocalDateTime from;
    private final LocalDateTime to;
    private final BankAccount acc;
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
        private BankAccount acc;
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

        public Builder acc(BankAccount acc) {
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

    public BankAccount getAcc() {
        return acc;
    }

    public OperationType getType() {
        return type;
    }
}

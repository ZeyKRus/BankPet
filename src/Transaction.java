import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public record Transaction(
        LocalDateTime dateTime,
        BankAccount accFrom,
        BankAccount accTo,
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
}

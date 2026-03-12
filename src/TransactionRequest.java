import org.jetbrains.annotations.NotNull;

public record TransactionRequest(
        Account accFrom,
        Account accTo,
        OperationType operationType,
        double amount
) implements Comparable<TransactionRequest> {

    @Override
    public int compareTo(@NotNull TransactionRequest o) {
        return Double.compare(o.amount(),this.amount());
    }
};
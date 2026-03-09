record TransactionRequest(
        BankAccount accFrom,
        BankAccount accTo,
        OperationType operationType,
        double amount
) {};
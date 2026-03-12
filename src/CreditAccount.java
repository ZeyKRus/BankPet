public class CreditAccount extends Account {
    private final double DEFAULT_CREDIT_LIMIT = 1000;
    private double creditLimit;

    public CreditAccount(Bank bankOwner, int number, String owner, double initialBalance) {
        super(bankOwner, number, owner, initialBalance);
        creditLimit = DEFAULT_CREDIT_LIMIT; //Базовый кредитный лимит
    }

    //######################## Создание заявки на транзакцию #############################

    @Override
    public void withdrawRequest(double amount) throws InsufficientFundsException {
        if (amount <= 0) throw new IllegalArgumentException("Сумма снятия средств должна быть больше нуля");
        if (!canWithdraw(amount)) throw new InsufficientFundsException("Сумма снятия средств должна быть не больше, чем сумма имеющихся средств и доступного кредита. Недостаток: " + (amount - (balance + creditLimit)), amount - (balance + creditLimit));

        TransactionRequest req = new TransactionRequest(this, null, OperationType.WITHDRAW, amount);
        sendRequest(req);
    }

    @Override
    public void transferRequest(Account accTo, double amount) throws InsufficientFundsException {
        if (amount <= 0) throw new IllegalArgumentException("Сумма снятия средств должна быть больше нуля");
        if (!canWithdraw(amount)) throw new InsufficientFundsException("Сумма снятия средств должна быть не больше, чем сумма имеющихся средств и доступного кредита. Недостаток: " + (amount - (balance + creditLimit)), amount - (balance + creditLimit));
        if (this == accTo) throw new IllegalArgumentException("Нельзя переводить самому себе");
        if (accTo == null) throw new IllegalArgumentException("Счёт не найден");

        TransactionRequest req = new TransactionRequest(this, accTo, OperationType.TRANSFER, amount);
        sendRequest(req);
    }

    //######################## Действия со средствами #############################

    @Override
    void withdraw(double amount) throws InsufficientFundsException {
        if (amount <= 0) throw new IllegalArgumentException("Сумма снятия средств должна быть больше нуля");
        if (!canWithdraw(amount)) throw new InsufficientFundsException("Сумма снятия средств должна быть не больше, чем сумма имеющихся средств и доступного кредита. Недостаток: " + (amount - (balance + creditLimit)), amount - (balance + creditLimit));
        balance -= amount;
    }

    @Override
    public boolean canWithdraw(double amount) {
        boolean can = false;
        if ((balance + creditLimit) >= amount) can = true;
        return can;
    }

    //######################## Геттеры и сеттеры #############################

    void setCreditLimit(double creditLimit) {
        this.creditLimit = creditLimit;
    }

    public double getCreditLimit() {
        return creditLimit;
    }

}

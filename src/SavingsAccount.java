public class SavingsAccount extends Account {

    public SavingsAccount(Bank bankOwner, int number, String owner, double initialBalance) {
        super(bankOwner, number, owner, initialBalance);
    }

    //######################## Создание заявки на транзакцию #############################

    @Override
    public void withdrawRequest(double amount) throws InsufficientFundsException {
        if (amount <= 0) throw new IllegalArgumentException("Сумма снятия средств должна быть больше нуля");
        if (!canWithdraw(amount)) throw new InsufficientFundsException("Сумма снятия средств должна быть не больше, чем сумма имеющихся средств. Недостаток: " + (amount - balance), amount - balance);

        TransactionRequest req = new TransactionRequest(this, null, OperationType.WITHDRAW, amount);
        sendRequest(req);
    }

    public void transferRequest(Account accTo, double amount) throws InsufficientFundsException {
        if (amount <= 0) throw new IllegalArgumentException("Сумма снятия средств должна быть больше нуля");
        if (!canWithdraw(amount)) throw new InsufficientFundsException("Сумма снятия средств должна быть не больше, чем сумма имеющихся средств. Недостаток: " + (amount - balance), amount - balance);
        if (this == accTo) throw new IllegalArgumentException("Нельзя переводить самому себе");
        if (accTo == null) throw new IllegalArgumentException("Счёт не найден");

        TransactionRequest req = new TransactionRequest(this, accTo, OperationType.TRANSFER, amount);
        sendRequest(req);
    }

    //######################## Действия со средствами #############################

    @Override
    void withdraw(double amount) throws InsufficientFundsException {
        if (amount <= 0) throw new IllegalArgumentException("Сумма снятия средств должна быть больше нуля");
        if (!canWithdraw(amount)) throw new InsufficientFundsException("Сумма снятия средств должна быть не больше, чем сумма имеющихся средств. Недостаток: " + (amount - balance), amount - balance);
        balance -= amount;
    }

    @Override
    public boolean canWithdraw(double amount) {
        boolean can = false;
        if (balance >= amount) can = true;
        return can;
    }

}
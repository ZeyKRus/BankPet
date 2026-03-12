import java.util.List;

public abstract class Account {
    protected final Bank bankOwner;
    protected final int number;
    protected final String owner;
    protected double balance;


    public Account(Bank bankOwner, int number, String owner, double initialBalance) {
        this.number = number;
        this.owner = owner;
        this.bankOwner = bankOwner;
        this.balance = initialBalance;
    }

    //######################## Работа с историей операций #############################

    protected List<Transaction> getHistory() {
        return bankOwner.getLast10(this);
    }

    //######################## Создание заявки на транзакцию #############################

    protected void sendRequest(TransactionRequest req) {
        bankOwner.getNewRequest(req);
    };

    public void depositRequest(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Сумма пополнения счета должна быть больше нуля");

        TransactionRequest req = new TransactionRequest(this, null, OperationType.DEPOSIT, amount);
        sendRequest(req);
    }

    public abstract void transferRequest(Account accTo, double amount) throws InsufficientFundsException;

    public abstract void withdrawRequest(double amount) throws InsufficientFundsException;

    //######################## Действия со средствами #############################

    void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Сумма пополнения счета должна быть больше нуля");
        balance += amount;
    }


    abstract void withdraw(double amount) throws InsufficientFundsException;

    public abstract boolean canWithdraw(double amount);

    //######################## Геттеры и сеттеры #############################

    public double getBalance() {
        return balance;
    }

    public String getOwner() {
        return owner;
    }

    public int getNumber() {
        return number;
    }

    public Bank getBankOwner() {
        return bankOwner;
    }
}

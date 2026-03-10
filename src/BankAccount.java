import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class BankAccount {
    private final int number;
    private final String owner;
    private final Bank bankOwner;

    private double balance;


    public BankAccount(Bank bankOwner, int number, String owner, double initialBalance) {
        this.number = number;
        this.owner = owner;
        this.balance = initialBalance;
        this.bankOwner = bankOwner;
    }

    //######################## Работа с историей операций #############################

    public List<Transaction> getHistory() {
        return bankOwner.getLast10(this);
    }

    //######################## Создание заявки на транзакцию #############################

    private void sendRequest(TransactionRequest req) {
        bankOwner.getNewRequest(req);
    }

    public void depositRequest(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Сумма пополнения счета должна быть больше нуля");

        TransactionRequest req = new TransactionRequest(this, null, OperationType.DEPOSIT, amount);
        sendRequest(req);
    }

    public void withdrawRequest(double amount) throws InsufficientFundsException {
        if (amount <= 0) throw new IllegalArgumentException("Сумма снятия средств должна быть больше нуля");
        if (balance < amount) throw new InsufficientFundsException("Сумма снятия средств должна быть не больше, чем сумма имеющихся средств. Недостаток: "+(amount - balance), amount - balance);

        TransactionRequest req = new TransactionRequest(this, null, OperationType.WITHDRAW, amount);
        sendRequest(req);
    }

    public void transferRequest(BankAccount accTo, double amount) throws InsufficientFundsException {
        if (amount <= 0) throw new IllegalArgumentException("Сумма снятия средств должна быть больше нуля");
        if (balance < amount) throw new InsufficientFundsException("Сумма снятия средств должна быть не больше, чем сумма имеющихся средств. Недостаток: "+(amount - balance), amount - balance);
        if (this == accTo) throw new IllegalArgumentException("Нельзя переводить самому себе");
        if (accTo == null) throw new IllegalArgumentException("Счёт не найден");

        TransactionRequest req = new TransactionRequest(this, accTo, OperationType.TRANSFER, amount);
        sendRequest(req);
    }

    //######################## Действия со средствами #############################

    public void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Сумма пополнения счета должна быть больше нуля");
        balance += amount;
    }

    public void withdraw(double amount) throws InsufficientFundsException {
        if (amount <= 0) throw new IllegalArgumentException("Сумма снятия средств должна быть больше нуля");
        if (balance < amount) throw new InsufficientFundsException("Сумма снятия средств должна быть не больше, чем сумма имеющихся средств. Недостаток: "+(amount - balance), amount - balance);
        balance -= amount;
    }

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
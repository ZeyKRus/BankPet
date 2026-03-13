package main.java.com.github.zeykrus.bankpet.account;

import main.java.com.github.zeykrus.bankpet.model.Bank;
import main.java.com.github.zeykrus.bankpet.exception.InsufficientFundsException;
import main.java.com.github.zeykrus.bankpet.model.Transaction;
import main.java.com.github.zeykrus.bankpet.model.TransactionRequest;
import main.java.com.github.zeykrus.bankpet.model.OperationType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    public List<Transaction> getHistory() {
        return bankOwner.getHistory(this);
    }

    //######################## Создание заявки на транзакцию #############################

    protected void sendRequest(TransactionRequest req) {
        bankOwner.submitRequest(req);
    };

    public void depositRequest(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Сумма пополнения счета должна быть больше нуля");

        TransactionRequest req = new TransactionRequest(this, null, OperationType.DEPOSIT, amount);
        sendRequest(req);
    }

    public void transferRequest(Account accTo, double amount) throws InsufficientFundsException {
        if (amount <= 0) throw new IllegalArgumentException("Сумма снятия средств должна быть больше нуля");
        if (!canWithdraw(amount)) throw new InsufficientFundsException("На счете недостаточно средств",notEnough(amount));
        if (this == accTo) throw new IllegalArgumentException("Нельзя переводить самому себе");
        if (accTo == null) throw new IllegalArgumentException("Счёт не найден");

        TransactionRequest req = new TransactionRequest(this, accTo, OperationType.TRANSFER, amount);
        sendRequest(req);
    }

    public void withdrawRequest(double amount) throws InsufficientFundsException {
        if (amount <= 0) throw new IllegalArgumentException("Сумма снятия средств должна быть больше нуля");
        if (!canWithdraw(amount)) throw new InsufficientFundsException("На счете недостаточно средств",notEnough(amount));

        TransactionRequest req = new TransactionRequest(this, null, OperationType.WITHDRAW, amount);
        sendRequest(req);
    }

    //######################## Действия со средствами #############################

    public void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Сумма пополнения счета должна быть больше нуля");
        balance += amount;
    }

    public void withdraw(double amount) throws InsufficientFundsException {
        if (amount <= 0) throw new IllegalArgumentException("Сумма снятия средств должна быть больше нуля");
        if (!canWithdraw(amount)) throw new InsufficientFundsException("На счете недостаточно средств",notEnough(amount));
        balance -= amount;
    }

    public abstract boolean canWithdraw(double amount);

    public abstract double notEnough(double amount);

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return number == account.number;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(number);
    }
}

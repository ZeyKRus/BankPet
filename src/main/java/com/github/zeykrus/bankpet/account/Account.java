package com.github.zeykrus.bankpet.account;

import com.github.zeykrus.bankpet.services.Bank;
import com.github.zeykrus.bankpet.exception.InsufficientFundsException;
import com.github.zeykrus.bankpet.model.Transaction;
import com.github.zeykrus.bankpet.model.TransactionRequest;
import com.github.zeykrus.bankpet.model.OperationType;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public abstract class Account {
    protected final Bank bankOwner;
    protected final int number;
    protected final String owner;
    protected AtomicLong balance;


    public Account(Bank bankOwner, int number, String owner, long initialBalance) {
        this.number = number;
        this.owner = owner;
        this.bankOwner = bankOwner;
        this.balance = new AtomicLong(initialBalance);
    }

    //######################## Работа с историей операций #############################

    public List<Transaction> getHistory() {
        return bankOwner.getHistory(this);
    }

    //######################## Создание заявки на транзакцию #############################

    protected void sendRequest(TransactionRequest req) {
        bankOwner.submitRequest(req);
    };

    public void depositRequest(long amount) {
        if (amount <= 0) throw new IllegalArgumentException("Сумма пополнения счета должна быть больше нуля");

        TransactionRequest req = new TransactionRequest(this, null, OperationType.DEPOSIT, amount);
        sendRequest(req);
    }

    public void transferRequest(Account accTo, long amount) throws InsufficientFundsException {
        if (amount <= 0) throw new IllegalArgumentException("Сумма снятия средств должна быть больше нуля");
        if (!canWithdraw(amount)) throw new InsufficientFundsException("На счете недостаточно средств",notEnough(amount));
        if (this == accTo) throw new IllegalArgumentException("Нельзя переводить самому себе");
        if (accTo == null) throw new IllegalArgumentException("Счёт не найден");

        TransactionRequest req = new TransactionRequest(this, accTo, OperationType.TRANSFER, amount);
        sendRequest(req);
    }

    public void withdrawRequest(long amount) throws InsufficientFundsException {
        if (amount <= 0) throw new IllegalArgumentException("Сумма снятия средств должна быть больше нуля");
        if (!canWithdraw(amount)) throw new InsufficientFundsException("На счете недостаточно средств",notEnough(amount));

        TransactionRequest req = new TransactionRequest(this, null, OperationType.WITHDRAW, amount);
        sendRequest(req);
    }

    //######################## Действия со средствами #############################

    public void deposit(long amount) {
        if (amount <= 0) throw new IllegalArgumentException("Сумма пополнения счета должна быть больше нуля");
        balance.addAndGet(amount);
    }

    public void withdraw(long amount) throws InsufficientFundsException {
        while(true) {
            long current = balance.get();
            if (amount <= 0) throw new IllegalArgumentException("Сумма снятия средств должна быть больше нуля");
            if (!canWithdraw(amount)) throw new InsufficientFundsException("На счете недостаточно средств",notEnough(amount));

            if(balance.compareAndSet(current, current - amount)) break;
        }
    }

    public abstract boolean canWithdraw(long amount);

    public abstract double notEnough(long amount);

    //######################## Геттеры и сеттеры #############################

    public long getBalance() {
        return balance.get();
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

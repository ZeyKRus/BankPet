package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.FinanceCoreEngine;
import com.github.zeykrus.bankpet.account.Account;
import com.github.zeykrus.bankpet.account.CreditAccount;
import com.github.zeykrus.bankpet.account.InterestBearingAccount;
import com.github.zeykrus.bankpet.account.SavingsAccount;
import com.github.zeykrus.bankpet.model.Transaction;
import com.github.zeykrus.bankpet.model.TransactionRequest;

import java.util.List;

public class Bank {

    private final FinanceCoreEngine core;
    private final AccountManager accountManager;
    private final static String BANK_CODE_PREFIX = "B-";
    private final String name;
    private final int number;

    public Bank(String name, int number, FinanceCoreEngine core) {
        this.core = core;
        this.accountManager = new AccountManager(this);
        this.number = number;
        this.name = name;
    }

    public SavingsAccount createSavingAccount(String ownerUser, long initialBalance) {
        return accountManager.createSavingAccount(ownerUser, initialBalance);
    }

    public InterestBearingAccount createInterestBearingAccount(String ownerUser, long initialBalance) {
        return accountManager.createInterestBearingAccount(ownerUser, initialBalance);
    }

    public CreditAccount createCreditAccount(String ownerUser, long initialBalance) {
        return accountManager.createCreditAccount(ownerUser, initialBalance);
    }

    public void submitRequest(TransactionRequest req) {
        core.newRequest(req);
    }

    //######################## Геттеры и сеттеры #############################

    public String getName() {
        return name;
    }

    public int getNumber() {
        return number;
    }

    public List<Account> getAllAccounts() {
        return accountManager.getAllAccounts();
    }

    public List<Transaction> getHistory(Account acc) {
        return core.getHistory(acc);
    }

    @Override
    public String toString() {
        return "Bank number: "+BANK_CODE_PREFIX+number;
    }
}

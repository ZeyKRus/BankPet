package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.account.InterestBearingAccount;
import com.github.zeykrus.bankpet.model.Bank;
import com.github.zeykrus.bankpet.account.Account;
import com.github.zeykrus.bankpet.account.CreditAccount;
import com.github.zeykrus.bankpet.account.SavingsAccount;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AccountManager {
    private final Bank bank;
    private final Map<Integer, Account> accounts = new HashMap<>();
    private int accountNumber = 0;

    public AccountManager(Bank owner) {
        this.bank = owner;
    }

    public SavingsAccount createSavingAccount(String ownerUser, double initialBalance) {
        SavingsAccount current = new SavingsAccount(this.bank, accountNumber, ownerUser, initialBalance);
        accounts.put(current.getNumber(),current);
        accountNumber++;
        return current;
    }

    public InterestBearingAccount createInterestBearingAccount(String ownerUser, double initialBalance) {
        InterestBearingAccount current = new InterestBearingAccount(this.bank, accountNumber, ownerUser, initialBalance);
        accounts.put(current.getNumber(),current);
        accountNumber++;
        return current;
    }

    public CreditAccount createCreditAccount(String ownerUser, double initialBalance) {
        CreditAccount current = new CreditAccount(this.bank, accountNumber, ownerUser, initialBalance);
        accounts.put(current.getNumber(),current);
        accountNumber++;
        return current;
    }

    public Account getAccount(int accountNumber) {
        return Optional.ofNullable(accounts.get(accountNumber)).orElseThrow(() -> new IllegalArgumentException("Счет не найден"));
    }

    public Optional<Account> findAccount(int accountNumber) {
        return Optional.ofNullable(accounts.get(accountNumber));
    }

    public List<Account> getAllAccounts() {
        return accounts.values().stream().toList();
    }
}

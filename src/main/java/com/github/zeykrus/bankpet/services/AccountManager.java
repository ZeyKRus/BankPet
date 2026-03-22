package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.account.InterestBearingAccount;
import com.github.zeykrus.bankpet.account.Account;
import com.github.zeykrus.bankpet.account.CreditAccount;
import com.github.zeykrus.bankpet.account.SavingsAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class AccountManager {
    private static final Logger log = LoggerFactory.getLogger(AccountManager.class);
    private final Bank bank;
    private final Map<Integer, Account> accounts = new ConcurrentHashMap<>();
    private final AtomicInteger accountNumber = new AtomicInteger(0);

    public AccountManager(Bank owner) {
        this.bank = owner;
        log.info("Сервис инициализирован: {}", this.getClass().getSimpleName());
    }

    public SavingsAccount createSavingAccount(String ownerUser, long initialBalance) {
        SavingsAccount current = new SavingsAccount(this.bank, accountNumber.getAndIncrement(), ownerUser, initialBalance);
        accounts.put(current.getNumber(),current);
        return current;
    }

    public InterestBearingAccount createInterestBearingAccount(String ownerUser, long initialBalance) {
        InterestBearingAccount current = new InterestBearingAccount(this.bank, accountNumber.getAndIncrement(), ownerUser, initialBalance);
        accounts.put(current.getNumber(),current);
        return current;
    }

    public CreditAccount createCreditAccount(String ownerUser, long initialBalance) {
        CreditAccount current = new CreditAccount(this.bank, accountNumber.getAndIncrement(), ownerUser, initialBalance);
        accounts.put(current.getNumber(),current);
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

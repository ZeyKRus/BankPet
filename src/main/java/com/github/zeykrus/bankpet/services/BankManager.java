package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.FinanceCoreEngine;
import com.github.zeykrus.bankpet.account.Account;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class BankManager {
    private final FinanceCoreEngine owner;
    private final Map<Integer, Bank> bankList;
    private final static AtomicInteger counter;

    static {
        counter = new AtomicInteger(0);
    }

    public BankManager(FinanceCoreEngine owner) {
        this.owner = owner;
        this.bankList = new ConcurrentHashMap<>();
    }

    private static int generateBankNumber() {
        return counter.getAndIncrement();
    }

    public Bank generateNewBank(String name) {
        int number = generateBankNumber();
        Bank current = new Bank(name, number, owner);
        bankList.put(number, current);
        return current;
    }

    public Optional<Bank> findBank(int number) {
        return Optional.ofNullable(bankList.get(number));
    }

    public List<Account> getAllAccounts() {
        return bankList.values()
                .stream()
                .map(Bank::getAllAccounts)
                .flatMap(List::stream)
                .toList();
    }
}

package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.FinanceCoreEngine;
import com.github.zeykrus.bankpet.account.Account;

import java.util.*;

public class BankManager {
    private final FinanceCoreEngine owner;
    private final Map<Integer, Bank> bankList;
    private static int counter;

    public BankManager(FinanceCoreEngine owner) {
        this.owner = owner;
        this.bankList = new HashMap<>();
    }

    private static int generateBankNumber() {
        int current = counter;
        counter++;
        return current;
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

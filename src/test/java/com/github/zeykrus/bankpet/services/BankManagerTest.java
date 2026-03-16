package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.FinanceCoreEngine;
import com.github.zeykrus.bankpet.TestConstants;
import com.github.zeykrus.bankpet.account.Account;
import com.github.zeykrus.bankpet.model.Bank;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class BankManagerTest {
    private FinanceCoreEngine core;
    private BankManager bankManager;

    @BeforeEach
    void setUp() {
        core = new FinanceCoreEngine();
        bankManager = new BankManager(core);
    }

    @Test
    void generateNewBankAndFind() {
        Bank bank = bankManager.generateNewBank("Bank1");
        int number = bank.getNumber();

        Assertions.assertEquals(bank, bankManager.findBank(number).get(), "Ожидался тот же самый банк");
    }

    @Test
    void findBankNotExist() {
        Assertions.assertTrue(bankManager.findBank(10).isEmpty());
    }

    @Test
    void getAllAccounts() {
        int amount1 = 10;
        int amount2 = 5;
        Bank bank1 = bankManager.generateNewBank("Bank");
        Bank bank2 = bankManager.generateNewBank("Bank");
        for (int i = 0; i < amount1; i++) {
            bank1.createSavingAccount(TestConstants.PERSON_OWNER, TestConstants.POSITIVE_AMOUNT);
        }
        for (int i = 0; i < amount2; i++) {
            bank2.createSavingAccount(TestConstants.PERSON_OWNER, TestConstants.POSITIVE_AMOUNT);
        }

        List<Account> list = bankManager.getAllAccounts();

        Assertions.assertEquals(amount1 + amount2, list.size());
    }

    @Test
    void numberOfBanks() {
        Bank bank1 = bankManager.generateNewBank("Bank");
        Bank bank2 = bankManager.generateNewBank("Bank");

        Assertions.assertTrue(bank1.getNumber() != bank2.getNumber());
    }
}

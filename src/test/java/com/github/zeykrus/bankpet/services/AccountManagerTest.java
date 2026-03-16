package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.FinanceCoreEngine;
import com.github.zeykrus.bankpet.TestConstants;
import com.github.zeykrus.bankpet.account.Account;
import com.github.zeykrus.bankpet.account.CreditAccount;
import com.github.zeykrus.bankpet.account.InterestBearingAccount;
import com.github.zeykrus.bankpet.account.SavingsAccount;
import com.github.zeykrus.bankpet.model.Bank;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

public class AccountManagerTest {

    private FinanceCoreEngine core;
    private Bank bankOwner;
    private AccountManager accountManager;

    @BeforeEach
    void setUp() {
        core = new FinanceCoreEngine();
        bankOwner = new Bank("Bank1", 0, core);
        accountManager = new AccountManager(bankOwner);
    }

    void createAccount(Account acc) {
        int number = acc.getNumber();

        Assertions.assertEquals(1, accountManager.getAllAccounts().size(), "Ожидался ровно один аккаунт");
        Assertions.assertEquals(acc, accountManager.getAccount(number), "Счет не соответствует счету в менеджере аккаунтов");

        Assertions.assertAll(
                () -> Assertions.assertEquals(TestConstants.BIG_POSITIVE_AMOUNT, acc.getBalance(), "Ожидался другой стартовый баланс у счета"),
                () -> Assertions.assertEquals(TestConstants.PERSON_OWNER, acc.getOwner(), "Ожидался другой владелец у счета"),
                () -> Assertions.assertEquals(bankOwner, acc.getBankOwner(), "Ожидался другой банк-владелец у счета")
        );
    }

    @Test
    void createSavingsAccountTest() {
        SavingsAccount acc = accountManager.createSavingAccount(TestConstants.PERSON_OWNER,TestConstants.BIG_POSITIVE_AMOUNT);
        createAccount(acc);
    }

    @Test
    void createCreditAccountTest() {
        CreditAccount acc = accountManager.createCreditAccount(TestConstants.PERSON_OWNER,TestConstants.BIG_POSITIVE_AMOUNT);
        createAccount(acc);
    }

    @Test
    void createInterestBearingAccountTest() {
        InterestBearingAccount acc = accountManager.createInterestBearingAccount(TestConstants.PERSON_OWNER,TestConstants.BIG_POSITIVE_AMOUNT);
        createAccount(acc);
    }

    @Test
    void findAccountExist() {
        CreditAccount account = accountManager.createCreditAccount(TestConstants.PERSON_OWNER, TestConstants.BIG_POSITIVE_AMOUNT);
        int number = account.getNumber();

        Optional<Account> opt = accountManager.findAccount(number);

        Assertions.assertTrue(opt.isPresent(),"Ожидалось наличие объекта в Optional");
        Assertions.assertEquals(opt.get(),account,"Счета не одинаковые");
    }

    @Test
    void findAccountNotExist() {
        Optional<Account> opt = accountManager.findAccount(0);

        Assertions.assertTrue(opt.isEmpty(),"Ожидался пустой Optional");
    }

    @Test
    void getAccountExist() {
        CreditAccount account = accountManager.createCreditAccount(TestConstants.PERSON_OWNER, TestConstants.BIG_POSITIVE_AMOUNT);
        int number = account.getNumber();

        Assertions.assertEquals(account, accountManager.getAccount(number), "Счета не одинаковые");
    }

    @Test
    void getAccountNotExist() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> accountManager.getAccount(0), "Ожидалась ошибка неверного аргумента");
    }

    @Test
    void getAllAccountsExist() {
        CreditAccount account1 = accountManager.createCreditAccount(TestConstants.PERSON_OWNER, TestConstants.BIG_POSITIVE_AMOUNT);
        CreditAccount account2 = accountManager.createCreditAccount(TestConstants.PERSON_OWNER, TestConstants.BIG_POSITIVE_AMOUNT);

        List<Account> list = accountManager.getAllAccounts();

        Assertions.assertEquals(2, list.size(), "Ожидалось наличие ровно двух счетов");
        Assertions.assertAll(
                () -> Assertions.assertTrue(list.contains(account1), "Ожидалось наличие первого счета в списке"),
                () -> Assertions.assertTrue(list.contains(account2), "Ожидалось наличие второго счета в списке")
        );
    }

}

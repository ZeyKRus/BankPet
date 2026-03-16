package com.github.zeykrus.bankpet.account;

import com.github.zeykrus.bankpet.FinanceCoreEngine;
import com.github.zeykrus.bankpet.TestConstants;
import com.github.zeykrus.bankpet.exception.InsufficientFundsException;
import com.github.zeykrus.bankpet.model.Bank;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class  AccountTest<T extends Account> {

    protected Bank bank;
    protected FinanceCoreEngine core;
    protected T account;

    protected abstract T createAccount();

    @BeforeEach
    void setUp() {
        account = createAccount();
    }

    @Test
    void depositPositive() {
        double oldAmount = account.getBalance();
        double exceptedAmount = oldAmount + TestConstants.POSITIVE_AMOUNT;

        account.deposit(TestConstants.POSITIVE_AMOUNT);

        Assertions.assertEquals(exceptedAmount, account.getBalance(), "Несоответствие ожидаемого баланса и текущего");
    }

    @Test
    void depositZero() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> account.deposit(TestConstants.ZERO_AMOUNT), "Ожидалась ошибка некорректной суммы пополнения средств");
    }

    @Test
    void depositNegative() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> account.deposit(TestConstants.NEGATIVE_AMOUNT), "Ожидалась ошибка некорректной суммы пополнения средств");
    }



    @Test
    void withdrawPositive() {
        double amount = account.getBalance() / 2; //меньше, чем есть
        double expectedAmount = account.getBalance() - amount;

        try {
            account.withdraw(amount);
        } catch (InsufficientFundsException e) {
            throw new RuntimeException(e);
        }

        Assertions.assertEquals(expectedAmount, account.getBalance(), "Несоответствие баланса ожиданиям после снятия средств");
    }

    @Test
    void withdrawZero() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> account.withdraw(TestConstants.ZERO_AMOUNT), "Ожидалась ошибка некорректной суммы снятия средств");
    }

    @Test
    void withdrawNotEnough() {
        double amount = account.getBalance() * 2; //больше, чем есть

        Assertions.assertThrows(InsufficientFundsException.class, () -> account.withdraw(amount), "Ожидалась ошибка нехватки суммы для снятия средств");
    }

    @Test
    void withdrawNegative() {
        double amount = TestConstants.NEGATIVE_AMOUNT; //больше, чем есть

        Assertions.assertThrows(IllegalArgumentException.class, () -> account.withdraw(amount), "Ожидалась ошибка некорректной суммы для снятия средств");
    }
}

package com.github.zeykrus.bankpet.account;

import com.github.zeykrus.bankpet.FinanceCoreEngine;
import com.github.zeykrus.bankpet.TestConstants;
import com.github.zeykrus.bankpet.exception.InsufficientFundsException;
import com.github.zeykrus.bankpet.model.Bank;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SavingsAccountTest extends AccountTest<SavingsAccount> {
    @Override
    protected SavingsAccount createAccount() {
        this.core = new FinanceCoreEngine();
        this.bank = new Bank("Bank1",0,core);
        return bank.createSavingAccount("Owner1",TestConstants.BIG_POSITIVE_AMOUNT);
    }

    @Test
    void canWithdrawPositive() {
        double amount = account.getBalance() / 2; //меньше, чем есть

        Assertions.assertTrue(account.canWithdraw(amount),"Ожидался положительный исход возможности снятия средств");
    }

    @Test
    void canWithdrawNegative() {
        double amount = account.getBalance() * 2; //больше, чем есть

        Assertions.assertFalse(account.canWithdraw(amount),"Ожидался отрицательный исход возможности снятия средств");
    }

    @Test
    void notEnoughWhenEnough() {
        double amount = account.getBalance() / 2; //меньше, чем есть

        Assertions.assertEquals(0, account.notEnough(amount), "Ожидалось, что денег достаточно (не хватает 0)");
    }

    @Test
    void notEnoughPositive() {
        double amount = account.getBalance() + TestConstants.POSITIVE_AMOUNT;

        Assertions.assertEquals(TestConstants.POSITIVE_AMOUNT, account.notEnough(amount), "Ожидалось нехватка конкретной суммы средств");
    }

    @Test
    void constructorTest() {
        String owner = "Owner1";
        double initBalance = TestConstants.POSITIVE_AMOUNT;
        int number = 10;

        SavingsAccount acc = new SavingsAccount(bank, number, owner, initBalance);

        Assertions.assertAll(
                () -> Assertions.assertEquals(bank, acc.getBankOwner(),"Несоответствующий банк"),
                () -> Assertions.assertEquals(number, acc.getNumber(), "Несоответствующий номер"),
                () -> Assertions.assertEquals(initBalance, acc.getBalance(), "Несоответствующий баланс"),
                () -> Assertions.assertEquals(owner, acc.getOwner(), "Несоответствующий владелец")
        );
    }
}

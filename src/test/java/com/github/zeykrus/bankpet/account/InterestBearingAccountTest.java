package com.github.zeykrus.bankpet.account;

import com.github.zeykrus.bankpet.FinanceCoreEngine;
import com.github.zeykrus.bankpet.TestConstants;
import com.github.zeykrus.bankpet.interfaces.InterestBearing;
import com.github.zeykrus.bankpet.services.Bank;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InterestBearingAccountTest extends AccountTest<InterestBearingAccount> {
    @Override
    protected InterestBearingAccount createAccount() {
        this.core = new FinanceCoreEngine();
        this.bank = new Bank("Bank1",0,core);
        return bank.createInterestBearingAccount("Owner1",TestConstants.BIG_POSITIVE_AMOUNT);
    }

    @Test
    void constructorTest() {
        String owner = "Owner1";
        long initBalance = TestConstants.POSITIVE_AMOUNT;
        int number = 10;

        InterestBearingAccount acc = new InterestBearingAccount(bank, number, owner, initBalance);

        Assertions.assertAll(
                () -> Assertions.assertEquals(bank, acc.getBankOwner(),"Несоответствующий банк"),
                () -> Assertions.assertEquals(number, acc.getNumber(), "Несоответствующий номер"),
                () -> Assertions.assertEquals(initBalance, acc.getBalance(), "Несоответствующий баланс"),
                () -> Assertions.assertEquals(owner, acc.getOwner(), "Несоответствующий владелец")
        );
    }

    @Test
    void executeWhenPositive() {
        long expectedAmount = (long) (account.getBalance() + account.getBalance() * InterestBearing.DEFAULT_RATE);

        account.execute();

        Assertions.assertEquals(expectedAmount, account.getBalance(), "Ожидалось изменение баланса на конкретное значение");
    }

    @Test
    void executeWhenNegative() {
        account = new InterestBearingAccount(bank, 10, "Owner1", TestConstants.BIG_NEGATIVE_AMOUNT);

        account.execute();

        Assertions.assertEquals(TestConstants.BIG_NEGATIVE_AMOUNT, account.getBalance(), "Ожидалось, что баланс не изменится");
    }

    @Test
    void executeWhenZero() {
        account = new InterestBearingAccount(bank, 10, "Owner1", TestConstants.ZERO_AMOUNT);

        account.execute();

        Assertions.assertEquals(TestConstants.ZERO_AMOUNT, account.getBalance(), "Ожидалось, что баланс не изменится");
    }
}

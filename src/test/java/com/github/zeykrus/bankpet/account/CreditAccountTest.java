package com.github.zeykrus.bankpet.account;

import com.github.zeykrus.bankpet.FinanceCoreEngine;
import com.github.zeykrus.bankpet.TestConstants;
import com.github.zeykrus.bankpet.interfaces.CreditAllowed;
import com.github.zeykrus.bankpet.services.Bank;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CreditAccountTest extends AccountTest<CreditAccount> {
    @Override
    protected CreditAccount createAccount() {
        this.core = new FinanceCoreEngine();
        this.bank = new Bank("Bank1",0,core);
        return bank.createCreditAccount("Owner1",TestConstants.BIG_POSITIVE_AMOUNT);
    }

    @Test
    void constructorTest() {
        String owner = "Owner1";
        long initBalance = TestConstants.POSITIVE_AMOUNT;
        int number = 10;

        CreditAccount acc = new CreditAccount(bank, number, owner, initBalance);

        Assertions.assertAll(
                () -> Assertions.assertEquals(bank, acc.getBankOwner(),"Несоответствующий банк"),
                () -> Assertions.assertEquals(number, acc.getNumber(), "Несоответствующий номер"),
                () -> Assertions.assertEquals(initBalance, acc.getBalance(), "Несоответствующий баланс"),
                () -> Assertions.assertEquals(owner, acc.getOwner(), "Несоответствующий владелец"),
                () -> Assertions.assertEquals(CreditAllowed.DEFAULT_CREDIT_LIMIT, acc.getCreditLimit(), "Несоответствующий кредитный лимит")
        );
    }

    @Test
    void canWithdrawEnough() {
        Assertions.assertTrue(account.canWithdraw(TestConstants.POSITIVE_AMOUNT), "Ожидалась возможность снятия средств");
    }

    @Test
    void canWithdrawMoreThanBalanceLessThanCredit() {
        long amount = account.getBalance() + (account.getCreditLimit() / 2);

        Assertions.assertTrue(account.canWithdraw(amount), "Ожидалась возможность снятия средств");
    }

    @Test
    void canWithdrawMoreThanCredit() {
        long amount = account.getBalance() + account.getCreditLimit() + TestConstants.POSITIVE_AMOUNT;

        Assertions.assertFalse(account.canWithdraw(amount),"Ожидалась нехватка средств для снятия средств");
    }

    @Test
    void notEnoughWhenEnough() {
        long amount = account.getBalance() / 2;

        Assertions.assertEquals(0, account.notEnough(amount), "Ожидалось, что средств достаточно (не хватает 0)");
    }

    @Test
    void notEnoughMoreThanBalanceLessThanCredit() {
        long amount = account.getBalance() + (account.getCreditLimit() / 2);

        Assertions.assertEquals(0, account.notEnough(amount), "Ожидалось, что средств достаточно (не хватает 0)");
    }

    @Test
    void notEnoughMoreThanCredit() {
        long amount = account.getBalance() + account.getCreditLimit() + TestConstants.POSITIVE_AMOUNT;

        Assertions.assertEquals(TestConstants.POSITIVE_AMOUNT, account.notEnough(amount), "Ожидалась нехватка конкретной суммы средств");
    }

    @Test
    void executeWhenNegative() {
        account = new CreditAccount(bank, 1, "Owner1", TestConstants.BIG_NEGATIVE_AMOUNT);
        double expectedAmount = account.getBalance() + account.getBalance() * CreditAllowed.DEFAULT_CREDIT_PERCENT;

        account.execute();

        Assertions.assertEquals(expectedAmount, account.getBalance(), "Несоответствие суммы после применения процента кредитования");
    }

    @Test
    void executeWhenZero() {
        account = new CreditAccount(bank, 1, "Owner1", TestConstants.ZERO_AMOUNT);

        account.execute();

        Assertions.assertEquals(TestConstants.ZERO_AMOUNT, account.getBalance(), "Ожидалось, что баланс не изменится");
    }


    @Test
    void executeWhenPositive() {
        double oldAmount = account.getBalance();

        account.execute();

        Assertions.assertEquals(oldAmount, account.getBalance(), "Ожидалось, что баланс не изменится");
    }
}

package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.FinanceCoreEngine;
import com.github.zeykrus.bankpet.TestConstants;
import com.github.zeykrus.bankpet.account.Account;
import com.github.zeykrus.bankpet.account.SavingsAccount;
import com.github.zeykrus.bankpet.exception.InsufficientFundsException;
import com.github.zeykrus.bankpet.model.Bank;
import com.github.zeykrus.bankpet.model.HistoryFilter;
import com.github.zeykrus.bankpet.model.OperationType;
import com.github.zeykrus.bankpet.model.TransactionRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ActionHandlerTest {

    private ActionHandler actionHandler;
    private HistoryManager mockHistory;
    private Bank bank;
    private FinanceCoreEngine core;
    private Account accFrom;
    private Account accTo;

    @BeforeEach
    void setUp() {
        mockHistory = Mockito.mock(HistoryManager.class);
        actionHandler = new ActionHandler(mockHistory);
        core = new FinanceCoreEngine();
        bank = new Bank("Bank1",0,core);
        accFrom = new SavingsAccount(bank,0, TestConstants.PERSON_OWNER,TestConstants.BIG_POSITIVE_AMOUNT);
        accTo = new SavingsAccount(bank,1, TestConstants.PERSON_OWNER,TestConstants.BIG_POSITIVE_AMOUNT);
    }

    @Test
    void depositPositive() {
        TransactionRequest req = new TransactionRequest(accFrom,null, OperationType.DEPOSIT,TestConstants.POSITIVE_AMOUNT);
        double oldAmount = accFrom.getBalance();
        double exceptedAmount = oldAmount + TestConstants.POSITIVE_AMOUNT;

        actionHandler.deposit(req);

        Assertions.assertEquals(exceptedAmount, accFrom.getBalance(), "Баланс счета не соответствует ожиданиям после пополнения");
        Mockito.verify(mockHistory).addToHistory(req,true);
    }

    @Test
    void depositNegative() {
        TransactionRequest req = new TransactionRequest(accFrom,null, OperationType.DEPOSIT,TestConstants.NEGATIVE_AMOUNT);

        Assertions.assertThrows(IllegalArgumentException.class, () -> actionHandler.deposit(req),"Ожидалась ошибка неверного аргумента");
        Mockito.verify(mockHistory).addToHistory(req,false);
    }

    @Test
    void depositZero() {
        TransactionRequest req = new TransactionRequest(accFrom,null, OperationType.DEPOSIT,TestConstants.ZERO_AMOUNT);

        Assertions.assertThrows(IllegalArgumentException.class, () -> actionHandler.deposit(req),"Ожидалась ошибка неверного аргумента");
        Mockito.verify(mockHistory).addToHistory(req,false);
    }

    @Test
    void withdrawPositiveEnough() {
        TransactionRequest req = new TransactionRequest(accFrom,null, OperationType.WITHDRAW,TestConstants.POSITIVE_AMOUNT);
        double oldAmount = accFrom.getBalance();
        double exceptedAmount = oldAmount - TestConstants.POSITIVE_AMOUNT;

        try {
            actionHandler.withdraw(req);
        } catch (InsufficientFundsException e) {
            throw new RuntimeException(e); //Никогда не случится, объявлено, чтобы избежать аннотации throws
        }

        Assertions.assertEquals(exceptedAmount, accFrom.getBalance(),"Не соответствует ожидаемому балансу");
        Mockito.verify(mockHistory).addToHistory(req,true);
    }

    @Test
    void withdrawPositiveNotEnough() {
        TransactionRequest req = new TransactionRequest(accFrom,null, OperationType.WITHDRAW,TestConstants.BIG_POSITIVE_AMOUNT*2);
        double oldAmount = accFrom.getBalance();

        Assertions.assertAll(
                () -> Assertions.assertThrows(InsufficientFundsException.class, () -> actionHandler.withdraw(req)),
                () -> Assertions.assertEquals(oldAmount, accFrom.getBalance(),"Баланс не должен был измениться")
        );
        Mockito.verify(mockHistory).addToHistory(req,false);
    }

    @Test
    void withdrawZero() {
        TransactionRequest req = new TransactionRequest(accFrom,null, OperationType.WITHDRAW,TestConstants.ZERO_AMOUNT);
        double oldAmount = accFrom.getBalance();

        Assertions.assertAll(
                () -> Assertions.assertThrows(IllegalArgumentException.class, () -> actionHandler.withdraw(req)),
                () -> Assertions.assertEquals(oldAmount, accFrom.getBalance(),"Баланс не должен был измениться")
        );
        Mockito.verify(mockHistory).addToHistory(req,false);
    }

    @Test
    void withdrawNegative() {
        TransactionRequest req = new TransactionRequest(accFrom,null, OperationType.WITHDRAW,TestConstants.NEGATIVE_AMOUNT);
        double oldAmount = accFrom.getBalance();

        Assertions.assertAll(
                () -> Assertions.assertThrows(IllegalArgumentException.class, () -> actionHandler.withdraw(req)),
                () -> Assertions.assertEquals(oldAmount, accFrom.getBalance(),"Баланс не должен был измениться")
        );
        Mockito.verify(mockHistory).addToHistory(req,false);
    }

    @Test
    void transferPositiveEnough() {
        TransactionRequest req = new TransactionRequest(accFrom,accTo, OperationType.TRANSFER,TestConstants.POSITIVE_AMOUNT);
        double exceptedAmountFrom = accFrom.getBalance() - TestConstants.POSITIVE_AMOUNT;
        double exceptedAmountTo = accTo.getBalance() + TestConstants.POSITIVE_AMOUNT;

        try {
            actionHandler.transfer(req);
        } catch (InsufficientFundsException e) {
            throw new RuntimeException(e); //Никогда не случится, объявлено, чтобы избежать аннотации throws
        }

        Assertions.assertAll(
                () -> Assertions.assertEquals(exceptedAmountFrom, accFrom.getBalance()),
                () -> Assertions.assertEquals(exceptedAmountTo, accTo.getBalance())
        );
        Mockito.verify(mockHistory).addToHistory(req,true);
    }

    @Test
    void transferPositiveNotEnough() {
        TransactionRequest req = new TransactionRequest(accFrom,accTo, OperationType.TRANSFER,TestConstants.BIG_POSITIVE_AMOUNT*2);
        double exceptedAmountFrom = accFrom.getBalance();
        double exceptedAmountTo = accTo.getBalance();

        Assertions.assertAll(
                () -> Assertions.assertThrows(InsufficientFundsException.class, () -> actionHandler.transfer(req)),
                () -> Assertions.assertEquals(exceptedAmountFrom, accFrom.getBalance()),
                () -> Assertions.assertEquals(exceptedAmountTo, accTo.getBalance())
        );
        Mockito.verify(mockHistory).addToHistory(req,false);
    }

    @Test
    void transferZero() {
        TransactionRequest req = new TransactionRequest(accFrom,accTo, OperationType.TRANSFER,TestConstants.ZERO_AMOUNT);
        double exceptedAmountFrom = accFrom.getBalance();
        double exceptedAmountTo = accTo.getBalance();

        Assertions.assertAll(
                () -> Assertions.assertThrows(IllegalArgumentException.class, () -> actionHandler.transfer(req)),
                () -> Assertions.assertEquals(exceptedAmountFrom, accFrom.getBalance()),
                () -> Assertions.assertEquals(exceptedAmountTo, accTo.getBalance())
        );
        Mockito.verify(mockHistory).addToHistory(req,false);
    }

    @Test
    void transferNegative() {
        TransactionRequest req = new TransactionRequest(accFrom,accTo, OperationType.TRANSFER,TestConstants.NEGATIVE_AMOUNT);
        double exceptedAmountFrom = accFrom.getBalance();
        double exceptedAmountTo = accTo.getBalance();

        Assertions.assertAll(
                () -> Assertions.assertThrows(IllegalArgumentException.class, () -> actionHandler.transfer(req)),
                () -> Assertions.assertEquals(exceptedAmountFrom, accFrom.getBalance()),
                () -> Assertions.assertEquals(exceptedAmountTo, accTo.getBalance())
        );
        Mockito.verify(mockHistory).addToHistory(req,false);
    }

    @Test
    void transferItself() {
        TransactionRequest req = new TransactionRequest(accFrom,accFrom, OperationType.TRANSFER,TestConstants.POSITIVE_AMOUNT);
        double exceptedAmountFrom = accFrom.getBalance();

        Assertions.assertAll(
                () -> Assertions.assertThrows(IllegalArgumentException.class, () -> actionHandler.transfer(req)),
                () -> Assertions.assertEquals(exceptedAmountFrom, accFrom.getBalance())
        );
        Mockito.verify(mockHistory).addToHistory(req,false);
    }

    @Test
    void getHistoryTest() {
        actionHandler.getHistory(accFrom);

        Mockito.verify(mockHistory).getHistory(HistoryFilter.builder().acc(accFrom).build());
    }

    @Test
    void handleTestPositive() {
        TransactionRequest trReq = new TransactionRequest(accFrom,accTo, OperationType.TRANSFER,TestConstants.POSITIVE_AMOUNT);
        TransactionRequest depReq = new TransactionRequest(accFrom,null, OperationType.DEPOSIT,TestConstants.POSITIVE_AMOUNT);
        TransactionRequest wdReq = new TransactionRequest(accFrom,null, OperationType.WITHDRAW,TestConstants.POSITIVE_AMOUNT);

        try {
            actionHandler.handle(trReq);
            actionHandler.handle(depReq);
            actionHandler.handle(wdReq);
        } catch (Exception e) {
            throw new RuntimeException(e); //Заглушка, никогда не случится
        }

        Mockito.verify(mockHistory).addToHistory(trReq,true);
        Mockito.verify(mockHistory).addToHistory(depReq,true);
        Mockito.verify(mockHistory).addToHistory(wdReq,true);
    }

}

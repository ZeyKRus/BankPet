package com.github.zeykrus.bankpet;

import com.github.zeykrus.bankpet.account.Account;
import com.github.zeykrus.bankpet.account.CreditAccount;
import com.github.zeykrus.bankpet.account.InterestBearingAccount;
import com.github.zeykrus.bankpet.account.SavingsAccount;
import com.github.zeykrus.bankpet.services.Bank;
import com.github.zeykrus.bankpet.model.ExceptionRecord;
import com.github.zeykrus.bankpet.model.OperationType;
import com.github.zeykrus.bankpet.model.TransactionRequest;
import com.github.zeykrus.bankpet.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;


public class FinanceCoreEngineTest {
    private FinanceCoreEngine core;
    private QueueManager queueMock;
    private BankManager bankMock;
    private ActionHandler actionMock;
    private ExceptionQueue exceptionQueueMock;
    private ExceptionHandler exceptionHandlerMock;

    @BeforeEach
    void setUp() {
        queueMock = Mockito.mock(QueueManager.class);
        bankMock = Mockito.mock(BankManager.class);
        actionMock = Mockito.mock(ActionHandler.class);
        exceptionHandlerMock = Mockito.mock(ExceptionHandler.class);
        exceptionQueueMock = Mockito.mock(ExceptionQueue.class);

        core = new FinanceCoreEngine(bankMock, actionMock, queueMock, exceptionQueueMock, exceptionHandlerMock);

    }

    @Test
    void newRequestSubmit() {
        Bank bank = new Bank("Bank1", 0, core);
        Account accFrom = new SavingsAccount(bank, 0, TestConstants.PERSON_OWNER, TestConstants.BIG_POSITIVE_AMOUNT);
        Account accTo = new SavingsAccount(bank, 0, TestConstants.PERSON_OWNER, TestConstants.BIG_POSITIVE_AMOUNT);
        TransactionRequest tr = new TransactionRequest(accFrom, accTo, OperationType.TRANSFER, TestConstants.POSITIVE_AMOUNT);

        core.newRequest(tr);

        Mockito.verify(queueMock).add(tr);
    }

//    @Test
//    void handleRequestFromQueueExist() throws Exception {
//        Bank bank = new Bank("Bank1", 0, core);
//        Account accFrom = new SavingsAccount(bank, 0, TestConstants.PERSON_OWNER, TestConstants.BIG_POSITIVE_AMOUNT);
//        Account accTo = new SavingsAccount(bank, 0, TestConstants.PERSON_OWNER, TestConstants.BIG_POSITIVE_AMOUNT);
//        TransactionRequest tr = new TransactionRequest(accFrom, accTo, OperationType.TRANSFER, TestConstants.POSITIVE_AMOUNT);
//        Mockito.when(queueMock.poll()).thenReturn(Optional.of(tr));
//
//        core.handleRequestFromQueue();
//
//        Mockito.verify(queueMock).poll();
//        Mockito.verify(actionMock).handle(tr);
//    }

//    @Test
//    void handleRequestFromQueueNotExist() throws Exception {
//        Bank bank = new Bank("Bank1", 0, core);
//        Account accFrom = new SavingsAccount(bank, 0, TestConstants.PERSON_OWNER, TestConstants.BIG_POSITIVE_AMOUNT);
//        Account accTo = new SavingsAccount(bank, 0, TestConstants.PERSON_OWNER, TestConstants.BIG_POSITIVE_AMOUNT);
//        TransactionRequest tr = new TransactionRequest(accFrom, accTo, OperationType.TRANSFER, TestConstants.POSITIVE_AMOUNT);
//        Mockito.when(queueMock.poll()).thenReturn(Optional.empty());
//
//        core.handleRequestFromQueue();
//
//        Mockito.verify(queueMock).poll();
//        Mockito.verify(actionMock, Mockito.times(0)).handle(tr);
//    }

//    @Test
//    void handleExceptionFromQueueExist() {
//        Bank bank = new Bank("Bank1", 0, core);
//        Account accFrom = new SavingsAccount(bank, 0, TestConstants.PERSON_OWNER, TestConstants.BIG_POSITIVE_AMOUNT);
//        Account accTo = new SavingsAccount(bank, 0, TestConstants.PERSON_OWNER, TestConstants.BIG_POSITIVE_AMOUNT);
//        TransactionRequest tr = new TransactionRequest(accFrom, accTo, OperationType.TRANSFER, TestConstants.POSITIVE_AMOUNT);
//        ExceptionRecord exc = new ExceptionRecord(tr, new IllegalArgumentException());
//        Mockito.when(exceptionQueueMock.poll()).thenReturn(Optional.of(exc));
//
//        core.exceptionHandle();
//
//        Mockito.verify(exceptionQueueMock).poll();
//        Mockito.verify(exceptionHandlerMock).handle(exc);
//    }

//    @Test
//    void handleExceptionFromQueueNotExist() {
//        Bank bank = new Bank("Bank1", 0, core);
//        Account accFrom = new SavingsAccount(bank, 0, TestConstants.PERSON_OWNER, TestConstants.BIG_POSITIVE_AMOUNT);
//        Account accTo = new SavingsAccount(bank, 0, TestConstants.PERSON_OWNER, TestConstants.BIG_POSITIVE_AMOUNT);
//        TransactionRequest tr = new TransactionRequest(accFrom, accTo, OperationType.TRANSFER, TestConstants.POSITIVE_AMOUNT);
//        ExceptionRecord exc = new ExceptionRecord(tr, new IllegalArgumentException());
//        Mockito.when(exceptionQueueMock.poll()).thenReturn(Optional.empty());
//
//        core.exceptionHandle();
//
//        Mockito.verify(exceptionQueueMock).poll();
//        Mockito.verify(exceptionHandlerMock, Mockito.times(0)).handle(exc);
//    }

    @Test
    void executeAllSubmitTest() {
        Bank bank = new Bank("Bank1", 0, core);
        List<Account> accountList = new LinkedList<>();
        SavingsAccount savAcc = Mockito.spy(new SavingsAccount(bank,0,TestConstants.PERSON_OWNER,TestConstants.BIG_POSITIVE_AMOUNT));
        CreditAccount creditAcc = Mockito.spy(new CreditAccount(bank,0,TestConstants.PERSON_OWNER,TestConstants.BIG_POSITIVE_AMOUNT));
        InterestBearingAccount intAcc = Mockito.spy(new InterestBearingAccount(bank,0,TestConstants.PERSON_OWNER,TestConstants.BIG_POSITIVE_AMOUNT));
        accountList.add(savAcc);
        accountList.add(intAcc);
        accountList.add(creditAcc);
        accountList.add(intAcc);
        accountList.add(savAcc);
        accountList.add(creditAcc);
        accountList.add(creditAcc);
        Mockito.when(bankMock.getAllAccounts()).thenReturn(accountList);

        core.executeAll();

        Mockito.verify(intAcc, Mockito.times(2)).execute();
        Mockito.verify(creditAcc, Mockito.times(3)).execute();
    }

    @Test
    void getHistorySubmitTest() {
        Bank bank = new Bank("Bank1", 0, core);
        SavingsAccount savAcc = new SavingsAccount(bank,0,TestConstants.PERSON_OWNER,TestConstants.BIG_POSITIVE_AMOUNT);

        core.getHistory(savAcc);

        Mockito.verify(actionMock).getHistory(savAcc);
    }
}

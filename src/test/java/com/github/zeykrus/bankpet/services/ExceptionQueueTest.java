package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.FinanceCoreEngine;
import com.github.zeykrus.bankpet.TestConstants;
import com.github.zeykrus.bankpet.account.Account;
import com.github.zeykrus.bankpet.account.SavingsAccount;
import com.github.zeykrus.bankpet.model.ExceptionRecord;
import com.github.zeykrus.bankpet.model.OperationType;
import com.github.zeykrus.bankpet.model.TransactionRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExceptionQueueTest {
    private ExceptionQueue exceptionQueue;
    private Bank bank;
    private FinanceCoreEngine core;

    @BeforeEach
    void setUp() {
        exceptionQueue = new ExceptionQueue();
        core = new FinanceCoreEngine();
        bank = new Bank("Bank1",0,core);
    }

    @Test
    void addNewTransactionRequest() throws InterruptedException {
        Account accFrom = new SavingsAccount(bank,0, TestConstants.PERSON_OWNER,TestConstants.BIG_POSITIVE_AMOUNT);
        Account accTo = new SavingsAccount(bank,1, TestConstants.PERSON_OWNER,TestConstants.BIG_POSITIVE_AMOUNT);
        TransactionRequest tr = new TransactionRequest(accFrom, accTo, OperationType.TRANSFER, TestConstants.POSITIVE_AMOUNT);
        ExceptionRecord exc = new ExceptionRecord(tr, new IllegalArgumentException());

        exceptionQueue.add(exc);

        Assertions.assertEquals(exc, exceptionQueue.take(), "Ожидался тот же самый объект в очереди ошибок");
    }

    @Test
    void sizeTest() {
        Account accFrom = new SavingsAccount(bank,0, TestConstants.PERSON_OWNER,TestConstants.BIG_POSITIVE_AMOUNT);
        Account accTo = new SavingsAccount(bank,1, TestConstants.PERSON_OWNER,TestConstants.BIG_POSITIVE_AMOUNT);
        TransactionRequest tr = new TransactionRequest(accFrom, accTo, OperationType.TRANSFER, TestConstants.POSITIVE_AMOUNT);
        ExceptionRecord exc = new ExceptionRecord(tr, new IllegalArgumentException());
        int amount = 12;

        for (int i = 0; i < amount; i++) {
            exceptionQueue.add(exc);
        }

        Assertions.assertEquals(amount, exceptionQueue.size(), "Несоответствие длины очереди количеству помещенных запросов");
    }
}

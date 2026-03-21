package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.FinanceCoreEngine;
import com.github.zeykrus.bankpet.TestConstants;
import com.github.zeykrus.bankpet.account.Account;
import com.github.zeykrus.bankpet.account.SavingsAccount;
import com.github.zeykrus.bankpet.model.OperationType;
import com.github.zeykrus.bankpet.model.TransactionRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class QueueManagerTest {
    private QueueManager queueManager;
    private Bank bank;
    private FinanceCoreEngine core;

    @BeforeEach
    void setUp() {
        queueManager = new QueueManager();
        core = new FinanceCoreEngine();
        bank = new Bank("Bank1",0,core);
    }

    @Test
    void addNewTransactionRequest() throws InterruptedException {
        Account accFrom = new SavingsAccount(bank,0, TestConstants.PERSON_OWNER,TestConstants.BIG_POSITIVE_AMOUNT);
        Account accTo = new SavingsAccount(bank,1, TestConstants.PERSON_OWNER,TestConstants.BIG_POSITIVE_AMOUNT);
        TransactionRequest tr = new TransactionRequest(accFrom, accTo, OperationType.TRANSFER, TestConstants.POSITIVE_AMOUNT);

        queueManager.add(tr);

        Assertions.assertEquals(tr, queueManager.take(), "Ожидался тот же самый объект в очереди");
    }

    @Test
    void priorityTest() throws InterruptedException {
        Account accFrom = new SavingsAccount(bank,0, TestConstants.PERSON_OWNER,TestConstants.BIG_POSITIVE_AMOUNT);
        Account accTo = new SavingsAccount(bank,1, TestConstants.PERSON_OWNER,TestConstants.BIG_POSITIVE_AMOUNT);
        TransactionRequest tr = new TransactionRequest(accFrom, accTo, OperationType.TRANSFER, TestConstants.POSITIVE_AMOUNT);
        TransactionRequest trMustBeFirst = new TransactionRequest(accFrom, accTo, OperationType.TRANSFER, TestConstants.POSITIVE_AMOUNT * 2);

        queueManager.add(tr);
        queueManager.add(tr);
        queueManager.add(tr);
        queueManager.add(trMustBeFirst);
        queueManager.add(tr);
        queueManager.add(tr);

        Assertions.assertEquals(trMustBeFirst, queueManager.take(), "Ожидался объект с самой большой суммой транзакции");
    }

    @Test
    void sizeTest() {
        Account accFrom = new SavingsAccount(bank,0, TestConstants.PERSON_OWNER,TestConstants.BIG_POSITIVE_AMOUNT);
        Account accTo = new SavingsAccount(bank,1, TestConstants.PERSON_OWNER,TestConstants.BIG_POSITIVE_AMOUNT);
        TransactionRequest tr = new TransactionRequest(accFrom, accTo, OperationType.TRANSFER, TestConstants.POSITIVE_AMOUNT);
        int amount = 12;

        for (int i = 0; i < amount; i++) {
            queueManager.add(tr);
        }

        Assertions.assertEquals(amount, queueManager.size(), "Несоответствие длины очереди количеству помещенных запросов");
    }
}

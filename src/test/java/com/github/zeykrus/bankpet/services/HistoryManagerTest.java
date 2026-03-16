package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.FinanceCoreEngine;
import com.github.zeykrus.bankpet.TestConstants;
import com.github.zeykrus.bankpet.account.Account;
import com.github.zeykrus.bankpet.account.SavingsAccount;
import com.github.zeykrus.bankpet.model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class HistoryManagerTest {
    private HistoryManager historyManager;
    private FinanceCoreEngine core;
    private Bank bank;
    private Account accFrom;
    private Account accTo;

    private Long amountOfSuccessful;
    private Long amountOfDeposites;
    private Long amountOfTransfers;
    private Long amountOfWithdraws;
    private Long amountOfAccFrom;
    private Long amountOfAccTo;
    private Long amountOfTransactions;
    private double depositAmount;

    @BeforeEach
    void setUp() {
        historyManager = new HistoryManager();
        core = new FinanceCoreEngine();
        bank = new Bank("Bank1", 0, core);
        accFrom = new SavingsAccount(bank,0, TestConstants.PERSON_OWNER,TestConstants.BIG_POSITIVE_AMOUNT);
        accTo = new SavingsAccount(bank,1, TestConstants.PERSON_OWNER,TestConstants.BIG_POSITIVE_AMOUNT);
    }

    @Test
    void addToHistorySuccess() {
        TransactionRequest transactionRequest = new TransactionRequest(accFrom,accTo, OperationType.TRANSFER,TestConstants.POSITIVE_AMOUNT);

        historyManager.addToHistory(transactionRequest,true);

        List<Transaction> list = historyManager.getHistory(HistoryFilter.builder().build());
        Assertions.assertEquals(1,list.size(),"Ожидалось ровно одна операция в истории");
        Transaction tr = list.getFirst();
        Assertions.assertAll(
                () -> Assertions.assertEquals(tr.operationType(),transactionRequest.operationType(),"Операции отличаются"),
                () -> Assertions.assertEquals(tr.accFrom(), transactionRequest.accFrom(),"Счета, с которых осуществлялся перевод, отличаются"),
                () -> Assertions.assertEquals(tr.accTo(), transactionRequest.accTo(), "Счета, на которые совершается перевод, отличаются"),
                () -> Assertions.assertEquals(tr.amount(), transactionRequest.amount(), "Сумма перевода отличаются"),
                () -> Assertions.assertTrue(tr.success(), "Ожидалась успешная запись о завершении операции")
        );
    }

    @Test
    void addToHistoryNotSuccess() {
        TransactionRequest transactionRequest = new TransactionRequest(accFrom,accTo, OperationType.TRANSFER,TestConstants.POSITIVE_AMOUNT);

        historyManager.addToHistory(transactionRequest,false);

        List<Transaction> list = historyManager.getHistory(HistoryFilter.builder().build());
        Assertions.assertEquals(1,list.size(),"Ожидалось ровно одна операция в истории");
        Transaction tr = list.getFirst();
        Assertions.assertAll(
                () -> Assertions.assertEquals(tr.operationType(),transactionRequest.operationType(),"Операции отличаются"),
                () -> Assertions.assertEquals(tr.accFrom(), transactionRequest.accFrom(),"Счета, с которых осуществлялся перевод, отличаются"),
                () -> Assertions.assertEquals(tr.accTo(), transactionRequest.accTo(), "Счета, на которые совершается перевод, отличаются"),
                () -> Assertions.assertEquals(tr.amount(), transactionRequest.amount(), "Сумма перевода отличаются"),
                () -> Assertions.assertFalse(tr.success(), "Ожидалась безуспешная запись о завершении операции")
        );
    }

    void historyInitForTests() {
        amountOfSuccessful = 4L;
        amountOfAccFrom = 4L;
        amountOfAccTo = 3L;
        amountOfDeposites = 2L;
        amountOfTransfers = 2L;
        amountOfWithdraws = 1L;
        amountOfTransactions = 5L;
        depositAmount = 400;

        historyManager.addToHistory(LocalDateTime.of(2026,3,1,10,20), new TransactionRequest(accFrom, null, OperationType.DEPOSIT, 100),true);
        historyManager.addToHistory(LocalDateTime.of(2026,3,2,10,20), new TransactionRequest(accFrom, null, OperationType.DEPOSIT, 300),true);
        historyManager.addToHistory(LocalDateTime.of(2026,3,4,9, 0), new TransactionRequest(accFrom, accTo, OperationType.TRANSFER, 140),true);
        historyManager.addToHistory(LocalDateTime.of(2026,3,4,10,20), new TransactionRequest(accTo, null, OperationType.WITHDRAW, 100),true);
        historyManager.addToHistory(LocalDateTime.of(2026,3,4,11,20), new TransactionRequest(accTo, accFrom, OperationType.TRANSFER, 100),false);
    }

    @Test
    void getHistoryAll() {
        historyInitForTests();
        HistoryFilter filter = HistoryFilter.builder().build(); //Пустой фильтр

        Assertions.assertEquals(amountOfTransactions, historyManager.getHistory(filter).size(), "Ожидается ровно 5 записей в истории");
    }

    @Test
    void getHistoryNoHistory() {
        HistoryFilter filter = HistoryFilter.builder().build(); //Пустой фильтр

        Assertions.assertTrue(historyManager.getHistory(filter).isEmpty(), "Ожидается пустая история");
    }

    @Test
    void getHistoryWithFilterAccount() {
        historyInitForTests();
        HistoryFilter filter = HistoryFilter.builder().acc(accTo).build(); //Только те операции, где есть AccTo (таких 3)

        Assertions.assertEquals(amountOfAccTo, historyManager.getHistory(filter).size(), "Ожидалось ровно 3 записи в истории после фильтрации");
    }

    @Test
    void getHistoryDateFilter() {
        historyInitForTests();
        LocalDateTime from = LocalDateTime.of(2026,3,1,10,20);
        LocalDateTime to = LocalDateTime.of(2026,3,4,10,0);
        HistoryFilter filter = HistoryFilter.builder().from(from).to(to).build(); //Входит 3 события

        Assertions.assertEquals(3, historyManager.getHistory(filter).size(), "Ожидалось ровно 3 записи в истории после фильтрации");
    }

    @Test
    void getStatisticTest() {
        historyInitForTests();
        Map<OperationType, Long> stat = historyManager.getStatistic();

        Assertions.assertAll(
                () -> Assertions.assertEquals(3, stat.size(), "Ожидается 3 ключа в мапе"),
                () -> Assertions.assertEquals(2, stat.get(OperationType.DEPOSIT), "Ожидалось 2 успешных пополнения счета"),
                () -> Assertions.assertEquals(1, stat.get(OperationType.TRANSFER), "Ожидался 1 успешный перевод средств"),
                () -> Assertions.assertEquals(1, stat.get(OperationType.WITHDRAW), "Ожидался 1 успешное списание средств")
        );
    }

    @Test
    void getCountByTypeExist() {
        historyInitForTests();

        Assertions.assertAll(
                () -> Assertions.assertEquals(amountOfDeposites,historyManager.getCountByType(OperationType.DEPOSIT)),
                () -> Assertions.assertEquals(amountOfWithdraws,historyManager.getCountByType(OperationType.WITHDRAW)),
                () -> Assertions.assertEquals(amountOfTransfers - 1,historyManager.getCountByType(OperationType.TRANSFER)) //-1 потому что одна операция false
        );
    }

    @Test
    void getCountByTypeNotExist() {
        Assertions.assertEquals(0, historyManager.getCountByType(OperationType.DEPOSIT),"Ожидалось 0 объектов");
    }

    @Test
    void getSumByTypeExist() {
        historyInitForTests();

        Assertions.assertEquals(depositAmount, historyManager.getSumByType(OperationType.DEPOSIT),"Ожидалась сумма в 400 по депозитам");
    }

    @Test
    void getSumByTypeNotExist() {
        Assertions.assertEquals(0,historyManager.getSumByType(OperationType.TRANSFER),"Ожидался 0 при отсутствии операций");
    }

    @Test
    void getTopTransactionsExist() {
        historyInitForTests();
        List<Transaction> list = historyManager.getTopTransactions(OperationType.DEPOSIT,Integer.MAX_VALUE);
        Transaction trDep = list.getFirst();
        Transaction trTran = historyManager.getTopTransactions(OperationType.TRANSFER,1).getFirst();

        Assertions.assertEquals(300, trDep.amount());
        Assertions.assertEquals(140, trTran.amount());
        Assertions.assertEquals(amountOfDeposites,list.size(),"Несоответствие количества депозитов в истории с количеством заявленных");
    }

    @Test
    void getTopTransactionsNotExist() {
        List<Transaction> list = historyManager.getTopTransactions(OperationType.TRANSFER,Integer.MAX_VALUE);

        Assertions.assertEquals(0, list.size(),"Ожидался пустой список");
    }

}

package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.account.Account;
import com.github.zeykrus.bankpet.exception.IllegalAccountException;
import com.github.zeykrus.bankpet.exception.IllegalTransactionRequestException;
import com.github.zeykrus.bankpet.exception.InsufficientFundsException;
import com.github.zeykrus.bankpet.interfaces.PeriodicOperation;
import com.github.zeykrus.bankpet.interfaces.ThrowingConsumer;
import com.github.zeykrus.bankpet.model.HistoryFilter;
import com.github.zeykrus.bankpet.model.OperationType;
import com.github.zeykrus.bankpet.model.Transaction;
import com.github.zeykrus.bankpet.model.TransactionRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ActionHandler {

    private final Map<OperationType, ThrowingConsumer<TransactionRequest>> requestHandler = new HashMap<>();
    private final HistoryManager history;

    public ActionHandler(HistoryManager historyManager) {
        requestHandler.put(OperationType.DEPOSIT, this::deposit);
        requestHandler.put(OperationType.WITHDRAW, this::withdraw);
        requestHandler.put(OperationType.TRANSFER, this::transfer);
        this.history = historyManager;
    }

    public void transfer(TransactionRequest req) throws InsufficientFundsException {
        Account accFrom = req.accFrom();
        Account accTo = req.accTo();
        long amount = req.amount();

        try {
            if (accFrom == null) throw new IllegalArgumentException("Счета списания не существует");
            if (accTo == null) throw new IllegalArgumentException("Счета пополнения не существует");
            if (accFrom == accTo) throw new IllegalArgumentException("Счета списания и счет пополнения один и тот же");
            if (amount <= 0) throw new IllegalArgumentException("Некорректная сумма перевода");
            if (!accFrom.canWithdraw(amount)) throw new InsufficientFundsException("На счете недостаточно средств для перевода",accFrom.notEnough(amount));

            accFrom.withdraw(amount);
            accTo.deposit(amount);
        } catch (Exception e) {
            history.addToHistory(req,false);
            throw e;
        }

        history.addToHistory(req,true);
    }

    public void deposit(TransactionRequest req) {
        Account acc = req.accFrom();
        long amount = req.amount();

        try {
            if (acc == null) throw new IllegalArgumentException("Счета не существует");
            if (amount <= 0) throw new IllegalArgumentException("Некорректная сумма пополнения");

            acc.deposit(amount);
        } catch (Exception e) {
            history.addToHistory(req,false);
            throw e;
        }

        history.addToHistory(req,true);
    }

    public void withdraw(TransactionRequest req) throws InsufficientFundsException {
        Account acc = req.accFrom();
        long amount = req.amount();

        try {
            if (acc == null) throw new IllegalArgumentException("Счета не существует");
            if (amount <= 0) throw new IllegalArgumentException("Некорректная сумма списания");

            acc.withdraw(amount);
        } catch (Exception e) {
            history.addToHistory(req,false);
            throw e;
        }

        history.addToHistory(req,true);
    }

    public List<Transaction> getHistory(Account acc) {
        return history.getHistory(HistoryFilter.builder().acc(acc).build());
    }


    public void handle(TransactionRequest req) throws Exception {
        if (req == null) throw new IllegalTransactionRequestException("Некорректный запрос на транзакцию");
        requestHandler.get(req.operationType()).accept(req);
    }
}

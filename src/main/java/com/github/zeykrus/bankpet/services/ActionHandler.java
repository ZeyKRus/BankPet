package main.java.com.github.zeykrus.bankpet.services;

import main.java.com.github.zeykrus.bankpet.account.Account;
import main.java.com.github.zeykrus.bankpet.interfaces.PeriodicOperation;
import main.java.com.github.zeykrus.bankpet.model.HistoryFilter;
import main.java.com.github.zeykrus.bankpet.model.OperationType;
import main.java.com.github.zeykrus.bankpet.model.Transaction;
import main.java.com.github.zeykrus.bankpet.model.TransactionRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ActionHandler {

    private final Map<OperationType, Consumer<TransactionRequest>> requestHandler = new HashMap<>();
    private final HistoryManager history = new HistoryManager();

    public ActionHandler() {
        requestHandler.put(OperationType.DEPOSIT, this::deposit);
        requestHandler.put(OperationType.WITHDRAW, this::withdraw);
        requestHandler.put(OperationType.TRANSFER, this::transfer);
        requestHandler.put(OperationType.HISTORY_CHECK, this::historyCheck);
    }

    public void transfer(TransactionRequest req) {
        Account accFrom = req.accFrom();
        Account accTo = req.accTo();
        double amount = req.amount();

        try {
            if (accFrom == null) throw new IllegalArgumentException("Счета списания не существует");
            if (accTo == null) throw new IllegalArgumentException("Счета пополнения не существует");
            if (amount <= 0) throw new IllegalArgumentException("Некорректная сумма перевода");
            if (!accFrom.canWithdraw(amount)) throw new IllegalArgumentException("На счете недостаточно средств для перевода");

            accFrom.withdraw(amount);
            accTo.deposit(amount);
        } catch (Exception e) {
            history.addToHistory(req,false);
        }

        history.addToHistory(req,true);
    }

    public void deposit(TransactionRequest req) {
        Account acc = req.accFrom();
        double amount = req.amount();

        try {
            if (acc == null) throw new IllegalArgumentException("Счета не существует");
            if (amount <= 0) throw new IllegalArgumentException("Некорректная сумма пополнения");

            acc.deposit(amount);
        } catch (Exception e) {
            history.addToHistory(req,false);
        }

        history.addToHistory(req,true);
    }

    public void withdraw(TransactionRequest req) {
        Account acc = req.accFrom();
        double amount = req.amount();

        try {
            if (acc == null) throw new IllegalArgumentException("Счета не существует");
            if (amount <= 0) throw new IllegalArgumentException("Некорректная сумма списания");
            if (!acc.canWithdraw(amount)) throw new IllegalArgumentException("На счете недостаточно средств для перевода");

            acc.withdraw(amount);
        } catch (Exception e) {
            history.addToHistory(req,false);
        }

        history.addToHistory(req,true);
    }

    public void historyCheck(TransactionRequest req) {
        List<Transaction> current = history.getHistory(HistoryFilter.builder().acc(req.accFrom()).build());
        req.accFrom().applyHistory(current);
    }



    public void handle(TransactionRequest req) {
        requestHandler.get(req.operationType()).accept(req);
    }
}

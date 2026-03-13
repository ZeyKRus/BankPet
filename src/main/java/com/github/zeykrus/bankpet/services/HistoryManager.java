package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.account.Account;
import com.github.zeykrus.bankpet.model.HistoryFilter;
import com.github.zeykrus.bankpet.model.OperationType;
import com.github.zeykrus.bankpet.model.Transaction;
import com.github.zeykrus.bankpet.model.TransactionRequest;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HistoryManager {
    private final TreeMap<LocalDateTime, ArrayList<Transaction>> history = new TreeMap<>();

    //######################## Обработка статистики #############################

    private Stream<Transaction> successTransactions() {
        return history.values().stream()
                .flatMap(ArrayList::stream)
                .filter(Transaction::success);
    }

    public Map<OperationType, Long> getStatistic() {
        return successTransactions()
                .collect(Collectors.groupingBy(Transaction::operationType,Collectors.counting()));

    }

    public long getCountByType(OperationType type) {
        return successTransactions()
                .filter(t -> t.operationType() == type)
                .count();
    }

    public double getSumByType(OperationType type) {
        return successTransactions()
                .filter(t -> t.operationType() == type)
                .collect(Collectors.summingDouble(Transaction::amount));
    }

    public List<Transaction> getTopTransactions(OperationType type, int n) {
        if (n <= 0) return List.of();
        return successTransactions()
                .filter(t -> t.operationType() == type)
                .sorted(Comparator.comparingDouble(Transaction::amount).reversed())
                .limit(n)
                .collect(Collectors.toList());
    }

    //######################## Обработка истории #############################

    public void addToHistory(TransactionRequest req, boolean success) {
        LocalDateTime time = LocalDateTime.now();
        ArrayList<Transaction> list = history.get(time);
        if (list == null) {
            history.put(time, new ArrayList<>());
        }
        history.get(time).add(Transaction.fromRequest(time,req,success));
    }

    public List<Transaction> getHistory(HistoryFilter filter) {
        if (history.isEmpty()) return new ArrayList<>();
        if (filter == null) filter = HistoryFilter.builder().build();

        LocalDateTime start = filter.getFrom();
        LocalDateTime finish = filter.getTo();
        Account acc = filter.getAcc();
        OperationType type = filter.getType();

        if (start != null && finish != null && start.isAfter(finish)) return new ArrayList<>();

        if (start == null) start = history.firstKey();
        if (finish == null) finish = history.lastKey();

        return history.subMap(start,finish).values().stream()
                .flatMap(ArrayList::stream)
                .filter(t -> acc == null || t.accFrom() == acc || t.accTo() == acc)
                .filter(t -> type == null || t.operationType() == type)
                .toList();
    }

    public List<Transaction> getLast10(Account acc) {
        List<Transaction> list = getHistory(HistoryFilter.builder().acc(acc).build());
        list = list.reversed();
        list = list.subList(0,Math.min(10,list.size()));
        return list;
    }

}

package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.account.Account;
import com.github.zeykrus.bankpet.model.HistoryFilter;
import com.github.zeykrus.bankpet.model.OperationType;
import com.github.zeykrus.bankpet.model.Transaction;
import com.github.zeykrus.bankpet.model.TransactionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HistoryManager {
    private static final Logger log = LoggerFactory.getLogger(HistoryManager.class);
    private final TreeMap<LocalDateTime, ArrayList<Transaction>> history = new TreeMap<>();

    public HistoryManager() {
        log.info("Сервис инициализирован: {}", this.getClass().getSimpleName());
    }

    //######################## Обработка статистики #############################

    private Stream<Transaction> successTransactions() {
        return history.values().stream()
                .flatMap(ArrayList::stream)
                .filter(Transaction::success);
    }

    synchronized public Map<OperationType, Long> getStatistic() {
        log.trace("Запрошена статистика");
        return successTransactions()
                .collect(Collectors.groupingBy(Transaction::operationType,Collectors.counting()));

    }

    synchronized public long getCountByType(OperationType type) {
        log.trace("Запрошена статистика по типу операции");
        return successTransactions()
                .filter(t -> t.operationType() == type)
                .count();
    }

    synchronized public double getSumByType(OperationType type) {
        log.trace("Запрошена статистика по суммам операций");
        return successTransactions()
                .filter(t -> t.operationType() == type)
                .collect(Collectors.summingDouble(Transaction::amount));
    }

    synchronized public List<Transaction> getTopTransactions(OperationType type, int n) {
        log.trace("Запрошена статистика по топовым транзакциям");
        if (n <= 0) return List.of();
        return successTransactions()
                .filter(t -> t.operationType() == type)
                .sorted(Comparator.comparingDouble(Transaction::amount).reversed())
                .limit(n)
                .collect(Collectors.toList());
    }

    //######################## Обработка истории #############################

    synchronized public void addToHistory(TransactionRequest req, boolean success) {
        log.trace("Помещение в историю: {}, статус: {}",req,success);
        LocalDateTime time = LocalDateTime.now();
        ArrayList<Transaction> list = history.get(time);
        if (list == null) {
            history.put(time, new ArrayList<>());
        }
        history.get(time).add(Transaction.fromRequest(time,req,success));
    }

    synchronized void addToHistory(LocalDateTime dateTime, TransactionRequest req, boolean success) {
        log.trace("Помещение в историю со временем: {}, статус: {}, время: {}",req,success,dateTime);
        ArrayList<Transaction> list = history.get(dateTime);
        if (list == null) {
            history.put(dateTime, new ArrayList<>());
        }
        history.get(dateTime).add(Transaction.fromRequest(dateTime,req,success));
    }

    synchronized public List<Transaction> getHistory(HistoryFilter filter) {
        log.trace("Запрошена история по фильтру: {}",filter);
        if (history.isEmpty()) return new ArrayList<>();
        if (filter == null) filter = HistoryFilter.builder().build();

        LocalDateTime start = filter.getFrom();
        LocalDateTime finish = filter.getTo();
        Account acc = filter.getAcc();
        OperationType type = filter.getType();

        if (start != null && finish != null && start.isAfter(finish)) return new ArrayList<>();

        if (start == null) start = history.firstKey();
        if (finish == null) finish = history.lastKey().plusNanos(10);

        return history.subMap(start,finish).values().stream()
                .flatMap(ArrayList::stream)
                .filter(t -> acc == null || t.accFrom() == acc || t.accTo() == acc)
                .filter(t -> type == null || t.operationType() == type)
                .toList();
    }

}

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

/**
 * Управляет историей операций и предоставляет статистику.
 * <p>
 * Этот класс хранит все выполненные транзакции в {@link TreeMap}, где ключом является
 * время совершения операции, а значением — список транзакций, выполненных в этот момент.
 * 
 *
 * <p>
 * Основные возможности:
 * <ul>
 *   <li>сохранение операций в историю ({@link #addToHistory(TransactionRequest, boolean)})</li>
 *   <li>получение истории с фильтрацией по счёту, типу операции и диапазону дат</li>
 *   <li>статистика по успешным операциям (количество, сумма, топ-транзакции)</li>
 * </ul>
 * 
 *
 * <p>
 * Все публичные методы синхронизированы ({@code synchronized}), что делает класс
 * потокобезопасным, но может стать узким местом при высокой нагрузке.
 * 
 *
 * @see Transaction
 * @see HistoryFilter
 */
public class HistoryManager {
    private static final Logger log = LoggerFactory.getLogger(HistoryManager.class);

    /**
     * Хранилище истории операций.
     * <p>
     * Ключ — время выполнения операции (с точностью до наносекунд).
     * Значение — список транзакций, выполненных в этот момент.
     * 
     */
    private final TreeMap<LocalDateTime, ArrayList<Transaction>> history = new TreeMap<>();

    /**
     * Создаёт менеджер истории.
     */
    public HistoryManager() {
        log.info("Сервис инициализирован: {}", this.getClass().getSimpleName());
    }

    //######################## Обработка статистики #############################

    /**
     * Возвращает поток успешных транзакций (без учёта фильтров).
     *
     * @return поток успешных транзакций
     */
    private Stream<Transaction> successTransactions() {
        return history.values().stream()
                .flatMap(ArrayList::stream)
                .filter(Transaction::success);
    }

    /**
     * Возвращает статистику по количеству успешных операций каждого типа.
     *
     * @return мапа, где ключ — тип операции, значение — количество успешных операций
     */
    synchronized public Map<OperationType, Long> getStatistic() {
        log.trace("Запрошена статистика");
        return successTransactions()
                .collect(Collectors.groupingBy(Transaction::operationType,Collectors.counting()));

    }

    /**
     * Возвращает количество успешных операций указанного типа.
     *
     * @param type тип операции
     * @return количество успешных операций
     */
    synchronized public long getCountByType(OperationType type) {
        log.trace("Запрошена статистика по типу операции");
        return successTransactions()
                .filter(t -> t.operationType() == type)
                .count();
    }

    /**
     * Возвращает сумму всех успешных операций указанного типа.
     *
     * @param type тип операции
     * @return сумма успешных операций
     */
    synchronized public double getSumByType(OperationType type) {
        log.trace("Запрошена статистика по суммам операций");
        return successTransactions()
                .filter(t -> t.operationType() == type)
                .collect(Collectors.summingDouble(Transaction::amount));
    }

    /**
     * Возвращает топ-N успешных операций указанного типа по сумме.
     *
     * @param type тип операции
     * @param n    количество записей (если n less 0, возвращается пустой список)
     * @return список топ-транзакций (от большей суммы к меньшей)
     */
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

    /**
     * Добавляет запись в историю с текущим временем.
     *
     * @param req     запрос на операцию
     * @param success успешность выполнения
     */
    synchronized public void addToHistory(TransactionRequest req, boolean success) {
        log.trace("Помещение в историю: {}, статус: {}",req,success);
        LocalDateTime time = LocalDateTime.now();
        ArrayList<Transaction> list = history.get(time);
        if (list == null) {
            history.put(time, new ArrayList<>());
        }
        history.get(time).add(Transaction.fromRequest(time,req,success));
    }

    /**
     * Добавляет запись в историю с указанным временем (для тестов).
     *
     * @param dateTime время выполнения операции
     * @param req      запрос на операцию
     * @param success  успешность выполнения
     */
    synchronized void addToHistory(LocalDateTime dateTime, TransactionRequest req, boolean success) {
        log.trace("Помещение в историю со временем: {}, статус: {}, время: {}",req,success,dateTime);
        ArrayList<Transaction> list = history.get(dateTime);
        if (list == null) {
            history.put(dateTime, new ArrayList<>());
        }
        history.get(dateTime).add(Transaction.fromRequest(dateTime,req,success));
    }

    /**
     * Возвращает историю операций с применением фильтра.
     * <p>
     * Фильтр может содержать:
     * <ul>
     *   <li>диапазон дат ({@link HistoryFilter#getFrom()} и {@link HistoryFilter#getTo()})</li>
     *   <li>конкретный счёт ({@link HistoryFilter#getAcc()})</li>
     *   <li>тип операции ({@link HistoryFilter#getType()})</li>
     * </ul>
     * Если фильтр не задан (null), возвращается вся история.
     * 
     *
     * @param filter фильтр для выборки (может быть null)
     * @return список транзакций, соответствующих фильтру (может быть пустым)
     */
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

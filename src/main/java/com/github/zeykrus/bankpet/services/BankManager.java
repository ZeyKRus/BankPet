package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.FinanceCoreEngine;
import com.github.zeykrus.bankpet.account.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Управляет банками в системе.
 * <p>
 * Этот класс отвечает за:
 * <ul>
 *   <li>генерацию уникальных номеров банков с помощью {@link AtomicInteger}</li>
 *   <li>создание новых банков через {@link #generateNewBank(String)}</li>
 *   <li>хранение банков в потокобезопасной коллекции {@link ConcurrentHashMap}</li>
 *   <li>поиск банков по номеру (с возвратом {@link Optional})</li>
 *   <li>агрегацию всех счетов из всех банков для выполнения периодических операций</li>
 * </ul>
 * 
 *
 * <p>
 * Все операции потокобезопасны и могут выполняться одновременно из разных потоков.
 * 
 *
 * <p>
 * Каждый банк при создании автоматически получает уникальный номер,
 * который инкрементируется атомарно через {@link #generateBankNumber()}.
 * Номер банка используется в строковом представлении (например, "B-0").
 * 
 *
 * @see Bank
 * @see FinanceCoreEngine
 */
public class BankManager {
    private static final Logger log = LoggerFactory.getLogger(BankManager.class);
    private final FinanceCoreEngine owner;
    private final Map<Integer, Bank> bankList;
    private static final AtomicInteger counter;

    static {
        counter = new AtomicInteger(0);
    }

    /**
     * Создаёт менеджер банков.
     *
     * @param owner ссылка на ядро системы (не может быть null)
     */
    public BankManager(FinanceCoreEngine owner) {
        this.owner = owner;
        this.bankList = new ConcurrentHashMap<>();
        log.info("Сервис инициализирован: {}", this.getClass().getSimpleName());
    }

    /**
     * Генерирует следующий уникальный номер для банка.
     * <p>
     * Номера генерируются последовательно, начиная с 0.
     * 
     *
     * @return следующий номер банка
     */
    private static int generateBankNumber() {
        return counter.getAndIncrement();
    }

    /**
     * Создаёт новый банк с указанным названием.
     * <p>
     * Банку автоматически присваивается уникальный номер, который генерируется
     * атомарно. Созданный банк сохраняется во внутреннем хранилище.
     * 
     *
     * @param name название банка
     * @return созданный банк
     */
    public Bank generateNewBank(String name) {
        int number = generateBankNumber();
        Bank current = new Bank(name, number, owner);
        bankList.put(number, current);
        return current;
    }

    /**
     * Находит банк по номеру, возвращая {@link Optional}.
     * <p>
     * Если банк с указанным номером не найден, возвращается пустой {@link Optional}.
     * 
     *
     * @param number номер банка
     * @return {@link Optional}, содержащий банк, или пустой {@link Optional}, если банк не найден
     */
    public Optional<Bank> findBank(int number) {
        return Optional.ofNullable(bankList.get(number));
    }

    /**
     * Возвращает список всех счетов из всех банков в системе.
     * <p>
     * Этот метод используется для выполнения периодических операций
     * (например, начисления процентов) на всех счетах системы.
     * 
     *
     * @return список всех счетов (может быть пустым)
     */
    public List<Account> getAllAccounts() {
        return bankList.values()
                .stream()
                .map(Bank::getAllAccounts)
                .flatMap(List::stream)
                .toList();
    }
}

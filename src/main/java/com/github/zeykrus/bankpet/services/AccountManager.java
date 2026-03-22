package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.account.InterestBearingAccount;
import com.github.zeykrus.bankpet.account.Account;
import com.github.zeykrus.bankpet.account.CreditAccount;
import com.github.zeykrus.bankpet.account.SavingsAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Управляет счетами внутри одного банка.
 * <p>
 * Этот класс отвечает за:
 * <ul>
 *   <li>создание различных типов счетов ({@link SavingsAccount}, {@link CreditAccount}, {@link InterestBearingAccount})</li>
 *   <li>хранение счетов в потокобезопасной коллекции {@link ConcurrentHashMap}</li>
 *   <li>генерацию уникальных номеров счетов с помощью {@link AtomicInteger}</li>
 *   <li>поиск счетов по номеру (с проверкой существования или возвратом {@link Optional})</li>
 * </ul>
 * 
 *
 * <p>
 * Все операции потокобезопасны и могут выполняться одновременно из разных потоков.
 * 
 *
 * <p>
 * Каждый счёт при создании автоматически получает уникальный номер,
 * который инкрементируется атомарно.
 * 
 *
 * @see Account
 * @see SavingsAccount
 * @see CreditAccount
 * @see InterestBearingAccount
 */
public class AccountManager {
    private static final Logger log = LoggerFactory.getLogger(AccountManager.class);
    private final Bank bank;
    private final Map<Integer, Account> accounts = new ConcurrentHashMap<>();
    private final AtomicInteger accountNumber = new AtomicInteger(0);

    /**
     * Создаёт менеджер счетов для указанного банка.
     *
     * @param owner банк-владелец (не может быть null)
     */
    public AccountManager(Bank owner) {
        this.bank = owner;
        log.info("Сервис инициализирован: {}", this.getClass().getSimpleName());
    }

    /**
     * Создаёт обычный сберегательный счёт.
     *
     * @param ownerUser      имя владельца счёта
     * @param initialBalance начальный баланс (в копейках/центах)
     * @return созданный сберегательный счёт
     */
    public SavingsAccount createSavingAccount(String ownerUser, long initialBalance) {
        SavingsAccount current = new SavingsAccount(this.bank, accountNumber.getAndIncrement(), ownerUser, initialBalance);
        accounts.put(current.getNumber(),current);
        return current;
    }

    /**
     * Создаёт процентный сберегательный счёт.
     * <p>
     * На такой счёт периодически начисляются проценты (через {@link InterestBearingAccount#execute()}).
     * 
     *
     * @param ownerUser      имя владельца счёта
     * @param initialBalance начальный баланс (в копейках/центах)
     * @return созданный процентный счёт
     */
    public InterestBearingAccount createInterestBearingAccount(String ownerUser, long initialBalance) {
        InterestBearingAccount current = new InterestBearingAccount(this.bank, accountNumber.getAndIncrement(), ownerUser, initialBalance);
        accounts.put(current.getNumber(),current);
        return current;
    }

    /**
     * Создаёт кредитный счёт.
     * <p>
     * Кредитный счёт имеет кредитный лимит ({@link CreditAccount#DEFAULT_CREDIT_LIMIT}),
     * позволяет уходить в минус и начисляет проценты при отрицательном балансе.
     * 
     *
     * @param ownerUser      имя владельца счёта
     * @param initialBalance начальный баланс (может быть отрицательным, если используется кредитный лимит)
     * @return созданный кредитный счёт
     */
    public CreditAccount createCreditAccount(String ownerUser, long initialBalance) {
        CreditAccount current = new CreditAccount(this.bank, accountNumber.getAndIncrement(), ownerUser, initialBalance);
        accounts.put(current.getNumber(),current);
        return current;
    }

    /**
     * Возвращает счёт по номеру.
     * <p>
     * Если счёт с указанным номером не найден, кидает исключение.
     * 
     *
     * @param accountNumber номер счёта
     * @return счёт
     * @throws IllegalArgumentException если счёт с таким номером не существует
     */
    public Account getAccount(int accountNumber) {
        return Optional.ofNullable(accounts.get(accountNumber)).orElseThrow(() -> new IllegalArgumentException("Счет не найден"));
    }

    /**
     * Находит счёт по номеру, возвращая {@link Optional}.
     * <p>
     * В отличие от {@link #getAccount(int)}, не кидает исключение, если счёт не найден.
     * 
     *
     * @param accountNumber номер счёта
     * @return {@link Optional}, содержащий счёт, или пустой {@link Optional}, если счёт не найден
     */
    public Optional<Account> findAccount(int accountNumber) {
        return Optional.ofNullable(accounts.get(accountNumber));
    }

    /**
     * Возвращает список всех счетов, зарегистрированных в этом менеджере.
     *
     * @return неизменяемый список счетов (может быть пустым)
     */
    public List<Account> getAllAccounts() {
        return accounts.values().stream().toList();
    }
}

package com.github.zeykrus.bankpet.services;

import com.github.zeykrus.bankpet.FinanceCoreEngine;
import com.github.zeykrus.bankpet.account.Account;
import com.github.zeykrus.bankpet.account.CreditAccount;
import com.github.zeykrus.bankpet.account.InterestBearingAccount;
import com.github.zeykrus.bankpet.account.SavingsAccount;
import com.github.zeykrus.bankpet.model.Transaction;
import com.github.zeykrus.bankpet.model.TransactionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Представляет банк в системе.
 * <p>
 * Класс является фасадом для работы со счетами и операциями:
 * <ul>
 *   <li>создание счетов различных типов (через {@link AccountManager})</li>
 *   <li>отправка запросов на выполнение операций в ядро системы ({@link FinanceCoreEngine})</li>
 *   <li>получение истории операций по счёту</li>
 * </ul>
 * 
 *
 * <p>
 * Каждый банк имеет уникальный номер, который генерируется {@link BankManager}.
 * Номер банка используется в строковом представлении (например, "B-0").
 * 
 *
 * <p>
 * Сам банк не содержит бизнес-логики, а делегирует её:
 * <ul>
 *   <li>{@link AccountManager} — управление счетами</li>
 *   <li>{@link FinanceCoreEngine} — обработку операций и историю</li>
 * </ul>
 * 
 *
 * @see AccountManager
 * @see FinanceCoreEngine
 */
public class Bank {
    private static final Logger log = LoggerFactory.getLogger(Bank.class);
    private final FinanceCoreEngine core;
    private final AccountManager accountManager;
    private static final String BANK_CODE_PREFIX = "B-";
    private final String name;
    private final int number;

    /**
     * Создаёт новый банк.
     *
     * @param name   название банка (для отображения)
     * @param number уникальный номер банка (генерируется {@link BankManager})
     * @param core   ссылка на ядро системы (не может быть null)
     */
    public Bank(String name, int number, FinanceCoreEngine core) {
        this.core = core;
        this.accountManager = new AccountManager(this);
        this.number = number;
        this.name = name;
        log.info("Создание нового банка: {}", this);
    }

    /**
     * Создаёт обычный сберегательный счёт.
     *
     * @param ownerUser      имя владельца счёта
     * @param initialBalance начальный баланс (в копейках/центах)
     * @return созданный сберегательный счёт
     * @see AccountManager#createSavingAccount(String, long)
     */
    public SavingsAccount createSavingAccount(String ownerUser, long initialBalance) {
        return accountManager.createSavingAccount(ownerUser, initialBalance);
    }

    /**
     * Создаёт процентный сберегательный счёт.
     * <p>
     * На такой счёт периодически начисляются проценты через {@link InterestBearingAccount#execute()}.
     * 
     *
     * @param ownerUser      имя владельца счёта
     * @param initialBalance начальный баланс (в копейках/центах)
     * @return созданный процентный счёт
     * @see AccountManager#createInterestBearingAccount(String, long)
     */
    public InterestBearingAccount createInterestBearingAccount(String ownerUser, long initialBalance) {
        return accountManager.createInterestBearingAccount(ownerUser, initialBalance);
    }

    /**
     * Создаёт кредитный счёт.
     * <p>
     * Кредитный счёт имеет кредитный лимит и может уходить в минус.
     * При отрицательном балансе начисляются проценты через {@link CreditAccount#execute()}.
     * 
     *
     * @param ownerUser      имя владельца счёта
     * @param initialBalance начальный баланс (может быть отрицательным)
     * @return созданный кредитный счёт
     * @see AccountManager#createCreditAccount(String, long)
     */
    public CreditAccount createCreditAccount(String ownerUser, long initialBalance) {
        return accountManager.createCreditAccount(ownerUser, initialBalance);
    }

    /**
     * Отправляет запрос на выполнение операции.
     * <p>
     * Запрос будет помещён в очередь и обработан асинхронно.
     * 
     *
     * @param req запрос на операцию (пополнение, снятие, перевод)
     * @see FinanceCoreEngine#newRequest(TransactionRequest)
     */
    public void submitRequest(TransactionRequest req) {
        core.newRequest(req);
    }

    //######################## Геттеры и сеттеры #############################

    public String getName() {
        return name;
    }

    public int getNumber() {
        return number;
    }

    /**
     * Возвращает список всех счетов в этом банке.
     *
     * @return список счетов (может быть пустым)
     */
    public List<Account> getAllAccounts() {
        return accountManager.getAllAccounts();
    }

    /**
     * Возвращает историю операций для указанного счёта.
     *
     * @param acc счёт
     * @return список транзакций (может быть пустым)
     * @see FinanceCoreEngine#getHistory(Account)
     */
    public List<Transaction> getHistory(Account acc) {
        return core.getHistory(acc);
    }

    /**
     * Возвращает строковое представление банка.
     * <p>
     * Формат: "B-{номер банка}"
     * 
     *
     * @return строковое представление, например "B-0"
     */
    @Override
    public String toString() {
        return BANK_CODE_PREFIX+number;
    }
}

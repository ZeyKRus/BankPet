package com.github.zeykrus.bankpet.account;

import com.github.zeykrus.bankpet.services.Bank;
import com.github.zeykrus.bankpet.interfaces.CreditAllowed;

/**
 * Кредитный банковский счёт.
 * <p>
 * Кредитный счёт позволяет уходить в минус до определённого лимита ({@link #creditLimit}).
 * При отрицательном балансе периодически начисляются проценты через {@link #execute()}.
 * 
 *
 * <p>
 * Особенности:
 * <ul>
 *   <li>кредитный лимит задаётся через {@link CreditAllowed#DEFAULT_CREDIT_LIMIT} по умолчанию</li>
 *   <li>лимит можно изменить через {@link #setCreditLimit(long)}</li>
 *   <li>снятие средств возможно, если сумма не превышает баланс + кредитный лимит</li>
 *   <li>проценты начисляются только при отрицательном балансе</li>
 * </ul>
 * 
 *
 * <p>
 * Проценты начисляются по формуле:
 * <pre>
 * новый_баланс = баланс + баланс * DEFAULT_CREDIT_PERCENT
 * </pre>
 * (при отрицательном балансе это увеличивает долг)
 * 
 *
 * @see Account
 * @see CreditAllowed
 */
public class CreditAccount extends Account implements CreditAllowed {
    private long creditLimit;

    /**
     * Создаёт кредитный счёт.
     *
     * @param bankOwner       банк-владелец
     * @param number          номер счёта (уникальный в рамках банка)
     * @param owner           имя владельца
     * @param initialBalance  начальный баланс (в копейках/центах)
     */
    public CreditAccount(Bank bankOwner, int number, String owner, long initialBalance) {
        super(bankOwner, number, owner, initialBalance);
        creditLimit = CreditAllowed.DEFAULT_CREDIT_LIMIT; //Базовый кредитный лимит
    }

    //######################## Создание заявки на транзакцию #############################


    //######################## Действия со средствами #############################

    /**
     * Проверяет возможность снятия указанной суммы.
     * <p>
     * Для кредитного счёта снятие возможно, если:
     * <pre>
     * текущий_баланс + кредитный_лимит >= сумма_снятия
     * </pre>
     * 
     *
     * @param amount сумма снятия
     * @return true если снятие возможно, иначе false
     */
    @Override
    public boolean canWithdraw(long amount) {
        return balance.get() + creditLimit >= amount;
    }

    /**
     * Возвращает сумму, которой не хватает для снятия.
     * <p>
     * Если снятие возможно, возвращает 0.
     * Иначе возвращает разницу между суммой снятия и доступными средствами
     * (баланс + кредитный лимит).
     * 
     *
     * @param amount сумма снятия
     * @return 0 если средств достаточно, иначе недостающая сумма
     */
    @Override
    public long notEnough(long amount) {
        if (amount < (balance.get() + creditLimit)) return 0;
        else return amount - balance.get() - creditLimit;
    }

    /**
     * Начисляет проценты по кредитному счёту.
     * <p>
     * Проценты начисляются только при отрицательном балансе.
     * Формула: баланс += баланс * DEFAULT_CREDIT_PERCENT
     * (при отрицательном балансе это увеличивает сумму долга).
     * 
     */
    @Override
    public void execute() {
        if (balance.get() < 0) balance.addAndGet((long)(balance.get() * CreditAllowed.DEFAULT_CREDIT_PERCENT));
    }

    //######################## Геттеры и сеттеры #############################

    public void setCreditLimit(long creditLimit) {
        this.creditLimit = creditLimit;
    }

    public long getCreditLimit() {
        return creditLimit;
    }
}

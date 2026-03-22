package com.github.zeykrus.bankpet.account;

import com.github.zeykrus.bankpet.services.Bank;
import com.github.zeykrus.bankpet.interfaces.InterestBearing;

/**
 * Процентный сберегательный счёт.
 * <p>
 * Этот тип счёта отличается от обычного {@link SavingsAccount} тем, что на него
 * периодически начисляются проценты через {@link #execute()}. Проценты начисляются
 * только на положительный баланс.
 * 
 *
 * <p>
 * Проценты начисляются по формуле:
 * <pre>
 * новый_баланс = баланс + баланс * DEFAULT_RATE
 * </pre>
 * где {@link InterestBearing#DEFAULT_RATE} = 0.03 (3%).
 * 
 *
 * <p>
 * Счёт наследует все характеристики {@link SavingsAccount}:
 * <ul>
 *   <li>баланс атомарный ({@link java.util.concurrent.atomic.AtomicLong})</li>
 *   <li>снятие возможно только при положительном балансе</li>
 *   <li>операции потокобезопасны</li>
 * </ul>
 * 
 *
 * @see SavingsAccount
 * @see InterestBearing
 */
public class InterestBearingAccount extends SavingsAccount implements InterestBearing {

    /**
     * Создаёт процентный сберегательный счёт.
     *
     * @param bankOwner       банк-владелец
     * @param number          номер счёта (уникальный в рамках банка)
     * @param owner           имя владельца
     * @param initialBalance  начальный баланс (в копейках/центах)
     */
    public InterestBearingAccount(Bank bankOwner, int number, String owner, long initialBalance) {
        super(bankOwner, number, owner, initialBalance);
    }

    /**
     * Начисляет проценты на остаток по счёту.
     * <p>
     * Проценты начисляются только при положительном балансе.
     * Формула: баланс += баланс * {@link InterestBearing#DEFAULT_RATE}
     * 
     */
    @Override
    public void execute() {
        if (balance.get() > 0) balance.addAndGet((long) (balance.get() * InterestBearing.DEFAULT_RATE));
    }
}

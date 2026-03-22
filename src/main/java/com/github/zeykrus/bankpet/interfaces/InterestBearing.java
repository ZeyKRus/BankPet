package com.github.zeykrus.bankpet.interfaces;

/**
 * Интерфейс для счетов, на которые начисляются проценты.
 * <p>
 * Проценты начисляются периодически через метод {@link #execute()}.
 * 
 *
 * @see com.github.zeykrus.bankpet.account.InterestBearingAccount
 */
public interface InterestBearing extends PeriodicOperation {
    double DEFAULT_RATE = 0.03;

}

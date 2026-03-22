package com.github.zeykrus.bankpet.interfaces;

/**
 * Интерфейс для счетов, поддерживающих кредитование.
 * <p>
 * Кредитные счета могут уходить в минус до определённого лимита.
 * При отрицательном балансе начисляются проценты.
 * 
 *
 * @see com.github.zeykrus.bankpet.account.CreditAccount
 */
public interface CreditAllowed extends PeriodicOperation {
    double DEFAULT_CREDIT_PERCENT = 0.05;
    long DEFAULT_CREDIT_LIMIT = 1000;

    /**
     * Устанавливает кредитный лимит.
     *
     * @param credit новый кредитный лимит (в копейках/центах)
     */
    void setCreditLimit(long credit);

    /**
     * Возвращает текущий кредитный лимит.
     *
     * @return кредитный лимит (в копейках/центах)
     */
    long getCreditLimit();
}

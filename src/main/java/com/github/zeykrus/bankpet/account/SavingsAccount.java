package com.github.zeykrus.bankpet.account;

import com.github.zeykrus.bankpet.services.Bank;

/**
 * Сберегательный банковский счёт.
 * <p>
 * Это базовый тип счёта, который позволяет:
 * <ul>
 *   <li>пополнять и снимать средства</li>
 *   <li>проверять баланс</li>
 *   <li>отправлять запросы на операции</li>
 * </ul>
 * 
 *
 * <p>
 * Особенности:
 * <ul>
 *   <li>баланс хранится в {@link java.util.concurrent.atomic.AtomicLong} и потокобезопасен</li>
 *   <li>снятие средств возможно только при наличии достаточного баланса</li>
 *   <li>операции выполняются через асинхронную очередь ({@link com.github.zeykrus.bankpet.services.QueueManager})</li>
 * </ul>
 * 
 *
 * <p>
 * Этот класс может быть расширен для создания специализированных счетов,
 * например {@link InterestBearingAccount} (процентный) или {@link CreditAccount} (кредитный).
 * 
 *
 * @see Account
 * @see InterestBearingAccount
 * @see CreditAccount
 */
public class SavingsAccount extends Account {

    /**
     * Создаёт сберегательный счёт.
     *
     * @param bankOwner       банк-владелец
     * @param number          номер счёта (уникальный в рамках банка)
     * @param owner           имя владельца
     * @param initialBalance  начальный баланс (в копейках/центах)
     */
    public SavingsAccount(Bank bankOwner, int number, String owner, long initialBalance) {
        super(bankOwner, number, owner, initialBalance);
    }

    //######################## Создание заявки на транзакцию #############################


    //######################## Действия со средствами #############################

    /**
     * Проверяет возможность снятия указанной суммы.
     * <p>
     * Для сберегательного счёта снятие возможно, если баланс больше или равен
     * запрашиваемой сумме.
     * 
     *
     * @param amount сумма снятия
     * @return true если снятие возможно, иначе false
     */
    @Override
    public boolean canWithdraw(long amount) {
        return balance.get() >= amount;
    }

    /**
     * Возвращает сумму, которой не хватает для снятия.
     * <p>
     * Если снятие возможно, возвращает 0.
     * Иначе возвращает разницу между суммой снятия и текущим балансом.
     * 
     *
     * @param amount сумма снятия
     * @return 0 если средств достаточно, иначе недостающая сумма
     */
    @Override
    public long notEnough(long amount) {
        if (amount < balance.get()) return 0;
        else return amount - balance.get();
    }

}
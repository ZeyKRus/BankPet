package com.github.zeykrus.bankpet.model;

import com.github.zeykrus.bankpet.account.Account;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Запрос на выполнение транзакции.
 * <p>
 * Неизменяемый объект, содержащий все данные, необходимые для выполнения
 * банковской операции: счета, тип операции и сумму.
 * 
 *
 * <p>
 * Запросы помещаются в очередь {@link com.github.zeykrus.bankpet.services.QueueManager}
 * и обрабатываются асинхронно.
 * 
 *
 * @param accFrom       счёт отправителя (для DEPOSIT — целевой счёт)
 * @param accTo         счёт получателя (для DEPOSIT и WITHDRAW — null)
 * @param operationType тип операции
 * @param amount        сумма операции (в копейках/центах)
 */
public record TransactionRequest(
        Account accFrom,
        Account accTo,
        OperationType operationType,
        long amount
) implements Comparable<TransactionRequest> {

    /** Poison pill для остановки воркеров */
    public static final TransactionRequest POISON;

    static {
        POISON = new TransactionRequest(null, null, null, Integer.MAX_VALUE);
    }

    /**
     * Сравнивает запросы по сумме (убывание).
     * <p>
     * Запросы с большей суммой имеют более высокий приоритет.
     * 
     */
    @Override
    public int compareTo(@NotNull TransactionRequest o) {
        return Double.compare(o.amount(),this.amount());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionRequest that = (TransactionRequest) o;
        return Double.compare(amount, that.amount) == 0 && Objects.equals(accTo, that.accTo) && Objects.equals(accFrom, that.accFrom) && operationType == that.operationType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accFrom, accTo, operationType, amount);
    }
};
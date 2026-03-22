package com.github.zeykrus.bankpet.model;

import com.github.zeykrus.bankpet.account.Account;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Запись о выполненной транзакции.
 * <p>
 * Хранит полную информацию о совершённой операции:
 * <ul>
 *   <li>время выполнения</li>
 *   <li>счёт отправителя и получателя</li>
 *   <li>тип операции</li>
 *   <li>сумму</li>
 *   <li>статус выполнения</li>
 * </ul>
 * 
 *
 * @param dateTime      время выполнения операции
 * @param accFrom       счёт, с которого списаны средства (для DEPOSIT — целевой счёт)
 * @param accTo         счёт, на который зачислены средства (для WITHDRAW — null)
 * @param operationType тип операции
 * @param amount        сумма операции (в копейках/центах)
 * @param success       статус выполнения (true — успешно, false — ошибка)
 */
public record Transaction(
        LocalDateTime dateTime,
        Account accFrom,
        Account accTo,
        OperationType operationType,
        long amount,
        boolean success
) implements Comparable<Transaction> {

    /**
     * Создаёт транзакцию из запроса.
     *
     * @param time    время выполнения
     * @param req     исходный запрос
     * @param success статус выполнения
     * @return новая транзакция
     */
    public static Transaction fromRequest(LocalDateTime time, TransactionRequest req, boolean success) {
        return new Transaction(
                time,
                req.accFrom(),
                req.accTo(),
                req.operationType(),
                req.amount(),
                success
        );
    }

    /**
     * Сравнивает транзакции по времени (естественный порядок).
     */
    @Override
    public int compareTo(@NotNull Transaction o) {
        return this.dateTime.compareTo(o.dateTime());
    }

    /**
     * Сравнивает транзакции по сумме (убывание).
     */
    public int compareByAmount(@NotNull Transaction o) {
        return Double.compare(o.amount(),this.amount());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Double.compare(amount, that.amount) == 0 && success == that.success && Objects.equals(accTo, that.accTo) && Objects.equals(accFrom, that.accFrom) && Objects.equals(dateTime, that.dateTime) && operationType == that.operationType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dateTime, accFrom, accTo, operationType, amount, success);
    }
}

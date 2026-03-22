package com.github.zeykrus.bankpet.interfaces;

/**
 * Интерфейс для операций, выполняемых периодически.
 * <p>
 * Классы, реализующие этот интерфейс, могут участвовать в массовых
 * периодических операциях (например, начисление процентов).
 * 
 *
 */
public interface PeriodicOperation {
    void execute();
}

package com.github.zeykrus.bankpet.interfaces;

/**
 * Функциональный интерфейс, аналогичный {@link java.util.function.Consumer},
 * но позволяющий выбрасывать checked-исключения.
 * <p>
 * Используется в {@link com.github.zeykrus.bankpet.services.ActionHandler}
 * для обработки операций, которые могут кидать {@link Exception}.
 * 
 *
 * @param <T> тип входного аргумента
 */
@FunctionalInterface
public interface ThrowingConsumer<T> {

    /**
     * Выполняет операцию над переданным аргументом.
     *
     * @param t входной аргумент
     * @throws Exception любое исключение, возникшее при выполнении
     */
    void accept(T t) throws Exception;
}

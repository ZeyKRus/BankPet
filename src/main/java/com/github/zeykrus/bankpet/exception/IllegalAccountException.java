package com.github.zeykrus.bankpet.exception;

/**
 * Исключение, выбрасываемое при попытке выполнить операцию
 * с несуществующим или некорректным счётом.
 *
 */
public class IllegalAccountException extends Exception {

    /**
     * Создаёт исключение с сообщением.
     *
     * @param message сообщение об ошибке
     */
    public IllegalAccountException(String message) {
        super(message);
    }
}

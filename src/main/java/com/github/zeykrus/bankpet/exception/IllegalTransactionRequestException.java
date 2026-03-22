package com.github.zeykrus.bankpet.exception;

/**
 * Исключение, выбрасываемое при попытке обработать некорректный запрос
 * на транзакцию (например, null-запрос).
 *
 */
public class IllegalTransactionRequestException extends Exception {

    /**
     * Создаёт исключение с сообщением.
     *
     * @param message сообщение об ошибке
     */
    public IllegalTransactionRequestException(String message) {
        super(message);
    }
}

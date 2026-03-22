package com.github.zeykrus.bankpet.exception;

/**
 * Исключение, выбрасываемое при превышении лимита попыток CAS-операции
 * при снятии средств с высокой конкуренцией.
 *
 */
public class WithdrawCASException extends Exception {

    /**
     * Создаёт исключение с сообщением.
     *
     * @param message сообщение об ошибке
     */
    public WithdrawCASException(String message) {
        super(message);
    }
}

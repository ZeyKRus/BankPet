package com.github.zeykrus.bankpet.exception;

/**
 * Исключение, выбрасываемое при попытке снять сумму,
 * превышающую доступный баланс (с учётом кредитного лимита).
 *
 */
public class InsufficientFundsException extends Exception {
    private final long deficit;

    /**
     * Создаёт исключение с сообщением и суммой недостающих средств.
     *
     * @param message сообщение об ошибке
     * @param deficit сумма, которой не хватает
     */
    public InsufficientFundsException(String message, long deficit) {
        super(message);
        this.deficit = deficit;
    }

    /**
     * Возвращает сумму недостающих средств.
     *
     * @return дефицит средств
     */
    public long getDeficit() {
        return deficit;
    }
}

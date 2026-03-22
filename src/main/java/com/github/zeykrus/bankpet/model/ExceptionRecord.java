package com.github.zeykrus.bankpet.model;

/**
 * Запись об ошибке при обработке транзакции.
 * <p>
 * Содержит:
 * <ul>
 *   <li>исходный запрос, вызвавший ошибку</li>
 *   <li>исключение, которое возникло</li>
 *   <li>счётчик неудачных попыток обработки</li>
 * </ul>
 * 
 *
 * <p>
 * Используется в {@link com.github.zeykrus.bankpet.services.ExceptionHandler}
 * для реализации механизма повторных попыток (retry) и Dead Letter Queue.
 * 
 */
public class ExceptionRecord {
    public static final ExceptionRecord POISON;
    private final TransactionRequest req;
    private final Exception exception;
    private int failings;

    static {
        POISON = new ExceptionRecord(null, null);
        POISON.failings = -1;
    }

    /**
     * Создаёт запись об ошибке.
     *
     * @param req       запрос, вызвавший ошибку
     * @param exception возникшее исключение
     */
    public ExceptionRecord(TransactionRequest req, Exception exception)
    {
        this.req = req;
        this.exception = exception;
        this.failings = 0;
    }

    public void incrementFailings() {
        failings++;
    }

    /**
     * Возвращает исходный запрос.
     *
     * @return запрос
     */
    public TransactionRequest getReq() {
        return req;
    }

    /**
     * Возвращает исключение.
     *
     * @return исключение
     */
    public Exception getException() {
        return exception;
    }

    /**
     * Возвращает количество неудачных попыток.
     *
     * @return счётчик попыток
     */
    public int getFailings() {
        return failings;
    }

    @Override
    public String toString() {
        String reqString;
        String excString;
        if (req == null) reqString = "Null"; else reqString = req.toString();
        if (exception == null) excString = "Null"; else excString = exception.toString();
        return "Транзакция: " + reqString + " Исключение: " + excString + " Попытка: " + failings;
    }
}

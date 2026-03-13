package com.github.zeykrus.bankpet;

import com.github.zeykrus.bankpet.account.Account;
import com.github.zeykrus.bankpet.exception.IllegalAccountException;
import com.github.zeykrus.bankpet.exception.IllegalTransactionRequestException;
import com.github.zeykrus.bankpet.exception.InsufficientFundsException;
import com.github.zeykrus.bankpet.interfaces.PeriodicOperation;
import com.github.zeykrus.bankpet.model.ExceptionRecord;
import com.github.zeykrus.bankpet.model.Transaction;
import com.github.zeykrus.bankpet.model.TransactionRequest;
import com.github.zeykrus.bankpet.services.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class FinanceCoreEngine {
    private final BankManager bankManager;
    private final ActionHandler actionHandler;
    private final QueueManager queueManager;
    private final ExceptionQueue exceptionQueue;
    private final ExceptionHandler exceptionHandler;
    private final int MAX_RETRIES = 5;
    private final List<ExceptionRecord> deadLetterQueue;

    public FinanceCoreEngine() {
        this.bankManager = new BankManager(this);
        this.actionHandler = new ActionHandler(new HistoryManager());
        this.queueManager = new QueueManager();
        this.exceptionQueue = new ExceptionQueue();
        this.exceptionHandler = new ExceptionHandler();
        this.deadLetterQueue = new LinkedList<>();
    }

    public void newRequest(TransactionRequest req) {
        queueManager.add(req);
    }

    public void handleRequestFromQueue() {
        Optional<TransactionRequest> opt = queueManager.poll();
        opt.ifPresentOrElse(s -> {
            try {
                actionHandler.handle(s);
            } catch (InsufficientFundsException e) {
                System.err.println("Бизнес-ошибка при обработке запроса: "+e.getMessage());
                exceptionQueue.add(new ExceptionRecord(s,e));
            } catch (IllegalArgumentException | IllegalStateException |
                    IllegalAccountException e) {
                System.err.println("Ошибка валидации: "+e.getMessage());
                exceptionQueue.add(new ExceptionRecord(s,e));
            } catch (IllegalTransactionRequestException e) {
                System.err.println("Ошибка запроса: "+e.getMessage());
                exceptionQueue.add(new ExceptionRecord(s,e));
            } catch (Exception e) {
                System.err.println("Неизвестная ошибка при выполнения запроса "+s+" Сообщение: "+e.getMessage());
                exceptionQueue.add(new ExceptionRecord(s,e));
            }
        }, () -> System.out.println("Очередь пуста"));
    }

    public void exceptionHandle() {
        Optional<ExceptionRecord> opt = exceptionQueue.poll();
        opt.ifPresentOrElse(s -> {
            if(!exceptionHandler.accept(s)) {
                //TODO Переделать обработку исключений. Пока все кидаем обратно в очередь, увеличиваем счетчик
                //TODO Кидаем в deadLetter если повторяется уже в MAX_RETRIES раз
                if (s.getFailings() >= MAX_RETRIES) {
                    deadLetterQueue.add(s);
                    System.err.println("Новая повторяющаяся ошибка, требуется внимание администратора");
                } else {
                    s.incrementFailings();
                    exceptionQueue.add(s);
                }
            }
        }, () -> System.out.println("Очередь ошибок пуста"));
    }

    public void executeAll() {
        List<Account> accounts = bankManager.getAllAccounts();
        accounts.stream()
                .filter(acc -> acc instanceof PeriodicOperation)
                .forEach(acc -> ((PeriodicOperation) acc).execute());
    }

    public List<Transaction> getHistory(Account acc) {
        return actionHandler.getHistory(acc);
    }

}

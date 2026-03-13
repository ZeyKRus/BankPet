package main.java.com.github.zeykrus.bankpet;

import main.java.com.github.zeykrus.bankpet.account.Account;
import main.java.com.github.zeykrus.bankpet.exception.IllegalAccountException;
import main.java.com.github.zeykrus.bankpet.exception.IllegalTransactionRequestException;
import main.java.com.github.zeykrus.bankpet.exception.InsufficientFundsException;
import main.java.com.github.zeykrus.bankpet.interfaces.PeriodicOperation;
import main.java.com.github.zeykrus.bankpet.model.ExceptionRecord;
import main.java.com.github.zeykrus.bankpet.model.Transaction;
import main.java.com.github.zeykrus.bankpet.model.TransactionRequest;
import main.java.com.github.zeykrus.bankpet.services.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class FinanceCoreEngine {
    private final BankManager bankManager;
    private final ActionHandler actionHandler;
    private final QueueManager queueManager;
    private final ExceptionQueue exceptionQueue;
    private final ExceptionHandler exceptionHandler;

    public FinanceCoreEngine() {
        this.bankManager = new BankManager(this);
        this.actionHandler = new ActionHandler();
        this.queueManager = new QueueManager();
        this.exceptionQueue = new ExceptionQueue();
        this.exceptionHandler = new ExceptionHandler();
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
        AtomicBoolean success = new AtomicBoolean(false);
        opt.ifPresentOrElse(s -> {
            success.set(exceptionHandler.accept(s));
        }, () -> System.out.println("Очередь ошибок пуста"));
        if (!success.get()) {
            //TODO подумать над тем, какие исключения опять кинуть в очередь исключений, а какие обработать иначе
        }
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

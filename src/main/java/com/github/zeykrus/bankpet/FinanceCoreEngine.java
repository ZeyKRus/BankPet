package main.java.com.github.zeykrus.bankpet;

import main.java.com.github.zeykrus.bankpet.account.Account;
import main.java.com.github.zeykrus.bankpet.interfaces.PeriodicOperation;
import main.java.com.github.zeykrus.bankpet.model.TransactionRequest;
import main.java.com.github.zeykrus.bankpet.services.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FinanceCoreEngine {
    private final BankManager bankManager;
    private final ActionHandler actionHandler;
    private final QueueManager queueManager;

    public FinanceCoreEngine() {
        this.bankManager = new BankManager(this);
        this.actionHandler = new ActionHandler();
        this.queueManager = new QueueManager();
    }

    public void newRequest(TransactionRequest req) {
        queueManager.add(req);
    }

    public void handleRequestFromQueue() {
        Optional<TransactionRequest> opt = queueManager.poll();
        opt.ifPresentOrElse(actionHandler::handle, () -> System.out.println("Очередь пуста"));
    }

    public void executeAll() {
        List<Account> accounts = bankManager.getAllAccounts();
        accounts.stream()
                .filter(acc -> acc instanceof PeriodicOperation)
                .forEach(acc -> ((PeriodicOperation) acc).execute());
    }

}

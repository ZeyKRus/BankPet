package com.github.zeykrus.bankpet;

import com.github.zeykrus.bankpet.account.Account;
import com.github.zeykrus.bankpet.account.SavingsAccount;
import com.github.zeykrus.bankpet.model.OperationType;
import com.github.zeykrus.bankpet.model.TransactionRequest;
import com.github.zeykrus.bankpet.services.Bank;

import java.util.ArrayList;
import java.util.List;

public class SimpleThreadTest {
    public static void main(String[] args) throws InterruptedException {
        FinanceCoreEngine core = new FinanceCoreEngine();
        Bank bank = core.createBank("Bank1");
        Account acc = bank.createSavingAccount("Owner1", 0);
        //TransactionRequest req = new TransactionRequest(acc,acc, OperationType.DEPOSIT, 1);

        int amount = 1000;
        List<Thread> list = new ArrayList<>();
        for (int k = 0; k < 10; k++) list.add(new Thread(() -> {for (int i = 0; i < amount; i++) acc.deposit(1);}));
        for (Thread t : list) t.start();
        for (Thread t : list) t.join();
        System.out.println(acc.getBalance());
    }
}

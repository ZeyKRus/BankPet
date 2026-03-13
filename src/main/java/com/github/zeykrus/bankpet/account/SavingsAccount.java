package com.github.zeykrus.bankpet.account;

import com.github.zeykrus.bankpet.model.Bank;

public class SavingsAccount extends Account {

    public SavingsAccount(Bank bankOwner, int number, String owner, double initialBalance) {
        super(bankOwner, number, owner, initialBalance);
    }

    //######################## Создание заявки на транзакцию #############################


    //######################## Действия со средствами #############################

    @Override
    public boolean canWithdraw(double amount) {
        boolean can = false;
        if (balance >= amount) can = true;
        return can;
    }

    @Override
    public double notEnough(double amount) {
        if (amount < balance) return 0;
        else return amount - balance;
    }

}
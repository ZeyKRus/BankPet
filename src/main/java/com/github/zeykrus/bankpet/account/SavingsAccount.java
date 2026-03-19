package com.github.zeykrus.bankpet.account;

import com.github.zeykrus.bankpet.services.Bank;

public class SavingsAccount extends Account {

    public SavingsAccount(Bank bankOwner, int number, String owner, long initialBalance) {
        super(bankOwner, number, owner, initialBalance);
    }

    //######################## Создание заявки на транзакцию #############################


    //######################## Действия со средствами #############################

    @Override
    public boolean canWithdraw(long amount) {
        return balance.get() >= amount;
    }

    @Override
    public double notEnough(long amount) {
        if (amount < balance.get()) return 0;
        else return amount - balance.get();
    }

}
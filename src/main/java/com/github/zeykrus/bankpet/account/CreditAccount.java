package com.github.zeykrus.bankpet.account;

import com.github.zeykrus.bankpet.services.Bank;
import com.github.zeykrus.bankpet.interfaces.CreditAllowed;

public class CreditAccount extends Account implements CreditAllowed {
    private long creditLimit;

    public CreditAccount(Bank bankOwner, int number, String owner, long initialBalance) {
        super(bankOwner, number, owner, initialBalance);
        creditLimit = CreditAllowed.DEFAULT_CREDIT_LIMIT; //Базовый кредитный лимит
    }

    //######################## Создание заявки на транзакцию #############################


    //######################## Действия со средствами #############################

    @Override
    public boolean canWithdraw(long amount) {
        return balance.get() + creditLimit >= amount;
    }

    @Override
    public double notEnough(long amount) {
        if (amount < (balance.get() + creditLimit)) return 0;
        else return amount - balance.get() - creditLimit;
    }

    @Override
    public void execute() {
        if (balance.get() < 0) balance.addAndGet((long)(balance.get() * CreditAllowed.DEFAULT_CREDIT_PERCENT));
    }

    //######################## Геттеры и сеттеры #############################

    public void setCreditLimit(long creditLimit) {
        this.creditLimit = creditLimit;
    }

    public long getCreditLimit() {
        return creditLimit;
    }
}

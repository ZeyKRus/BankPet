package com.github.zeykrus.bankpet.account;

import com.github.zeykrus.bankpet.model.Bank;
import com.github.zeykrus.bankpet.interfaces.CreditAllowed;

public class CreditAccount extends Account implements CreditAllowed {
    private double creditLimit;

    public CreditAccount(Bank bankOwner, int number, String owner, double initialBalance) {
        super(bankOwner, number, owner, initialBalance);
        creditLimit = CreditAllowed.DEFAULT_CREDIT_LIMIT; //Базовый кредитный лимит
    }

    //######################## Создание заявки на транзакцию #############################


    //######################## Действия со средствами #############################

    @Override
    public boolean canWithdraw(double amount) {
        boolean can = false;
        if ((balance + creditLimit) >= amount) can = true;
        return can;
    }

    @Override
    public double notEnough(double amount) {
        if (amount < (balance + creditLimit)) return 0;
        else return amount - balance - creditLimit;
    }

    @Override
    public void execute() {
        if (balance < 0) balance += balance * CreditAllowed.DEFAULT_CREDIT_PERCENT;
    }

    //######################## Геттеры и сеттеры #############################

    public void setCreditLimit(double creditLimit) {
        this.creditLimit = creditLimit;
    }

    public double getCreditLimit() {
        return creditLimit;
    }
}

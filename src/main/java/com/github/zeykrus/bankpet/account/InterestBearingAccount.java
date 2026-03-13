package com.github.zeykrus.bankpet.account;

import com.github.zeykrus.bankpet.model.Bank;
import com.github.zeykrus.bankpet.interfaces.InterestBearing;

public class InterestBearingAccount extends SavingsAccount implements InterestBearing {

    public InterestBearingAccount(Bank bankOwner, int number, String owner, double initialBalance) {
        super(bankOwner, number, owner, initialBalance);
    }

    @Override
    public void execute() {
        if (balance > 0) balance += balance * InterestBearing.DEFAULT_RATE;
    }
}

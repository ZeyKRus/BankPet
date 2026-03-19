package com.github.zeykrus.bankpet.account;

import com.github.zeykrus.bankpet.services.Bank;
import com.github.zeykrus.bankpet.interfaces.InterestBearing;

public class InterestBearingAccount extends SavingsAccount implements InterestBearing {

    public InterestBearingAccount(Bank bankOwner, int number, String owner, long initialBalance) {
        super(bankOwner, number, owner, initialBalance);
    }

    @Override
    public void execute() {
        if (balance.get() > 0) balance.addAndGet((long) (balance.get() * InterestBearing.DEFAULT_RATE));
    }
}

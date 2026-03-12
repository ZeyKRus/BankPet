public class InterestBearingAccount extends SavingsAccount implements InterestBearing {

    public InterestBearingAccount(Bank bankOwner, int number, String owner, double initialBalance) {
        super(bankOwner, number, owner, initialBalance);
    }

    @Override
    public void applyInterest() {
        if (balance > 0) balance += balance * DEFAULT_RATE;
    }
}

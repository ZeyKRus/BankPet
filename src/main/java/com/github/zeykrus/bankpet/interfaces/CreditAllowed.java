package main.java.com.github.zeykrus.bankpet.interfaces;

public interface CreditAllowed extends PeriodicOperation {
    double DEFAULT_CREDIT_PERCENT = 0.05;
    double DEFAULT_CREDIT_LIMIT = 1000;

    void setCreditLimit(double credit);
    double getCreditLimit();
}

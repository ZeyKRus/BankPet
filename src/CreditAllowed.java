public interface CreditAllowed {
    double DEFAULT_CREDIT_PERCENT = 0.05;
    double DEFAULT_CREDIT_LIMIT = 1000;

    void applyCredit();
    void setCreditLimit(double credit);
    double getCreditLimit();
}

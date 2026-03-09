public class BankAccount {
    private final int number;
    private final String owner;
    private final Bank bankOwner;

    private double balance;


    public BankAccount(Bank bankOwner, int number, String owner, double initialBalance) {
        this.number = number;
        this.owner = owner;
        this.balance = initialBalance;
        this.bankOwner = bankOwner;
    }

    public void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Сумма пополнения счета должна быть больше нуля");
        balance += amount;
    }

    public void withdraw(double amount) throws InsufficientFundsException {
        if (amount <= 0) throw new IllegalArgumentException("Сумма снятия средств должна быть больше нуля");
        if (balance < amount) throw new InsufficientFundsException("Сумма снятия средств должна быть не больше, чем сумма имеющихся средств. Недостаток: "+(amount - balance), amount - balance);
        balance -= amount;
    }

    public double getBalance() {
        return balance;
    }

    public String getOwner() {
        return owner;
    }

    public int getNumber() {
        return number;
    }

    public Bank getBankOwner() {
        return bankOwner;
    }
}
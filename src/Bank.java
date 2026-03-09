import java.util.HashMap;
import java.util.Map;

public class Bank {

    private static int counter;

    private int accountNumber = 0;
    private final String name;
    private final int number;
    private final Map<Integer, BankAccount> accounts = new HashMap<>();


    public Bank(String name) {
        this.number = counter; //Автоматическое присвоение номера банка через счетчик
        counter++;

        this.name = name;
    }

    public BankAccount createAccount(String owner, double initialBalance) {
        BankAccount current = new BankAccount(this, accountNumber, owner, initialBalance);
        accounts.put(current.getNumber(),current);
        accountNumber++;
        return current;
    }

    public BankAccount findAccount(int accountNumber) {
        BankAccount acc = accounts.get(accountNumber);
        if (acc == null) throw new IllegalArgumentException("Аккаунт с указанным номером не существует");
        return acc;
    }

    public void transfer(BankAccount from, BankAccount to, double amount) throws InsufficientFundsException {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Счёт не найден");
        }
        if (from == to) {
            throw new IllegalArgumentException("Нельзя переводить самому себе");
        }
        from.withdraw(amount);
        to.deposit(amount);
    }

    

    public String getName() {
        return name;
    }

    public int getNumber() {
        return number;
    }

}

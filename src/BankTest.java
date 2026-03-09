import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class BankTest {

    BankAccount createBankAccount(Bank bankOwner, String owner, double initialBalance) {
        return bankOwner.createAccount(owner, initialBalance);
    }

    void depositPositive(BankAccount acc, double amount) {
        double except = acc.getBalance() + amount;
        acc.deposit(amount);
        Assertions.assertEquals(except, acc.getBalance(), "Остаток на счету не совпадает с ожиданиями");
    }

    void depositNonPositive(BankAccount acc, double amount) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> acc.deposit(amount),"Ожидалась ошибка суммы пополнения");
    }

    void withdrawPositive(BankAccount acc, double amount) {
        double except = acc.getBalance() - amount;
        try {
            acc.withdraw(amount);
        } catch (InsufficientFundsException e) {
            throw new RuntimeException(e);
        }
        Assertions.assertEquals(except, acc.getBalance(), "Остаток на счету не совпадает с ожиданиями");
    }

    void withdrawNonPositive(BankAccount acc, double amount) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {acc.withdraw(amount);}, "Ожидалось исключение списания невозможного значения");
    }

    void withdrawNotEnoughBalance(BankAccount acc, double amount) {
        Assertions.assertThrows(InsufficientFundsException.class, () -> {acc.withdraw(amount);}, "Ожидалось исключение нехватки средств");
    }

    void transferPositive(Bank bank, BankAccount from, BankAccount to, double amount) {
        double startBalanceFrom = from.getBalance();
        double startBalanceTo = to.getBalance();
        double exceptionFrom = startBalanceFrom - amount;
        double exceptionTo = startBalanceTo + amount;
        try {
            bank.transfer(from,to,amount);
        } catch (InsufficientFundsException e) {
            throw new RuntimeException(e);
        }
        Assertions.assertAll(
                () -> {Assertions.assertEquals(exceptionFrom, from.getBalance(), "Сумма средств счета списания не соответствует ожиданиям");},
                () -> {Assertions.assertEquals(exceptionTo, to.getBalance(), "Сумма средств счета пополнения не соответствует ожиданиям");}
        );
    }

    void transferNullBank(Bank bank, BankAccount from, BankAccount to, double amount) {
        Assertions.assertThrows(NullPointerException.class, () -> {bank.transfer(from, to, amount);}, "Ожидалось исключение NullPointerException");
    }

    void transferNullAccount(Bank bank, BankAccount from, BankAccount to, double amount) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {bank.transfer(from, to, amount);},"Ожидалось исключение отсутствующего счета");
    }

    void transferNonPositive(Bank bank, BankAccount from, BankAccount to, double amount) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {bank.transfer(from, to, amount);}, "Ожидалось исключение списания невозможного значения");
    }

    void transferNotEnough(Bank bank, BankAccount from, BankAccount to, double amount) {
        Assertions.assertThrows(InsufficientFundsException.class, () -> {bank.transfer(from, to, amount);}, "Ожидалось исключение нехватки средств");
    }

    void transferItself(Bank bank, BankAccount from, BankAccount to, double amount) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {bank.transfer(from, to, amount);},"Ожидалось исключение одинаковых счетов");
    }

    void findAccountPositive (Bank bank, int number) {
        BankAccount acc = bank.findAccount(number);
        Assertions.assertEquals(acc.getNumber(), number);
    }

    void findAccountNullBank (Bank bank, int number) {
        Assertions.assertThrows(NullPointerException.class, () -> {bank.findAccount(number);}, "Ожидалось исключение NullPointerException");
    }

    void findAccountNotExist (Bank bank, int number) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {bank.findAccount(number);}, "Ожидалась ошибка отсутствующего счета");
    }


    enum Action {
        DEPOSIT,
        DEPOSIT_ZERO,
        DEPOSIT_NEGATIVE,
        WITHDRAW,
        WITHDRAW_ZERO,
        WITHDRAW_NEGATIVE,
        WITHDRAW_NOT_ENOUGH,
        TRANSFER,
        TRANSFER_FROM_NULL,
        TRANSFER_TO_NULL,
        TRANSFER_ZERO,
        TRANSFER_NEGATIVE,
        TRANSFER_ITSELF,
        FIND_ACCOUNT,
        FIND_ACCOUNT_NOT_EXIST,
        FIND_ACCOUNT_BANK_NULL
    }


    record Scenario(Action action, Bank bankFrom, Bank bankTo, BankAccount accFrom, BankAccount accTo) {};

    static Stream<Scenario> scenarioProvider() {
        Random rand = new Random(42);
        int amount = 100;

        List<Scenario> list = new ArrayList<>();

        for (int i = 0; i < amount; i++) {
            Action act = Action.values()[rand.nextInt(0,Action.values().length)];

            Bank bankFrom = new Bank("Bank 1");
            Bank bankTo = new Bank("Bank 2");

            double balanceFrom = rand.nextDouble(100, 1000);
            double balanceTo = rand.nextDouble(100, 1000);

            BankAccount accFrom = bankFrom.createAccount("Account 1", balanceFrom);
            BankAccount accTo = bankTo.createAccount("Account 2", balanceTo);

            list.add(new Scenario(act,bankFrom,bankTo,accFrom,accTo));
        }

        return list.stream();
    }

    private class ActionHandler {
        private Map<Action, Consumer<Scenario>> handler = new HashMap<>();
        private final double POSITIVE_AMOUNT = 10;
        private final double NEGATIVE_AMOUNT = -10;
        private final double ZERO_AMOUNT = 0;
        private final double BIG_POSITIVE_AMOUNT = 100000;
        private final int NOT_EXISTING_ACCOUNT_NUMBER = 99999;

        public ActionHandler() {
            handler.put(Action.DEPOSIT, sc -> depositPositive(sc.accFrom(),POSITIVE_AMOUNT));
            handler.put(Action.DEPOSIT_NEGATIVE, sc -> depositNonPositive(sc.accFrom(), NEGATIVE_AMOUNT));
            handler.put(Action.DEPOSIT_ZERO, sc -> depositNonPositive(sc.accFrom(), ZERO_AMOUNT));
            handler.put(Action.WITHDRAW, sc -> withdrawPositive(sc.accFrom(), POSITIVE_AMOUNT));
            handler.put(Action.WITHDRAW_ZERO, sc -> withdrawNonPositive(sc.accFrom(), ZERO_AMOUNT));
            handler.put(Action.WITHDRAW_NEGATIVE, sc -> withdrawNonPositive(sc.accFrom(), NEGATIVE_AMOUNT));
            handler.put(Action.WITHDRAW_NOT_ENOUGH, sc -> withdrawNotEnoughBalance(sc.accFrom(), BIG_POSITIVE_AMOUNT));
            handler.put(Action.TRANSFER, sc -> transferPositive(sc.bankFrom(), sc.accFrom(), sc.accTo(), POSITIVE_AMOUNT));
            handler.put(Action.TRANSFER_ITSELF, sc -> transferItself(sc.bankFrom(), sc.accFrom(), sc.accFrom(), POSITIVE_AMOUNT));
            handler.put(Action.TRANSFER_NEGATIVE, sc -> transferNonPositive(sc.bankFrom(), sc.accFrom(), sc.accTo(), NEGATIVE_AMOUNT));
            handler.put(Action.TRANSFER_ZERO, sc -> transferNonPositive(sc.bankFrom(), sc.accFrom(), sc.accTo(), ZERO_AMOUNT));
            handler.put(Action.TRANSFER_FROM_NULL, sc -> transferNullAccount(sc.bankFrom(), null, sc.accTo(), POSITIVE_AMOUNT));
            handler.put(Action.TRANSFER_TO_NULL, sc -> transferNullAccount(sc.bankFrom(), sc.accFrom(), null, POSITIVE_AMOUNT));
            handler.put(Action.FIND_ACCOUNT, sc -> findAccountPositive(sc.bankFrom(), sc.accFrom().getNumber()));
            handler.put(Action.FIND_ACCOUNT_NOT_EXIST, sc -> findAccountNotExist(sc.bankFrom(), NOT_EXISTING_ACCOUNT_NUMBER));
            handler.put(Action.FIND_ACCOUNT_BANK_NULL, sc -> findAccountNullBank(null, sc.accFrom().getNumber()));
        }

        public void run(Scenario sc) {
            handler.get(sc.action()).accept(sc);
        }

    }

    @ParameterizedTest
    @MethodSource("scenarioProvider")
    void stressTest(Scenario sc) {
        ActionHandler handler = new ActionHandler();
        System.out.println("Trying "+sc.action());
        handler.run(sc);
    }

}

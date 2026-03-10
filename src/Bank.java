import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;

public class Bank {

    private static int counter;

    private int accountNumber = 0;

    private final String name;
    private final int number;
    private final Map<Integer, BankAccount> accounts = new HashMap<>();
    private final TreeMap<LocalDateTime, ArrayList<Transaction>> history = new TreeMap<>();
    private final PriorityQueue<TransactionRequest> transactionQueue = new PriorityQueue<>(TransactionRequest::compareTo);
    private final Map<OperationType, Consumer<TransactionRequest>> requestHandler = new HashMap<>();
    private final Map<OperationType, Integer> statistic = new HashMap<>();

    public Bank(String name) {
        this.number = counter; //Автоматическое присвоение номера банка через счетчик
        counter++;

        for (OperationType type : OperationType.values()) {
            statistic.put(type,0);
        }

        requestHandler.put(OperationType.DEPOSIT, this::deposit);
        requestHandler.put(OperationType.WITHDRAW, this::withdraw);
        requestHandler.put(OperationType.TRANSFER, t -> {
            try {
                transfer(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        this.name = name;
    }

    //######################## Действия с аккаунтами #############################

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

    //######################## Обработка истории и статистики #############################

    private void addToStatistic(OperationType type) {
        int current = statistic.get(type);
        statistic.put(type, current + 1);
    }

    private void addToHistory(TransactionRequest req, boolean success) {
        LocalDateTime time = LocalDateTime.now();
        ArrayList<Transaction> list = history.get(time);
        if (list == null) {
            history.put(time, new ArrayList<>());
        }
        history.get(time).add(Transaction.fromRequest(time,req,success));
        if (success) addToStatistic(req.operationType());
    }

    public List<Transaction> getHistory() {
        LocalDateTime start = history.firstKey();
        LocalDateTime finish = history.lastKey();
        return getHistory(start, finish);
    }

    public List<Transaction> getHistory(BankAccount acc) {
        LocalDateTime start = history.firstKey();
        LocalDateTime finish = history.lastKey();
        return getHistory(start, finish, acc);
    }

    public List<Transaction> getHistory(LocalDateTime from, LocalDateTime to) {
        BankAccount acc = null;
        return getHistory(from, to, acc);
    }

    public List<Transaction> getHistory(LocalDateTime from, LocalDateTime to, BankAccount acc) {
        OperationType type = null;
        return getHistory(from, to, acc, type);
    }

    public List<Transaction> getHistory(LocalDateTime from, LocalDateTime to, BankAccount acc, OperationType type) {
        SortedMap<LocalDateTime, ArrayList<Transaction>> subMap = history.subMap(from, to);
        List<Transaction> list = new ArrayList<>();
        subMap.forEach((k,s) -> list.addAll(s));

        List<Transaction> filteredList = list;

        if (acc != null) {
            filteredList = filteredList.stream()
                    .filter(t -> t.accFrom() == acc || t.accTo() == acc)
                    .toList();
        }

        if (type != null) {
            filteredList = filteredList.stream()
                    .filter(t -> t.operationType() == type)
                    .toList();
        }

        return filteredList;
    }

    public List<Transaction> getLast10(BankAccount acc) {
        List<Transaction> list = getHistory(acc);
        list = list.reversed();
        list = list.subList(0,Math.min(10,list.size()));
        return list;
    }

    //######################## Обработка запросов #############################

    public List<Transaction> last10Request(BankAccount acc) {
        return getLast10(acc);
    }

    public void getNewRequest(TransactionRequest req) {
        transactionQueue.add(req);
    }

    public void getRequestFromQueue() {
        if (transactionQueue.isEmpty()) throw new IllegalStateException("Очередь пуста");
        TransactionRequest req = transactionQueue.poll();

        requestHandler.get(req.operationType()).accept(req);
    }

    //######################## Действия со средствами на аккаунтах #############################

    public void transfer(TransactionRequest req) {
        BankAccount accFrom = req.accFrom();
        BankAccount accTo = req.accTo();
        double amount = req.amount();

        try {
            if (accFrom == null) throw new IllegalArgumentException("Счета списания не существует");
            if (accTo == null) throw new IllegalArgumentException("Счета пополнения не существует");
            if (amount <= 0) throw new IllegalArgumentException("Некорректная сумма перевода");
            if (accFrom.getBalance() < amount) throw new IllegalArgumentException("На счете недостаточно средств для перевода");

            accFrom.withdraw(amount);
            accTo.deposit(amount);
        } catch (Exception e) {
            addToHistory(req,false);
            throw new RuntimeException(e);
        }

        addToHistory(req,true);
    }

    public void deposit(TransactionRequest req) {
        BankAccount acc = req.accFrom();
        double amount = req.amount();

        try {
            if (acc == null) throw new IllegalArgumentException("Счета не существует");
            if (amount <= 0) throw new IllegalArgumentException("Некорректная сумма пополнения");
            acc.deposit(amount);
        } catch (Exception e) {
            addToHistory(req,false);
            throw new RuntimeException(e);
        }

        addToHistory(req,true);
    }

    public void withdraw(TransactionRequest req) {
        BankAccount acc = req.accFrom();
        double amount = req.amount();

        try {
            if (acc == null) throw new IllegalArgumentException("Счета не существует");
            if (amount <= 0) throw new IllegalArgumentException("Некорректная сумма списания");
            acc.withdraw(amount);
        } catch (Exception e) {
            addToHistory(req,false);
            throw new RuntimeException(e);
        }

        addToHistory(req,true);
    }

    //######################## Геттеры и сеттеры #############################

    public String getName() {
        return name;
    }

    public int getNumber() {
        return number;
    }

}

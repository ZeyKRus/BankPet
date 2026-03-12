import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Bank {

    private static int counter;

    private int accountNumber = 0;

    private final String name;
    private final int number;
    private final Map<Integer, Account> accounts = new HashMap<>();
    private final TreeMap<LocalDateTime, ArrayList<Transaction>> history = new TreeMap<>();
    private final PriorityQueue<TransactionRequest> transactionQueue = new PriorityQueue<>(TransactionRequest::compareTo);
    private final Map<OperationType, Consumer<TransactionRequest>> requestHandler = new HashMap<>();

    public Bank(String name) {
        this.number = counter; //Автоматическое присвоение номера банка через счетчик
        counter++;

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

    public SavingsAccount createSavingAccount(String owner, double initialBalance) {
        SavingsAccount current = new SavingsAccount(this, accountNumber, owner, initialBalance);
        accounts.put(current.getNumber(),current);
        accountNumber++;
        return current;
    }

    public CreditAccount createCreditAccount(String owner, double initialBalance) {
        CreditAccount current = new CreditAccount(this, accountNumber, owner, initialBalance);
        accounts.put(current.getNumber(),current);
        accountNumber++;
        return current;
    }

    public Account getAccount(int accountNumber) {
        return Optional.ofNullable(accounts.get(accountNumber)).orElseThrow(() -> new IllegalArgumentException("Счет не найден"));
    }

    public Optional<Account> findAccount(int accountNumber) {
        return Optional.ofNullable(accounts.get(accountNumber));
    }

    //######################## Обработка статистики #############################

    private Stream<Transaction> successTransactions() {
        return history.values().stream()
                .flatMap(ArrayList::stream)
                .filter(Transaction::success);
    }

    private Map<OperationType, Long> getStatistic() {
        return successTransactions()
                .collect(Collectors.groupingBy(Transaction::operationType,Collectors.counting()));

    }

    private long getCountByType(OperationType type) {
        return successTransactions()
                .filter(t -> t.operationType() == type)
                .count();
    }

    private double getSumByType(OperationType type) {
        return successTransactions()
                .filter(t -> t.operationType() == type)
                .collect(Collectors.summingDouble(Transaction::amount));
    }

    private List<Transaction> getTopTransactions(OperationType type, int n) {
        if (n <= 0) return List.of();
        return successTransactions()
                .filter(t -> t.operationType() == type)
                .sorted(Comparator.comparingDouble(Transaction::amount).reversed())
                .limit(n)
                .collect(Collectors.toList());
    }

    //######################## Обработка истории #############################

    private void addToHistory(TransactionRequest req, boolean success) {
        LocalDateTime time = LocalDateTime.now();
        ArrayList<Transaction> list = history.get(time);
        if (list == null) {
            history.put(time, new ArrayList<>());
        }
        history.get(time).add(Transaction.fromRequest(time,req,success));
    }

    public List<Transaction> getHistory(HistoryFilter filter) {
        if (history.isEmpty()) return new ArrayList<>();
        if (filter == null) filter = HistoryFilter.builder().build();

        LocalDateTime start = filter.getFrom();
        LocalDateTime finish = filter.getTo();
        Account acc = filter.getAcc();
        OperationType type = filter.getType();

        if (start != null && finish != null && start.isAfter(finish)) return new ArrayList<>();

        if (start == null) start = history.firstKey();
        if (finish == null) finish = history.lastKey();

        return history.subMap(start,finish).values().stream()
                .flatMap(ArrayList::stream)
                .filter(t -> acc == null || t.accFrom() == acc || t.accTo() == acc)
                .filter(t -> type == null || t.operationType() == type)
                .toList();
    }

    public List<Transaction> getLast10(Account acc) {
        List<Transaction> list = getHistory(HistoryFilter.builder().acc(acc).build());
        list = list.reversed();
        list = list.subList(0,Math.min(10,list.size()));
        return list;
    }

    //######################## Обработка запросов #############################

    public List<Transaction> last10Request(Account acc) {
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
        Account accFrom = req.accFrom();
        Account accTo = req.accTo();
        double amount = req.amount();

        try {
            if (accFrom == null) throw new IllegalArgumentException("Счета списания не существует");
            if (accTo == null) throw new IllegalArgumentException("Счета пополнения не существует");
            if (amount <= 0) throw new IllegalArgumentException("Некорректная сумма перевода");
            if (!accFrom.canWithdraw(amount)) throw new IllegalArgumentException("На счете недостаточно средств для перевода");

            accFrom.withdraw(amount);
            accTo.deposit(amount);
        } catch (Exception e) {
            addToHistory(req,false);
            throw new RuntimeException(e);
        }

        addToHistory(req,true);
    }

    public void deposit(TransactionRequest req) {
        Account acc = req.accFrom();
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
        Account acc = req.accFrom();
        double amount = req.amount();

        try {
            if (acc == null) throw new IllegalArgumentException("Счета не существует");
            if (amount <= 0) throw new IllegalArgumentException("Некорректная сумма списания");
            if (!acc.canWithdraw(amount)) throw new IllegalArgumentException("На счете недостаточно средств для перевода");

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

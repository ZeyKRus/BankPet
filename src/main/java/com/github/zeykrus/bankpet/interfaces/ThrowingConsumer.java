package main.java.com.github.zeykrus.bankpet.interfaces;

@FunctionalInterface
public interface ThrowingConsumer<T> {
    void accept(T t) throws Exception;
}

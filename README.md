# BankPet — учебный банковский проект на Java

[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.9-blue)](https://maven.apache.org/)
[![JUnit 5](https://img.shields.io/badge/JUnit%205-5.12.0-green)](https://junit.org/junit5/)
[![Mockito](https://img.shields.io/badge/Mockito-5.23.0-yellow)](https://site.mockito.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Учебный проект, демонстрирующий применение Java Collections Framework, Stream API, многопоточности, современных инструментов сборки и тестирования на примере простой банковской системы.

---

## 📌 О проекте

BankPet — это консольное приложение, моделирующее работу банка. Проект создавался для изучения и закрепления ключевых возможностей Java, а также для отработки навыков промышленной разработки:

-   **Коллекции:** `HashMap`, `TreeMap`, `PriorityQueue`.
-   **Функциональное программирование:** Stream API, лямбды, `Optional`, функциональные интерфейсы (`ThrowingConsumer`).
-   **Работа с датами:** `java.time`.
-   **Многопоточность:** атомарные типы (`AtomicLong`), потокобезопасные коллекции, синхронизация.
-   **Паттерны проектирования:** Builder, Dependency Injection (частично), стратегия, цепочка обязанностей.
-   **Системы сборки:** Maven (управление зависимостями, многомодульность).
-   **Тестирование:** JUnit 5, Mockito, параметризованные тесты.

---

## ✅ Текущие возможности (реализовано)

-   **Управление счетами:** создание счетов, поиск по номеру.
-   **Типы счетов:** сберегательный, кредитный (с лимитом), процентный.
-   **Финансовые операции:** пополнение, снятие, переводы между счетами.
-   **Приоритетная обработка:** заявки обрабатываются в порядке убывания суммы (`PriorityQueue`).
-   **Потокобезопасность:** баланс счетов реализован через `AtomicLong`, критичные секции защищены.
-   **Обработка ошибок:** отдельная очередь для сбойных запросов, счётчик попыток, **dead letter queue** для неисправимых ошибок.
-   **История операций:** каждая операция сохраняется в историю с временной меткой (`TreeMap`).
-   **Статистика:** подсчёт количества и общей суммы операций по типам (Stream API).
-   **Гибкая фильтрация:** поиск операций по диапазону дат, счету и типу с помощью `HistoryFilter` (паттерн Builder).
-   **Тестирование:** более **90 юнит-тестов** (покрытие ~80%) с использованием моков и параметризации.

---

## 🏗 Архитектура

Проект разделён на модули с чёткой ответственностью:

-   `com.github.zeykrus.bankpet.services.Bank` — фасад для управления счетами и взаимодействия с ядром.
-   `com.github.zeykrus.bankpet.FinanceCoreEngine` — оркестратор, связывающий очереди, обработчики и менеджеры.
-   `com.github.zeykrus.bankpet.services.AccountManager` — управление счетами внутри банка.
-   `com.github.zeykrus.bankpet.services.BankManager` — управление банками.
-   `com.github.zeykrus.bankpet.services.ActionHandler` — обработка транзакций (deposit, withdraw, transfer).
-   `com.github.zeykrus.bankpet.services.HistoryManager` — хранение и фильтрация истории операций.
-   `com.github.zeykrus.bankpet.services.QueueManager` — очередь запросов с приоритетом.
-   `com.github.zeykrus.bankpet.services.ExceptionQueue` / `ExceptionHandler` / `ExceptionRecord` — подсистема обработки ошибок (retry + DLQ).
-   `com.github.zeykrus.bankpet.interfaces` — контракты для расширения (`PeriodicOperation`, `ThrowingConsumer`, `CreditAllowed`, `InterestBearing`).
-   `com.github.zeykrus.bankpet.account` — иерархия счетов (абстрактный `Account`, `SavingsAccount`, `CreditAccount`, `InterestBearingAccount`).
-   `com.github.zeykrus.bankpet.model` — неизменяемые записи (`Transaction`, `TransactionRequest`, `HistoryFilter`, `ExceptionRecord`).
-   `com.github.zeykrus.bankpet.exception` — собственные checked-исключения (`InsufficientFundsException`, `IllegalAccountException`, `IllegalTransactionRequestException`).

---

## 🛠 Технологический стек

| Компонент              | Версия / Технология                |
|------------------------|-------------------------------------|
| Язык                   | Java 21                             |
| Сборка                 | Maven 3.9+                          |
| Тестирование           | JUnit 5.12.0, Mockito 5.23.0        |
| Многопоточность        | `AtomicLong`, `ConcurrentHashMap` (планируется) |
| Коллекции              | `HashMap`, `TreeMap`, `PriorityQueue` |
| Функциональные возможности | Stream API, лямбды, `Optional`, `record` |
| Паттерны               | Builder, Dependency Injection, Strategy |
| Система контроля версий| Git (ветки `main`, `refactoring`, `multithreading`) |

---

## 🧪 Покрытие тестами

На данный момент написано **91 успешный тест**, покрывающий:

-   Все операции со счетами (позитивные и негативные сценарии).
-   Логику обработки ошибок и ретраев.
-   Фильтрацию истории и статистику.
-   Многопоточные сценарии (проверка атомарности).

> Планируется дальнейшее расширение тестовой базы.

---

## 🔮 Планируемые улучшения (Next steps)

-   **Многопоточная обработка:** запуск обработчиков очереди в пуле потоков (`ExecutorService`).
-   **Concurrent collections:** замена `HashMap` на `ConcurrentHashMap` для потокобезопасного доступа.
-   **Dead Letter Queue:** интеграция с хранилищем (файл/база) для неисправимых ошибок.
-   **Spring Boot:** миграция на Spring для управления зависимостями и создания REST API.
-   **Docker:** контейнеризация приложения.
-   **CI/CD:** настройка GitHub Actions для автоматической сборки и тестирования.

---

## 🚀 Как собрать и запустить

1.  Убедись, что установлены **Java 21** и **Maven**.
2.  Склонируй репозиторий:
    ```bash
    git clone https://github.com/ZeyKRus/BankPet.git
    cd BankPet
    ```
3.  Собери проект и запусти тесты:
    ```bash
    mvn clean test
    ```
4.  (Опционально) Собери JAR:
    ```bash
    mvn clean package
    java -jar target/bankpet-1.0-SNAPSHOT.jar
    ```

---

## 📄 Лицензия

Проект распространяется под лицензией MIT. Подробнее — в файле [LICENSE](LICENSE).

---

**Автор:** [ZeyKRus](https://github.com/ZeyKRus)  
**Курс:** Самостоятельное изучение Java, март 2026
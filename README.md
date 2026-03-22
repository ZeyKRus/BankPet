# BankPet — учебный банковский проект на Java

[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.9-blue)](https://maven.apache.org/)
[![JUnit 5](https://img.shields.io/badge/JUnit%205-5.12.0-green)](https://junit.org/junit5/)
[![Mockito](https://img.shields.io/badge/Mockito-5.23.0-yellow)](https://site.mockito.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**Pet-проект: многопоточная банковская система на Java 21**  
*Версия 1.0 — завершённый проект, демонстрирующий промышленный подход к разработке.*

---

## 📌 О проекте

BankPet — это учебный проект, который моделирует работу банка: счета, переводы, асинхронную обработку запросов, очередь ошибок, повторные попытки и Dead Letter Queue.  
Проект **завершён** и служит демонстрацией навыков проектирования многопоточных систем, работы с коллекциями, тестирования и управления зависимостями.

---

## 🧠 Архитектура

Система построена на принципах **SOLID** и состоит из нескольких независимых модулей:

| Модуль | Ответственность |
|--------|-----------------|
| `FinanceCoreEngine` | Оркестрация всех сервисов |
| `QueueManager` | Очередь запросов с приоритетом (на основе суммы) |
| `QueueProcessingService` + `RequestProcessor` | Пул воркеров для обработки запросов |
| `ExceptionQueue` | Очередь ошибок |
| `ExceptionProcessingService` + `ExceptionProcessor` | Пул воркеров для обработки ошибок (retry, DLQ) |
| `ActionHandler` | Бизнес-логика (deposit, withdraw, transfer) |
| `HistoryManager` | История операций (синхронизированная) |
| `Account` (и наследники) | Счета с атомарным балансом (`AtomicLong`) |

### Потокобезопасность
- Все очереди — `BlockingQueue`
- Счета — `AtomicLong` + CAS
- Коллекции — `ConcurrentHashMap`
- История — `synchronized`

### Обработка ошибок
- Запросы, упавшие с исключением, попадают в `ExceptionQueue`
- Каждая ошибка имеет счётчик попыток
- После 5 неудач запрос уходит в **Dead Letter Queue** (DLQ)
- Остановка воркеров — через **poison pill**

---

## 🛠 Технологический стек

| Компонент | Технология |
|-----------|------------|
| Язык | Java 21 |
| Система сборки | Maven |
| Тестирование | JUnit 5, Mockito |
| Логирование | SLF4J + Logback |
| Многопоточность | `BlockingQueue`, `ExecutorService`, `AtomicLong`, `volatile` |

---

## 🚀 Сборка и запуск

### Требования
- Java 21
- Maven 3.9+

### Команды
```bash
mvn clean compile      # компиляция
mvn clean test         # запуск тестов
mvn clean package      # сборка JAR
```

### Пример использования
```java
FinanceCoreEngine core = new FinanceCoreEngine();
Bank bank = core.createBank("MyBank");
Account acc = bank.createSavingAccount("Иван", 1000);

// Асинхронное пополнение
acc.depositRequest(500);
core.startProcessingQueue(2);   // запускаем 2 воркера

// Завершение работы
core.stopProcessingQueue();
```

---

## 📂 Структура проекта

```
src/main/java/com/github/zeykrus/bankpet/
├── account/               # Иерархия счетов
├── exception/             # Кастомные исключения
├── interfaces/            # ThrowingConsumer, PeriodicOperation и др.
├── model/                 # Transaction, TransactionRequest, HistoryFilter
├── services/              # Основные сервисы (Bank, ActionHandler, ...)
└── FinanceCoreEngine.java # Оркестратор
```

---

## 🧪 Тестирование

- **Общее покрытие:** >60%
- **Проверены:**
    - Бизнес-логика счетов
    - Многопоточная обработка очередей
    - Retry и Dead Letter Queue
    - Корректная остановка воркеров через poison pill
    - Параллельная обработка запросов

Тесты написаны с использованием `CountDownLatch` для синхронизации и `Mockito` для изоляции зависимостей.

---

## 📄 Лицензия

Проект распространяется под лицензией MIT. Подробнее — в файле [LICENSE](LICENSE).

---

## ✨ Заключение

Проект **BankPet** — это не просто учебный код, а пример осознанного проектирования многопоточной системы.  
В нём реализованы все ключевые принципы промышленной разработки:
- чистая архитектура
- потокобезопасность
- надёжная обработка ошибок
- тестирование
- логирование

Этот проект завершён. Дальнейшее развитие (REST API, Spring, базы данных) будет выполняться в рамках следующих пет-проектов.

**Автор:** [ZeyKRus](https://github.com/ZeyKRus)  
**Курс:** Самостоятельное изучение Java, март 2026
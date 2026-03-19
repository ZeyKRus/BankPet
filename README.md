# BankPet — учебный банковский проект на Java

[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Учебный проект, демонстрирующий применение Java Collections Framework, Stream API, Optional и многопоточности на примере простой банковской системы.

## О проекте

BankPet — это консольное приложение, моделирующее работу банка. Проект создавался для изучения и закрепления ключевых возможностей Java, включая:

-   **Коллекции:** `HashMap`, `TreeMap`, `PriorityQueue`.
-   **Функциональное программирование:** Stream API, лямбды, `Optional`.
-   **Работу с датами:** `java.time`.
-   **Паттерны проектирования:** Builder, Dependency Injection (частично).

## Основные возможности

-   **Управление счетами:** создание счетов, поиск по номеру.
-   **Финансовые операции:** пополнение, снятие, переводы между счетами.
-   **Приоритетная обработка:** заявки на перевод обрабатываются в порядке убывания суммы (`PriorityQueue`).
-   **История операций:** каждая операция сохраняется в историю с временной меткой (`TreeMap`).
-   **Статистика:** подсчёт количества и общей суммы операций по типам.
-   **Гибкая фильтрация:** поиск операций по диапазону дат, счету и типу с помощью `main.java.com.github.zeykrus.bankpet.model.HistoryFilter` (паттерн Builder).

## Архитектура

Проект состоит из нескольких ключевых классов:

-   `main.java.com.github.zeykrus.bankpet.services.Bank` — центральный класс, управляющий счетами, очередью запросов и историей.
-   `main.java.com.github.zeykrus.bankpet.account.SavingsAccount` — представляет банковский счёт, хранит баланс и владельца.
-   `main.java.com.github.zeykrus.bankpet.model.Transaction` — неизменяемый `record` с данными о совершённой операции.
-   `main.java.com.github.zeykrus.bankpet.model.TransactionRequest` — `record`, описывающий запрос на операцию.
-   `main.java.com.github.zeykrus.bankpet.model.HistoryFilter` — `класс` с паттерном Builder для фильтрации истории.
-   `main.java.com.github.zeykrus.bankpet.exception.InsufficientFundsException` — собственное checked исключение для бизнес-ситуаций.

## Технологический стек

-   **Java 21**
-   **JUnit 5** (для тестов)
# Система управления банковскими картами

Проект представляет собой **REST API сервис** для управления пользователями и банковскими картами, реализованный на **Spring Boot + PostgreSQL + Liquibase + JWT**.

Система поддерживает регистрацию пользователей, выпуск и управление картами, операции перевода средств и документирована через **Swagger (OpenAPI)**.

---

## 🚀 Стек технологий

* **Java 17**
* **Spring Boot 3**

    * Spring Web
    * Spring Data JPA (Hibernate)
    * Spring Security (JWT)
* **PostgreSQL**
* **Liquibase** — миграции БД
* **Swagger / Springdoc OpenAPI** — документация
* **Maven**
* **Docker + docker-compose** (Postgres в контейнере)

---

## ⚙️ Установка и запуск

### 1. Склонировать репозиторий

```bash
git clone https://github.com/stannisl/Bank_REST.git
cd Bank_REST
```

### 2. Запустить PostgreSQL через Docker

```bash
docker-compose up -d
```

Будет поднята база данных `bankcards` на порту `5432`.

### 3. Запустить приложение

```bash
mvn spring-boot:run
```

После старта сервис будет доступен по адресу:
👉 `http://localhost:8080`

---

## 📑 API Документация

Swagger UI доступен по адресу:
👉 [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

Основные эндпоинты:

### 🔑 Аутентификация

* `POST /api/auth/register` — регистрация нового пользователя
* `POST /api/auth/login` — вход (получение JWT-токена)

### 👤 Пользователи (Admin only)

* `GET /api/users` — список пользователей (с пагинацией)
* `GET /api/users/{id}` — получить пользователя по UUID
* `POST /api/users` — создать пользователя
* `PUT /api/users/{id}` — обновить данные пользователя
* `DELETE /api/users/{id}` — удалить пользователя

### 💳 Карты

* `GET /api/cards` — список карт (фильтрация по статусу: ACTIVE/BLOCKED/EXPIRED)
* `POST /api/cards` — создать карту для пользователя
* `PUT /api/cards/{id}/block` — блокировать карту
* `PUT /api/cards/{id}/activate` — активировать карту
* `GET /api/cards/{id}/balance` — получить баланс карты

### 🔄 Транзакции

* `POST /api/cards/transfer` — перевод средств между картами

---

## 🗄️ Миграции БД

Используется **Liquibase**.
Миграции находятся в `src/main/resources/db/migration`.

Создаётся 3 таблицы:

* `users` — пользователи (с ролью `USER`/`ADMIN`)
* `cards` — банковские карты (привязка к `users` через `owner_id`)
* `transactions` — переводы между картами

---

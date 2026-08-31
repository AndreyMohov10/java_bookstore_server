# Bookstore API

Учебный проект REST API для управления книжным магазином. Реализованы полноценные CRUD-операции, поиск с фильтрацией, оформление заказов с контролем остатков и аналитические отчёты.

---

## 🛠 Технологии
- **Java 17**
- **Spring Boot 3** (Web, Validation)
- **jOOQ** – типобезопасный слой доступа к БД
- **PostgreSQL** – реляционная база данных
- **Swagger/OpenAPI 3** (springdoc-openapi) – документация API
- **Maven** – сборка и управление зависимостями

---

## ⚙️ Функциональные возможности
- ✅ **Авторы** – создание, просмотр, обновление, удаление
- ✅ **Книги** – создание, просмотр, обновление, удаление, поиск с фильтрацией по:
    - части названия (регистронезависимый)
    - жанру
    - диапазону цен
    - автору
    - наличию на складе
    - пагинация (page, size)
- ✅ **Заказы** – атомарное оформление:
    - проверка наличия книг
    - списание остатков
    - создание заказа и позиций
- ✅ **Аналитика** – статистика продаж по авторам:
    - количество проданных книг
    - общая выручка
    - количество уникальных книг в продаже
- ✅ **Валидация** входных DTO с помощью Jakarta Validation
- ✅ **Глобальная обработка исключений** с ответами в формате **Problem Details** (RFC 7807)
- ✅ **Интерактивная документация** через Swagger UI

---

## 🚀 Запуск

### 1. Требования
- **PostgreSQL** (версия 12 или выше)
- **JDK 17**
- **Maven** (или используйте встроенный maven-wrapper)

### 2. Настройка базы данных
Создайте базу данных, например, `bookstore`:
```sql
CREATE DATABASE bookstore;
```

### 3. Конфигурация подключения
В файле `application.properties` (или `application.yml`) укажите параметры:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/bookstore
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

# Диалект для jOOQ (уже задан в JooqConfig)
```

### 4. Генерация классов jOOQ (если необходимо)
Проект уже содержит сгенерированные классы в пакете `org.example.bookstore.jooq.tables`. Если вы меняете схему БД, запустите генерацию через плагин jOOQ (конфигурация обычно в `pom.xml`).

### 5. Сборка и запуск
```bash
mvn clean spring-boot:run
```
Или запустите главный класс `BookstoreApplication` из вашей IDE.

Приложение будет доступно по адресу:  
**http://localhost:8080**

---

## 📁 Структура проекта
```
src/main/java/org/example/bookstore/
├── config/               # Конфигурационные классы (jOOQ, OpenAPI)
├── controller/           # REST-контроллеры
├── dto/                  # Объекты передачи данных (records)
├── exception/            # Кастомные исключения и глобальный обработчик
├── repository/           # Репозитории с jOOQ-запросами
├── service/              # Бизнес-логика (сервисы)
└── BookstoreApplication.java
```

---

## 📌 API Эндпоинты

| Метод | URL | Описание |
|-------|-----|----------|
| **Авторы** |||
| GET | `/api/authors` | Получить всех авторов |
| GET | `/api/authors/{id}` | Получить автора по ID |
| POST | `/api/authors` | Создать нового автора |
| PUT | `/api/authors/{id}` | Обновить автора |
| DELETE | `/api/authors/{id}` | Удалить автора |
| **Книги** |||
| GET | `/api/books` | Поиск книг с фильтрами и пагинацией |
| GET | `/api/books/{id}` | Получить книгу по ID |
| POST | `/api/books` | Добавить новую книгу |
| PUT | `/api/books/{id}` | Обновить книгу |
| DELETE | `/api/books/{id}` | Удалить книгу |
| **Заказы** |||
| POST | `/api/orders` | Оформить новый заказ (атомарно) |
| GET | `/api/orders/{id}` | Получить заказ по ID |
| **Аналитика** |||
| GET | `/api/analytics/authors-sales` | Статистика продаж по авторам |

**Полную документацию со схемами запросов и ответов** можно посмотреть в Swagger UI:  
[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## 📝 Примеры запросов

### 1. Создание автора
**POST** `/api/authors`
```json
{
  "name": "Лев Толстой",
  "bio": "Великий русский писатель",
  "birthDate": "1828-09-09"
}
```

### 2. Создание книги
**POST** `/api/books`
```json
{
  "title": "Война и мир",
  "isbn": "978-5-17-090830-4",
  "genre": "Исторический роман",
  "price": 850.00,
  "stockQuantity": 15,
  "authorId": 1
}
```

### 3. Поиск книг
**GET** `/api/books?titleLike=война&minPrice=500&inStockOnly=true&page=0&size=5`

### 4. Оформление заказа
**POST** `/api/orders`
```json
{
  "customerId": 1,
  "items": [
    {
      "bookId": 1,
      "quantity": 2
    }
  ]
}
```

### 5. Аналитика по авторам
**GET** `/api/analytics/authors-sales`
```json
[
  {
    "authorId": 1,
    "authorName": "Лев Толстой",
    "totalBooksSold": 45,
    "totalRevenue": 32400.00,
    "distinctBooksSold": 3
  }
]
```

---

## 🧩 Обработка ошибок

Все ошибки возвращаются в формате **Problem Details** (RFC 7807) с понятными сообщениями и дополнительными свойствами.

Пример ответа при нехватке книг (HTTP 422):
```json
{
  "type": "https://bookstore.example.com/errors/insufficient-stock",
  "title": "Недостаточно товара на складе",
  "status": 422,
  "detail": "Недостаточно экземпляров книги \"Война и мир\" (ID=1): запрошено 5, доступно 2",
  "bookId": 1,
  "bookTitle": "Война и мир",
  "requested": 5,
  "available": 2,
  "timestamp": "2026-08-31T10:15:30Z"
}
```

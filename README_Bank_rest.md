#  Система управления банковскими картами

REST API приложение для управления банковскими картами, построенное на Spring Boot с использованием JWT аутентификации и PostgreSQL.

##  Возможности

###  Для пользователей (USER)
- Просмотр своих карт с поиском и пагинацией
- Запрос блокировки карты
- Переводы между своими картами
- Просмотр баланса карт

###  Для администраторов (ADMIN)
- Создание новых карт для пользователей
- Блокировка и активация карт
- Удаление карт
- Просмотр всех карт в системе
- Назначение ролей пользователям
- Обновление истёкших карт

##  Технологии

- **Java 21**
- **Spring Boot 3.x**
- **Spring Security** + JWT
- **Spring Data JPA**
- **PostgreSQL**
- **Liquibase** (миграции БД)
- **Docker & Docker Compose**
- **Swagger/OpenAPI** (документация API)
- **Maven**

##  Быстрый старт

### Предварительные требования
- Java 21+
- Maven 3.6+
- Docker & Docker Compose

### 1. Клонирование проекта
```bash
git clone https://github.com/vorqathil/Bank_REST.git
cd Bank_REST
```

### 2. Сборка приложения
```bash
mvn clean package -DskipTests
```

### 3. Запуск с Docker Compose
```bash
docker-compose up --build
```

Приложение будет доступно по адресу: http://localhost:8080

### 4. Документация API
Swagger UI: http://localhost:8080/swagger-ui/index.html

## 🔧 Конфигурация

### База данных
- **Host:** localhost:5433
- **Database:** bank_rest_db
- **Username:** postgres
- **Password:** postgres

##  API эндпоинты

### Аутентификация
```bash
    POST /api/v1/auth/register - Регистрация пользователя
    POST /api/v1/auth/login    - Вход в систему
```

### Карты (пользователи)
```bash
    GET    /api/v1/cards                              - Список карт пользователя
    GET    /api/v1/cards/{cardId}/balance             - Баланс карты
    PUT    /api/v1/cards/{cardId}/block               - Запрос блокировки карты
    PUT    /api/v1/cards/{cardId}/transfer/{cardNumber} - Перевод между картами
```

### Администрирование
```bash
    GET    /api/v1/admin                    - Все карты
    GET    /api/v1/admin/{cardId}           - Карта по ID
    POST   /api/v1/admin/create             - Создание карты
    PUT    /api/v1/admin/{cardId}/activate  - Активация карты
    PUT    /api/v1/admin/{cardId}/block     - Блокировка карты
    DELETE /api/v1/admin/{cardId}           - Удаление карты
    PUT    /api/v1/admin/{username}/make-admin - Назначение роли админа
    PUT    /api/v1/admin/update-expiration  - Обновление истёкших карт
```


##  Безопасность

- JWT токены для аутентификации
- Ролевая авторизация (USER/ADMIN)
- Хеширование паролей (BCrypt)
- Маскирование номеров карт (**** **** **** 1234)
- Валидация входных данных

##  Структура БД

### Пользователи (users)
- id, username, password, role

### Карты (cards)
- id, card_number, masked_card_number, balance, status, validity_period, user_id

### Статусы карт
- PENDING (ожидает активации)
- ACTIVE (активна)
- BLOCKED (заблокирована)
- PENDING_TO_BLOCKING (ожидает блокировки)
- EXPIRED (истёк срок)
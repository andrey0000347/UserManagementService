# User Management Service
Сервис для управления пользователями с поддержкой асинхронных задач,
аналитики и интеграционными тестами.

#  Оглавление
- Технологии

- Функциональность

- Архитектура

- Запуск проекта

- Тестирование

- Структура проекта

- Примеры запросов

## Технологии
Технология	    Версия	    
Spring Boot	    3.2.4	    
Java	        17+	        
PostgreSQL	    15	        
Hibernate	    6.4.4	    
MapStruct	    1.5.5	    
Lombok	        1.18.30	    
Testcontainers	1.19.3	    
JUnit 5	        -	        
AssertJ	        -	        
SLF4J	        -

## Функциональность
Функциональность:
 CRUD операции
- Создание пользователя с валидацией
- Получение всех пользователей
- Получение пользователя по ID
-  Обновление пользователя
- Удаление пользователя

##  Аналитика
 Статистика с оконными функциями (AVG, PERCENT_RANK, NTILE, ROW_NUMBER)

Группировка пользователей по возрастным группам

 Автоматическая простановка времени создания/обновления (@PrePersist, @PreUpdate)

## Технические особенности
 Валидация входных данных

 Global Exception Handler

 Логирование всех операций

 Поддержка Docker + Testcontainers

 Интеграционные тесты

## Архитектура
┌─────────────────────────────────────────────────────────────┐
│                      Presentation Layer                     │
│                     UserController.java                     │
│                    AsyncController.java                     │
└─────────────────────────────┬───────────────────────────────┘
│
┌─────────────────────────────▼───────────────────────────────┐
│                       Business Layer                        │
│                    UserService (Interface)                  │
│                  UserServiceImpl (Implementation)           │
└─────────────────────────────┬───────────────────────────────┘
│
┌─────────────────────────────▼───────────────────────────────┐
│                       Data Access Layer                     │
│                      UserRepository.java                    │
│                     AsyncTaskRepository                     │
└─────────────────────────────┬───────────────────────────────┘
│
┌─────────────────────────────▼───────────────────────────────┐
│                        Database Layer                       │
│                    PostgreSQL (Docker)                      │
└─────────────────────────────────────────────────────────────┘

Поток данных:
Client Request (JSON)
↓
[Controller] → Валидация (@Valid)
↓
[Service] → Бизнес-логика
↓
[Mapper] → Entity ↔ DTO (MapStruct)
↓
[Repository] → Сохранение в БД
↓
[Service] → Маппинг ответа
↓
[Controller] → HTTP Response (JSON)

## Запуск проекта
Предварительные требования
- Java 17+
- Maven 3.6+
- Docker Desktop
- Git

1. Клонирование репозитория
   bash
   git clone https://github.com/andrey0000347/UserManagementService.git
   cd user-management-service
2. Запуск PostgreSQL в Docker
      bash
# Создать и запустить контейнер
docker run --name postgres-db \
-e POSTGRES_USER=api_user \
-e POSTGRES_PASSWORD=api_pass \
-e POSTGRES_DB=api_monitor \
-p 5432:5432 \
-d postgres:15

# Или использовать Docker Compose (рекомендуется)
docker-compose up -d
3. Настройка application.yml
   spring:
   datasource:
   url: jdbc:postgresql://localhost:5432/api_monitor
   username: api_user
   password: api_pass
   driver-class-name: org.postgresql.Driver

jpa:
hibernate:
ddl-auto: create-drop
properties:
hibernate:
dialect: org.hibernate.dialect.PostgreSQLDialect
format_sql: true
show_sql: true

4. Сборка и запуск
# Сборка проекта
bash
mvn clean compile

# Запуск приложения
bash
mvn spring-boot:run

# Сборка JAR файла
mvn clean package
java -jar target/apps-0.0.1-SNAPSHOT.jar

# Тестирование
## Запуск всех тестов
bash
mvn test
## Запуск интеграционных тестов с Testcontainers
bash
mvn test -Dtest=UserServiceIntegrationTest

# Примеры запросов
## Создание пользователя
curl -X POST http://localhost:8080/api/users \
-H "Content-Type: application/json" \
-d '{
"email": "john@example.com",
"password": "password123",
"firstName": "John",
"lastName": "Doe",
"age": 25
}'
## Ответ:

{
"id": 1,
"email": "john@example.com",
"firstName": "John",
"lastName": "Doe",
"age": 25,
"fullName": "John Doe",
"createdAt": "2026-08-06T21:30:00",
"updatedAt": "2026-08-06T21:30:00"
}

## Получение статистики:
bash
curl http://localhost:8080/api/users/statistics
## Ответ:

{
"id": 5,
"email": "charlie@example.com",
"firstName": "Charlie",
"lastName": "Brown",
"age": 60,
"createdAt": "2026-08-06T21:30:00",
"avgAge": 36.4,
"agePercentile": 1.0,
"ageQuartile": 4,
"userNumber": 5
}





# Dine-In Restaurant Ordering and Inventory System

> Phase-1 & Phase-2 Project — Software Engineering Principles and Design Patterns

## Team Members

| Name                | Registration |
|---------------------|--------------|
| Shimul Das          | 2022331016   |
| Md. Farhan Hasin Anik | 2022331024 |
| Syeda Maisha Anika  | 2022331026   |
| Md. Pranta          | 2022331050   |
| Abdullah Al Mamun   | 2022331080   |

---

## Tech Stack

| Component    | Technology                          |
|--------------|-------------------------------------|
| Backend      | Spring Boot 3.2.5 (REST API)        |
| Frontend     | JavaFX 21 (Desktop Client)          |
| Database     | MySQL 9.x                           |
| Java Version | Temurin 21                          |
| Build Tool   | Maven 3.9+                          |
| Auth         | JWT (jjwt 0.12.6)                   |
| ORM          | Spring Data JPA / Hibernate         |

---

## Project Structure

```
restaurant-management-system/
├── backend/               Spring Boot REST API
│   └── src/main/java/com/restaurant/
│       ├── config/        DataSeeder (auto seeds on first run)
│       ├── controller/    REST endpoints (8 controllers)
│       ├── dto/           Request/Response data objects
│       ├── event/         Observer Pattern (LowStockEvent)
│       ├── exception/     Global exception handling
│       ├── model/         JPA entities + enums
│       ├── repository/    Spring Data JPA repositories
│       ├── security/      JWT auth filter + Spring Security config
│       └── service/
│           ├── payment/   Strategy Pattern (CASH/CARD/MOBILE)
│           └── *Service   Business logic (Facade over repositories)
├── frontend/              JavaFX Desktop Client
│   └── src/main/
│       ├── java/com/restaurant/client/
│       │   ├── controller/  JavaFX screen controllers
│       │   ├── model/       DTO classes for JSON mapping
│       │   └── util/        ApiClient, SessionManager (Singleton), AlertUtil
│       └── resources/
│           ├── fxml/        UI layout files (5 screens)
│           └── css/         styles.css
└── database/
    ├── schema.sql         Table creation script
    └── seed.sql           Sample data (alternative to auto-seeder)
```

---

## Design Patterns Applied

| Pattern        | Location                                     | Purpose |
|----------------|----------------------------------------------|---------|
| **Strategy**   | `service/payment/PaymentStrategy` + `PaymentStrategyFactory` | Interchangeable CASH / CARD / MOBILE payment behaviour |
| **Observer**   | `event/LowStockEvent` + `LowStockEventListener` | Notifies when ingredient stock drops below threshold |
| **Factory**    | `BillingService.generateBill()`              | Creates Bill entity from order items (idempotent) |
| **Singleton**  | `client/util/SessionManager`                 | Single session object shared across all JavaFX controllers |
| **Repository** | All `repository/*.java` interfaces           | Abstracts data access behind a clean interface |
| **Facade**     | All `service/*.java` classes                 | Hides repository complexity from controllers |

---

## Architecture (Layered)

```
JavaFX Client (Presentation)
       ↕ HTTP REST / JSON
Spring Boot Controllers (API Layer)
       ↕
Spring Services (Business Logic)
       ↕
Spring Repositories (Data Access)
       ↕
MySQL Database
```

---

## User Roles & Access

| Role        | Screens Accessible                          |
|-------------|---------------------------------------------|
| **Manager** | Menu Management, Inventory, Reports         |
| **Waiter**  | Table selection, Order creation, Mark Served|
| **Kitchen** | Live order queue, Status updates            |
| **Cashier** | Bill view, Payment processing               |

---

## Order Lifecycle

```
PENDING → CONFIRMED → PREPARING → READY → SERVED → PAID
   (Waiter)  (Waiter)   (Kitchen)  (Kitchen) (Waiter) (Cashier)
```

---

## Prerequisites

1. **Java 21** (Temurin / Eclipse Adoptium) — must be on PATH
2. **Maven 3.9+** — must be on PATH
3. **MySQL 9.x** — running on localhost:3306

Verify installations:
```bash
java -version
mvn -version
mysql --version
```

---

## Setup & Run

### Step 1 — Create the MySQL Database

```sql
mysql -u root -p
CREATE DATABASE restaurant_db;
EXIT;
```

Or simply start the backend — Spring Boot will create the database automatically
(`createDatabaseIfNotExist=true` is set in `application.properties`).

### Step 2 — Configure Database Password

Open `backend/src/main/resources/application.properties` and verify:
```properties
spring.datasource.username=root
spring.datasource.password=password
```
Change `password` to your MySQL root password if different.

### Step 3 — Start the Backend

```bash
cd restaurant-management-system/backend
mvn spring-boot:run
```


The backend will:
- Start on `http://localhost:8080`
- Auto-create all tables (Hibernate `ddl-auto=update`)
- Seed default users, tables, menu items, and ingredients on first run

**Default login credentials (all use password `password123`):**

| Username  | Password    | Role    |
|-----------|-------------|---------|
| `manager` | password123 | Manager |
| `waiter`  | password123 | Waiter  |
| `kitchen` | password123 | Kitchen |
| `cashier` | password123 | Cashier |

### Step 4 — Start the Frontend

Open a new terminal:
```bash
cd restaurant-management-system/frontend
mvn javafx:run
```

The login window will appear. Use any of the credentials above.

---

## API Overview

| Method | Endpoint                              | Role        | Description              |
|--------|---------------------------------------|-------------|--------------------------|
| POST   | `/api/auth/login`                     | All         | Login, get JWT token     |
| GET    | `/api/tables`                         | All         | List all tables          |
| GET    | `/api/menu/categories`                | All         | List menu categories     |
| POST   | `/api/menu/categories`                | Manager     | Create category          |
| GET    | `/api/menu/items`                     | All         | List menu items          |
| POST   | `/api/menu/items`                     | Manager     | Create menu item         |
| PUT    | `/api/menu/items/{id}`                | Manager     | Update menu item         |
| POST   | `/api/orders`                         | Waiter      | Create order for table   |
| POST   | `/api/orders/{id}/items`              | Waiter      | Add item to order        |
| PUT    | `/api/orders/{id}/confirm`            | Waiter      | Send order to kitchen    |
| PUT    | `/api/orders/{id}/serve`              | Waiter      | Mark order as served     |
| GET    | `/api/kitchen/queue`                  | Kitchen     | Get active order queue   |
| PUT    | `/api/kitchen/orders/{id}/status`     | Kitchen     | Update order status      |
| GET    | `/api/billing/orders/{id}/bill`       | Cashier     | Get bill for order       |
| POST   | `/api/billing/orders/{id}/pay`        | Cashier     | Process payment          |
| GET    | `/api/inventory/ingredients`          | Manager     | List all ingredients     |
| POST   | `/api/inventory/ingredients/{id}/adjust` | Manager  | Adjust stock (audit log) |
| GET    | `/api/inventory/ingredients/low-stock`| Manager     | Get low-stock alerts     |
| GET    | `/api/reports/sales`                  | Manager     | Sales report by date     |
| GET    | `/api/reports/top-items`              | Manager     | Top-selling items        |

---

## Troubleshooting

**`Communications link failure` (MySQL connection refused)**
- Ensure MySQL is running: `net start MySQL90` (Windows) or `sudo service mysql start`

**`Access denied for user 'root'`**
- Update the password in `application.properties`

**`mvn javafx:run` fails with module error**
- Make sure you are running Java 21: `java -version`
- Run `mvn clean` then `mvn javafx:run` again

**`mysql-connector-j` version conflict**
- If MySQL 9.x has issues with connector 9.0.0, try changing to `8.4.0` in `backend/pom.xml`

**Tables/screens appear blank**
- Ensure the backend is running before starting the frontend
- Check `http://localhost:8080/api/auth/login` in Postman to verify backend health

---

## Key Business Rules

- Waiters can only add/remove items from **PENDING** orders
- Once **CONFIRMED**, the order goes to kitchen and cannot be modified
- Payment is only allowed on **SERVED** orders
- Ingredient deduction is **atomic** — rolls back on failure (NFR-04)
- Manual inventory adjustments are permanently logged (audit log — NFR-06)
- Stock never goes below zero (FR-22)
- All users are pre-created by the system; self-registration is not supported

---

## License

Academic project — Daffodil International University, CSE Department.

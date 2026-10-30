# BidVelocity - Database Configuration & Auto Table Creation Guide

This document details how database tables are automatically generated, where to update your PostgreSQL credentials and database names in `application.properties`, and how starter accounts and catalog lots are seeded.

---

## 1. How Table Auto-Creation Works

Each microservice uses Spring Data JPA with **Hibernate DDL Auto** configured in its `application.properties`:
```properties
spring.jpa.generate-ddl=true
spring.jpa.hibernate.ddl-auto=update
```
- When each microservice boots, Hibernate inspects your entity models (`User`, `Auction`, `Bid`, `Payment`, etc.).
- If tables, primary keys, foreign keys, or indexes do not exist in the database, Hibernate automatically executes the `CREATE TABLE` and constraint statements.
- Existing data is **preserved** across restarts.

---

## 2. Where to Update Database Names, Username, and Password

Each service has its configuration at the very top of `application.properties` under `# 1. DATABASE CONFIGURATION`:

### A. Auth Service (`/backend/auth-service/src/main/resources/application.properties`)
```properties
# 1. DATABASE CONFIGURATION
spring.datasource.url=jdbc:postgresql://localhost:5432/bidvelocity_auth
spring.datasource.username=postgres          # <-- Update your username here
spring.datasource.password=postgrespassword  # <-- Update your password here
spring.datasource.driver-class-name=org.postgresql.Driver
```
*Creates table: `users`*

### B. Auction Service (`/backend/auction-service/src/main/resources/application.properties`)
```properties
# 1. DATABASE CONFIGURATION
spring.datasource.url=jdbc:postgresql://localhost:5432/bidvelocity_auction
spring.datasource.username=postgres          # <-- Update your username here
spring.datasource.password=postgrespassword  # <-- Update your password here
spring.datasource.driver-class-name=org.postgresql.Driver
```
*Creates tables: `auctions`, `processed_bid_requests`*

### C. Bidding Service (`/backend/bidding-service/src/main/resources/application.properties`)
```properties
# 1. DATABASE CONFIGURATION
spring.datasource.url=jdbc:postgresql://localhost:5432/bidvelocity_bidding
spring.datasource.username=postgres          # <-- Update your username here
spring.datasource.password=postgrespassword  # <-- Update your password here
spring.datasource.driver-class-name=org.postgresql.Driver
```
*Creates table: `bids`*

### D. Payment Service (`/backend/payment-service/src/main/resources/application.properties`)
```properties
# 1. DATABASE CONFIGURATION
spring.datasource.url=jdbc:postgresql://localhost:5432/bidvelocity_payment
spring.datasource.username=postgres          # <-- Update your username here
spring.datasource.password=postgrespassword  # <-- Update your password here
spring.datasource.driver-class-name=org.postgresql.Driver
```
*Creates table: `payments`*

> **Tip - Single Shared Database:**
> If you prefer using a single PostgreSQL database rather than 4 separate ones (e.g., `jdbc:postgresql://localhost:5432/bidvelocity`), simply change `spring.datasource.url` in all 4 `application.properties` files to point to the same database name! All tables will coexist in the public schema without conflict.

---

## 3. Creating the Database in PostgreSQL (One-Time Step)

Before running the microservices for the first time, PostgreSQL needs the target database(s) to exist so Spring Boot can connect and run DDL:

### Option 1: 4 Separate Databases (Microservices Standard)
Connect to PostgreSQL (`psql -U postgres`) and run:
```sql
CREATE DATABASE bidvelocity_auth;
CREATE DATABASE bidvelocity_auction;
CREATE DATABASE bidvelocity_bidding;
CREATE DATABASE bidvelocity_payment;
```

### Option 2: 1 Single Database (Fastest Local Setup)
```sql
CREATE DATABASE bidvelocity;
```
*(Then point all 4 `application.properties` files to `.../bidvelocity`)*

---

## 4. Automatic Starter Data Seeding

On startup, if the database tables are empty, Spring Boot `CommandLineRunner` seeders automatically insert initial data:

### Seeded User Accounts:
| Role | Username | Password | Email | Purpose |
| :--- | :--- | :--- | :--- | :--- |
| **Buyer** | `buyer1` | `password123` | `buyer1@bidvelocity.io` | Placing live bids & checkout |
| **Seller** | `seller1` | `sellerpass123` | `seller1@bidvelocity.io` | Managing lots & closing auctions |
| **Admin** | `admin` | `adminpass123` | `admin@bidvelocity.io` | Platform administration |

*(Passwords are securely hashed using BCrypt)*

### Seeded Luxury Lots:
1. **Watches**: 1968 Rolex Submariner Ref. 5513 "Meters First"
2. **Electronics**: Dieter Rams Braun Audio 300 Hi-Fi Console (1969)
3. **Collectibles**: Pierre Jeanneret Floating Back Armchair PJ-SI-28-A
4. **Fine Art**: Brutalist Cast Bronze Kinetic Sculpture by Lynn Chadwick

---

## 5. Starting the Backend Services

Recommended startup order:
1. **PostgreSQL** (Port 5432)
2. **Eureka Server** (`/backend/eureka-server` on Port 8761):
   ```bash
   mvn spring-boot:run
   ```
3. **Auth Service** (`/backend/auth-service` on Port 8081):
   ```bash
   mvn spring-boot:run
   ```
4. **Auction Service** (`/backend/auction-service` on Port 8082):
   ```bash
   mvn spring-boot:run
   ```
5. **Bidding Service** (`/backend/bidding-service` on Port 8083):
   ```bash
   mvn spring-boot:run
   ```
6. **Payment Service** (`/backend/payment-service` on Port 8084):
   ```bash
   mvn spring-boot:run
   ```
7. **API Gateway** (`/backend/api-gateway` on Port 8080):
   ```bash
   mvn spring-boot:run
   ```

Or run everything with Docker:
```bash
docker-compose up -d
```

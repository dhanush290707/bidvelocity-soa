# BidVelocity: Complete Local Microservices Execution Guide

This guide details how to run the entire BidVelocity microservices suite locally on your workstation (`localhost`), including Eureka Service Registry, API Gateway, and the four business domain services.

---

## 📋 Architectural Overview & Local Port Allocations

When running locally, each service binds to `localhost` and registers with the local Eureka server:

| Service | Port | Application Name | Local URL / Health | Purpose |
| :--- | :---: | :--- | :--- | :--- |
| **Eureka Server** | `8761` | `eureka-server` | `http://localhost:8761` | Central Service Registry & Discovery Dashboard |
| **API Gateway** | `8080` | `api-gateway` | `http://localhost:8080` | Reactive Gateway, routing, CORS, and auth filter |
| **Auth Service** | `8081` | `auth-service` | `http://localhost:8081/actuator/health` | JWT generation, buyer/seller registration & login |
| **Auction Service** | `8082` | `auction-service` | `http://localhost:8082/actuator/health` | Catalog, lot lifecycle, consignment & closure |
| **Bidding Service** | `8083` | `bidding-service` | `http://localhost:8083/actuator/health` | High-frequency bids & concurrency protection |
| **Payment Service** | `8084` | `payment-service` | `http://localhost:8084/actuator/health` | Checkout sessions, settlement & webhooks |

---

## 🛠️ Step 1: Local PostgreSQL Setup

The backend uses **Hibernate DDL Auto (`spring.jpa.hibernate.ddl-auto=update`)**, which **automatically generates all tables, columns, indexes, and primary keys** the moment each service starts.

You only need to ensure the PostgreSQL server is running locally on port `5432` and that the databases exist:

### Quick Database Creation
Open `psql` or pgAdmin / DBeaver:

```sql
CREATE DATABASE bidvelocity_auth;
CREATE DATABASE bidvelocity_auction;
CREATE DATABASE bidvelocity_bidding;
CREATE DATABASE bidvelocity_payment;
```

*(Or simply execute the provided `backend/init-databases.sql` script).*

### Default Credentials
Each service's `application.properties` connects with:
- **Host**: `localhost:5432`
- **Username**: `postgres`
- **Password**: `postgrespassword`

If your local PostgreSQL has a different username or password, you can either:
1. Update `spring.datasource.username` and `spring.datasource.password` directly in each `application.properties` file, **OR**
2. Export environment variables in your terminal:
   ```bash
   export POSTGRES_USER=myuser
   export POSTGRES_PASSWORD=mypassword
   ```

---

## ⚡ Option A: Automated One-Click Script (Recommended)

### On macOS / Linux:
```bash
# 1. Make the launcher executable
chmod +x backend/run-local.sh backend/stop-local.sh

# 2. Launch all services
./backend/run-local.sh

# 3. View live logs anytime
tail -f backend/logs/eureka-server.log
tail -f backend/logs/auth-service.log
tail -f backend/logs/auction-service.log

# 4. Stop all running services
./backend/stop-local.sh
```

### On Windows:
Double-click `backend\run-local.bat` or run from Command Prompt / PowerShell:
```cmd
backend\run-local.bat
```
Each service will open in its own labeled terminal window so you can monitor logs in real-time.  
To stop all services, run `backend\stop-local.bat`.

---

## 💻 Option B: Running in IDE (IntelliJ IDEA, Eclipse / STS, VS Code)

If you prefer developing and debugging inside an IDE, import the root `backend/pom.xml` as a Maven Project and start the applications **in this order**:

### Startup Order:
1. **`EurekaServerApplication`** (`eureka-server`)
   - Wait ~10 seconds until console outputs `Started EurekaServerApplication`.
   - Open browser at `http://localhost:8761` to verify Eureka Dashboard is running.

2. **`AuthServiceApplication`** (`auth-service`)
   - Watches database `bidvelocity_auth`, auto-creates `users` table and seeds default accounts.

3. **`AuctionServiceApplication`** (`auction-service`)
   - Watches database `bidvelocity_auction`, auto-creates `auctions` table and seeds starter lots.

4. **`BiddingServiceApplication`** (`bidding-service`)
   - Watches database `bidvelocity_bidding`, auto-creates `bids` table.

5. **`PaymentServiceApplication`** (`payment-service`)
   - Watches database `bidvelocity_payment`, auto-creates `payments` table.

6. **`ApiGatewayApplication`** (`api-gateway`)
   - Connects to Eureka, discovers the 4 domain services, and binds on port `8080`.

---

## 🖥️ Option C: Running Manually via Maven CLI

Open separate terminal tabs and run:

```bash
# Terminal 1 - Eureka Server
cd backend/eureka-server
mvn spring-boot:run

# Terminal 2 - Auth Service
cd backend/auth-service
mvn spring-boot:run

# Terminal 3 - Auction Service
cd backend/auction-service
mvn spring-boot:run

# Terminal 4 - Bidding Service
cd backend/bidding-service
mvn spring-boot:run

# Terminal 5 - Payment Service
cd backend/payment-service
mvn spring-boot:run

# Terminal 6 - API Gateway
cd backend/api-gateway
mvn spring-boot:run
```

---

## 🔍 How to Verify Everything Is Running Locally

### 1. Check Eureka Registry
Navigate to **`http://localhost:8761`** in your browser. Under **"Instances currently registered with Eureka"**, you will see:
- `API-GATEWAY` (port 8080)
- `AUTH-SERVICE` (port 8081)
- `AUCTION-SERVICE` (port 8082)
- `BIDDING-SERVICE` (port 8083)
- `PAYMENT-SERVICE` (port 8084)

Status should read `UP (1) - <hostname>:port`.

### 2. Check Gateway Routing
All traffic should go through the Gateway on port **`8080`**:
- Health check: `curl http://localhost:8080/actuator/health`
- Auction catalog via Gateway: `curl http://localhost:8080/api/auctions`
- Auth login via Gateway:
  ```bash
  curl -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"buyer1@bidvelocity.com","password":"Password123!"}'
  ```

### 3. Connect the Frontend
In your frontend `.env` or configuration, set your API base URL to:
```env
VITE_API_BASE_URL=http://localhost:8080
```
CORS is pre-configured on API Gateway to allow all requests originating from `http://localhost:3000` or `http://localhost:5173`.

---

## ❓ Local Troubleshooting FAQ

- **Port already in use error (`BindException: Address already in use`)**:
  - Run `./backend/stop-local.sh` (or `backend\stop-local.bat` on Windows) to terminate stale processes.
- **`PSQLException: Connection to localhost:5432 refused`**:
  - Verify PostgreSQL service is active: `pg_isready` or check Services in Windows.
  - Verify your password in `application.properties` matches your local PostgreSQL installation.
- **Service not appearing in Eureka**:
  - By default, services take 5–15 seconds to register heartbeat with Eureka.
  - Ensure Eureka is started *before* other services.

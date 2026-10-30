# BidVelocity: Real-Time Distributed Bidding & Auction Clearance Engine

Academic Microservices Architecture Project using **Java 21**, **Spring Boot 3.4.3**, and **Spring Cloud 2024.0.0**.

---

## 1. System Architecture Overview

BidVelocity is designed to satisfy the rigorous requirements of a high-concurrency digital auction platform:
- **High Concurrency Bidding**: Hundreds of bids submitted during closing seconds are handled with `@Version` optimistic locking in isolated transactions, preventing race conditions without blocking listing queries.
- **Strict Database Ownership**: Four isolated PostgreSQL databases (`bidvelocity_auth`, `bidvelocity_auction`, `bidvelocity_bidding`, `bidvelocity_payment`). No cross-database joins or entity sharing.
- **Lean Spring Cloud Ecosystem**: Only the core necessary Spring Cloud modules are used (Netflix Eureka Server for Service Discovery and Reactive Spring Cloud Gateway for Ingress routing).
- **Mock Payment Gateway with UPI QR**: Dynamic checkout URLs (`${CHECKOUT_BASE_URL}/api/payments/mock-gateway/pay?token=...`) embedded in SVG QR codes, constant-time HMAC-SHA256 webhook verification, idempotency keys, and reconciliation retries.

### Microservice Port Map & Database Mapping

| Service Name | Port | Database | Primary Responsibility |
|--------------|------|----------|------------------------|
| `eureka-server` | `8761` | None | Service Registry & Discovery |
| `api-gateway` | `8080` | None | Reactive Ingress, JWT Validation, Header Sanitization, `lb://` Routing |
| `auth-service` | `8081` | `bidvelocity_auth` | User Registration, BCrypt Hashing, Role-Based JWT Issuance |
| `auction-service` | `8082` | `bidvelocity_auction` | Item Listings, Lifecycle State Machine, Atomic Bid Validation (`@Version`) |
| `bidding-service` | `8083` | `bidvelocity_bidding` | High-Concurrency Bid Submissions, Bid Idempotency (`UUID`), Audit History |
| `payment-service` | `8084` | `bidvelocity_payment` | Mock UPI Gateway, QR Checkout, HMAC Webhook, Reconciliation Worker |

---

## 2. Running Locally in Spring Tool Suite (STS) / Eclipse

### Step 1: PostgreSQL Setup
Make sure PostgreSQL is running on `localhost:5432` with username `postgres` and password `postgrespassword` (or customize via environment variables `POSTGRES_USER` and `POSTGRES_PASSWORD`).

Execute the initialization script to create the 4 isolated databases:
```bash
psql -U postgres -f init-databases.sql
```
*(Or use pgAdmin to run the contents of `init-databases.sql`)*.

### Step 2: Import into STS / Eclipse
1. Open Spring Tool Suite (STS) or Eclipse with Spring Tools plugin.
2. Click **File** ➔ **Import...** ➔ **Maven** ➔ **Existing Maven Projects**.
3. Select the `backend` folder as the Root Directory.
4. Click **Finish**. STS will detect `bidvelocity-parent` and all 6 modules.

### Step 3: Run the Microservices in Sequence
Right-click each application main class and choose **Run As ➔ Spring Boot App**:

1. **`eureka-server`**: Run `EurekaServerApplication.java`
   - Wait ~5 seconds. Open browser at `http://localhost:8761` to verify Eureka Dashboard is running.
2. **`auth-service`**: Run `AuthServiceApplication.java` (Port 8081)
3. **`auction-service`**: Run `AuctionServiceApplication.java` (Port 8082)
4. **`bidding-service`**: Run `BiddingServiceApplication.java` (Port 8083)
5. **`payment-service`**: Run `PaymentServiceApplication.java` (Port 8084)
6. **`api-gateway`**: Run `ApiGatewayApplication.java` (Port 8080)

*All services will automatically register with Eureka. Refresh `http://localhost:8761` to see all 5 microservices under "Instances currently registered with Eureka".*

---

## 3. Running with Docker Compose

If you have Docker and Docker Compose installed on your laptop, you can start the entire platform with one command:

```bash
cd backend
docker-compose up --build -d
```

To view logs across all services:
```bash
docker-compose logs -f
```

To stop:
```bash
docker-compose down
```

---

## 4. End-to-End Walkthrough (cURL / Postman)

### Step 1: Register Seller & Buyers
```bash
# Register Seller
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "alice", "email": "alice@auction.com", "password": "password123", "role": "ROLE_SELLER"}'

# Register Buyer
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "bob", "email": "bob@buyer.com", "password": "password123", "role": "ROLE_BUYER"}'
```

### Step 2: Login to Receive JWT
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "bob", "password": "password123"}'
```
*Copy the `token` value from the response.*

### Step 3: Create an Auction (Seller)
```bash
curl -X POST http://localhost:8080/api/auctions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <SELLER_TOKEN>" \
  -d '{
    "itemName": "Antique Pocket Watch 1890",
    "description": "Mint condition gold plated pocket watch",
    "startingPrice": 5000.00,
    "reservePrice": 8000.00,
    "endTime": "2026-12-31T23:59:59"
  }'
```

### Step 4: Place High-Concurrency Bids (Idempotent)
```bash
curl -X POST http://localhost:8080/api/bids \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <BUYER_TOKEN>" \
  -d '{
    "auctionId": 1,
    "bidAmount": 9500.00,
    "bidRequestId": "bid-uuid-001"
  }'
```

### Step 5: Close Auction & Trigger Clearance
```bash
curl -X PUT http://localhost:8080/api/auctions/1/close \
  -H "Authorization: Bearer <SELLER_TOKEN>"
```
*Auction shifts to `PAYMENT_PENDING`, and Payment Service creates an order with a 15-minute checkout token.*

### Step 6: Checkout Details with Mock UPI QR Code
```bash
curl -X GET http://localhost:8080/api/payments/1/checkout
```
*Response contains:
- `checkoutUrl`: `http://localhost:8080/api/payments/mock-gateway/pay?token=...`
- `qrCodeSvg`: Raw vector SVG representing the mock UPI checkout QR code.
- `upiPayUri`: `upi://pay?pa=bidvelocity@upi&pn=BidVelocity&am=9500.00&cu=INR&tn=Auction_1`*

### Step 7: Complete Mock Payment
Open the `checkoutUrl` in your browser (or scan the QR code).
Click **"Simulate Successful Payment"**.
- The Mock Gateway signs the callback with HMAC-SHA256 and calls `POST /api/payments/webhook`.
- Webhook validates the HMAC signature, timestamp drift, amount, and idempotency key.
- Updates payment status to `COMPLETED` and calls `auction-service` to transition the auction to `CLEARED`.

---

## 5. Academic Viva / Defense Q&A

**Q1: Why did you choose a Microservices architecture instead of a Monolith?**
> In a high-traffic auction system, bid ingestion experiences immense traffic spikes during the closing seconds. Separating Bidding Service from Auction Service ensures that heavy write traffic does not degrade listing catalog searches, user authentication, or payment clearance pipelines.

**Q2: How is race condition handled when two users submit bids at the same millisecond?**
> We use `@Version` optimistic locking in the Auction entity. When two bids collide, Spring Data JPA detects the stale version and throws an `ObjectOptimisticLockingFailureException`. The service retries in a fresh transaction (`REQUIRES_NEW`) up to 3 times. If the incoming bid amount is no longer higher than the newly updated highest bid, it fails fast and returns `409 Conflict`.

**Q3: How is Idempotency guaranteed during network retries?**
> Bids require a `bidRequestId` (UUID). It is persisted with a unique constraint in `bidding-service`, and simultaneously recorded in `auction-service` (`processed_bid_requests`) within the same database transaction as the highest-bid update. Any repeated request returns the existing accepted response without side-effects.

**Q4: How do microservices talk to each other without hardcoding IP addresses?**
> All microservices register as Eureka Clients with the Eureka Discovery Server. When Bidding Service or Payment Service makes a REST call, they use `@LoadBalanced RestTemplate` with the logical service name (e.g., `http://AUCTION-SERVICE/...`). Spring Cloud LoadBalancer dynamically resolves the active host and port from Eureka.

**Q5: How is security handled at the API Gateway?**
> The reactive Spring Cloud Gateway runs `JwtAuthGatewayFilter` with high precedence. It strips any incoming untrusted `X-User-*` headers from external callers to prevent identity spoofing. For protected routes, it verifies the Bearer JWT token, parses the claims (`userId`, `role`), and safely injects authenticated `X-User-Id` and `X-User-Role` headers downstream.

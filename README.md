# BidVelocity

BidVelocity is a real-time digital auction marketplace built as a distributed
microservices application. The React frontend provides the public catalogue,
buyer bidding experience, seller consignment dashboard, and mock payment
checkout flow. Spring Boot services provide authentication, auctions, bidding,
payments, service discovery, and API gateway routing.

## Architecture

```text
React + Vite (:3000)
        |
Spring Cloud API Gateway (:8080)
        |
        +--> Auth Service    (:8081) --> bidvelocity_auth
        +--> Auction Service (:8082) --> bidvelocity_auction
        +--> Bidding Service (:8083) --> bidvelocity_bidding
        +--> Payment Service (:8084) --> bidvelocity_payment

Eureka Service Registry (:8761) provides service discovery for the backend.
```

The backend uses Java 21, Spring Boot 3.4, Spring Cloud 2024, PostgreSQL, JPA,
and JWT authentication. Hibernate creates or updates the service tables when
each service starts. The auction service uses optimistic locking for
concurrent bids, and the payment service includes a mock UPI QR checkout and
webhook-signature flow.

## Prerequisites

- Node.js 18+ and npm
- Java 21
- Apache Maven 3.8+
- PostgreSQL 14+ running on `localhost:5432`
- `curl` and `nc` for the local service launcher health checks

## Quick start

### 1. Configure the frontend

From the repository root:

```bash
cp .env.example .env.local
```

The frontend uses `VITE_API_BASE_URL` to reach the gateway and defaults to
`http://localhost:8080`. `GEMINI_API_KEY` and `APP_URL` are retained for AI
Studio deployments; they are not required by the local marketplace client.

Install dependencies and start Vite:

```bash
npm install
npm run dev
```

Open [http://localhost:3000](http://localhost:3000).

### 2. Configure PostgreSQL

Create the four service databases once:

```bash
psql -U postgres -f backend/init-databases.sql
```

The backend launcher reads `backend/.env.local` when it exists. Set the
PostgreSQL connection values there without committing the file:

```dotenv
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your-local-password
```

Each service also supports `POSTGRES_*`, `*_DB_NAME`, and `EUREKA_HOST`
environment-variable overrides. See the service
`application.properties` files for the complete configuration.

### 3. Start the backend

From the repository root:

```bash
./backend/run-local.sh
```

The script starts Eureka first, then the domain services, and finally the API
Gateway. Stop all services with:

```bash
./backend/stop-local.sh
```

On Windows, use `backend/run-local.bat` and `backend/stop-local.bat`.

## Service endpoints

| Component | URL | Purpose |
| --- | --- | --- |
| Frontend | `http://localhost:3000` | React/Vite marketplace |
| API Gateway | `http://localhost:8080` | Frontend entry point for backend APIs |
| Eureka | `http://localhost:8761` | Service registry dashboard |
| Auth | `http://localhost:8081` | Registration, login, and JWT sessions |
| Auction | `http://localhost:8082` | Auction catalogue and lot lifecycle |
| Bidding | `http://localhost:8083` | Bid placement and bid history |
| Payment | `http://localhost:8084` | Checkout, mock payment, and reconciliation |

Use the gateway routes from the frontend:

- `/api/auth/**`
- `/api/auctions/**`
- `/api/bids/**`
- `/api/payments/**`

## Postman testing

Start the backend first, then create a Postman environment with:

```text
baseUrl       http://localhost:8080
sellerToken   (set after seller login)
buyerToken    (set after buyer login)
sellerId      1
buyerId       2
auctionId     (set after creating an auction)
paymentToken  (set after creating checkout)
paymentId     (set after creating checkout)
```

Send `Content-Type: application/json` on requests with JSON bodies. Use
`Authorization: Bearer {{sellerToken}}` or
`Authorization: Bearer {{buyerToken}}` where shown. The gateway validates the
JWT and forwards the authenticated user headers to downstream services.

### 1. Check the gateway and service discovery

```http
GET {{baseUrl}}/actuator/health
```

Expected result: HTTP `200` with an `UP` health response. If the gateway
actuator route is not exposed in your profile, use the public route instead:

```http
GET {{baseUrl}}/api/auctions
```

### 2. Register a buyer or seller

```http
POST {{baseUrl}}/api/auth/register
Content-Type: application/json
```

```json
{
  "username": "postmanbuyer",
  "email": "postmanbuyer@example.com",
  "password": "password123",
  "role": "ROLE_BUYER"
}
```

For a seller, change the username, email, and role to `ROLE_SELLER`.
Expected result: HTTP `201` containing `token`, `userId`, `username`, `email`,
and `role`. Save the returned token as `buyerToken` or `sellerToken`.

Alternatively, use the seeded development accounts:

```http
POST {{baseUrl}}/api/auth/login
Content-Type: application/json
```

```json
{
  "username": "seller1",
  "password": "sellerpass123"
}
```

```json
{
  "username": "buyer1",
  "password": "password123"
}
```

### 3. Validate the JWT session

```http
GET {{baseUrl}}/api/auth/validate
Authorization: Bearer {{buyerToken}}
```

```http
GET {{baseUrl}}/api/auth/me
Authorization: Bearer {{buyerToken}}
```

Expected result: HTTP `200` with the authenticated user's identity and role.
An invalid or expired token should return HTTP `401`.

### 4. Create and view an auction

Create a listing as the seller:

```http
POST {{baseUrl}}/api/auctions
Authorization: Bearer {{sellerToken}}
Content-Type: application/json
```

```json
{
  "itemName": "Postman Test Vintage Camera",
  "description": "A test lot for the BidVelocity auction workflow.",
  "category": "Collectibles",
  "imageUrl": "https://example.com/camera.jpg",
  "conditionReport": "Good working condition",
  "provenance": "Postman integration test",
  "startingPrice": 1000.00,
  "reservePrice": 1500.00,
  "startTime": "2026-10-30T23:00:00",
  "endTime": "2027-12-31T23:59:00"
}
```

Expected result: HTTP `201`. Save the returned `auctionId` as `auctionId`.
`startTime` may be omitted; `endTime` must be a future ISO local date-time.

```http
GET {{baseUrl}}/api/auctions
```

```http
GET {{baseUrl}}/api/auctions?status=ACTIVE
```

```http
GET {{baseUrl}}/api/auctions/{{auctionId}}
```

### 5. Place and inspect bids

Place a valid buyer bid strictly above the current highest bid:

```http
POST {{baseUrl}}/api/bids
Authorization: Bearer {{buyerToken}}
Content-Type: application/json
```

```json
{
  "auctionId": {{auctionId}},
  "bidAmount": 1600.00,
  "bidRequestId": "postman-bid-001"
}
```

Expected result: HTTP `201` with an accepted bid. Resend the exact request to
test idempotency; it should not create a second bid.

```http
GET {{baseUrl}}/api/bids/auction/{{auctionId}}
Authorization: Bearer {{buyerToken}}
```

```http
GET {{baseUrl}}/api/bids/my-bids
Authorization: Bearer {{buyerToken}}
```

### 6. Verify invalid-bid protection

Use the following requests and verify that the service rejects them:

```json
{
  "auctionId": {{auctionId}},
  "bidAmount": 1500.00,
  "bidRequestId": "postman-bid-too-low"
}
```

Expected result: HTTP `409`, because the bid is not strictly above the current
highest bid. A seller attempting to bid on their own auction should receive
HTTP `400`. A bid sent after closure should also be rejected.

For a concurrency test, send several `POST /api/bids` requests at the same
time with different `bidRequestId` values and increasing amounts. The highest
valid bid should win; conflicting requests should receive a controlled `409`
response rather than corrupting the auction state.

### 7. Close the auction and create checkout

Close the active auction after the highest bid meets the reserve price:

```http
PUT {{baseUrl}}/api/auctions/{{auctionId}}/close
Authorization: Bearer {{sellerToken}}
```

Expected result: HTTP `200`. The auction changes to `PAYMENT_PENDING`, and a
payment is initialized for the winning bidder. If the reserve price was not
met, the auction changes to `PAYMENT_FAILED`.

Retrieve the winner's checkout:

```http
GET {{baseUrl}}/api/payments/auction/{{auctionId}}/checkout
Authorization: Bearer {{buyerToken}}
```

Save `checkoutToken` as `paymentToken` and `paymentId` as `paymentId` from the
response. You can also retrieve checkout details directly:

```http
GET {{baseUrl}}/api/payments/{{paymentId}}/checkout
Authorization: Bearer {{buyerToken}}
```

### 8. Simulate successful payment

The mock gateway calculates and verifies the webhook signature server-side:

```http
POST {{baseUrl}}/api/payments/mock-gateway/simulate-submit
Content-Type: application/json
```

```json
{
  "token": "{{paymentToken}}",
  "action": "SUCCESS",
  "transactionRef": "UPI-POSTMAN-001",
  "idempotencyKey": "postman-payment-001"
}
```

Expected result: HTTP `200`, with payment `status` set to `SUCCESS` and a
successful synchronization status. To test failure handling, repeat the
request with a new idempotency key and `"action": "FAILED"`.

```http
GET {{baseUrl}}/api/payments/my-payments
Authorization: Bearer {{buyerToken}}
```

### 9. Test manual reconciliation

```http
POST {{baseUrl}}/api/payments/{{paymentId}}/reconcile-manual
Authorization: Bearer {{buyerToken}}
```

Expected result: HTTP `200` containing `paymentId`, `syncStatus`, and
`retryCount`.

### Optional: direct-service testing

For troubleshooting only, the same service controllers are available directly
on ports `8081` through `8084` using the paths above. Normal application
testing should use `http://localhost:8080` so JWT validation, Eureka
discovery, routing, and inter-service communication are exercised together.

## Starter accounts

On a new authentication database, the auth service seeds these development
accounts:

| Username | Password | Role |
| --- | --- | --- |
| `buyer1` | `password123` | Buyer |
| `seller1` | `sellerpass123` | Seller |
| `admin` | `adminpass123` | Admin |

The auction service also seeds four active catalogue lots when its database is
empty. These accounts and lots are intended for local development only.

## Useful commands

```bash
# Frontend
npm run dev       # Start the Vite development server
npm run build     # Create a production build in dist/
npm run preview   # Preview the production build
npm run lint      # Run the configured TypeScript check

# Backend
cd backend
mvn clean verify  # Compile and verify all Spring Boot modules
docker compose up --build  # Run backend services in containers
```

When using Docker Compose, PostgreSQL is expected to be available from the
host through `host.docker.internal`; ensure Docker Desktop is running and that
`backend/.env.local` contains the database credentials.

## Troubleshooting

- **Gateway offline in the UI:** confirm that Eureka and all backend services
  have started, then retry from the diagnostics banner. Check logs in
  `backend/logs/`.
- **Database connection errors:** verify PostgreSQL is listening on port 5432,
  the four databases exist, and the values in `backend/.env.local` are
  correct.
- **Port conflicts:** stop existing services with
  `./backend/stop-local.sh`, or update the relevant Spring `server.port` and
  frontend API URL together.
- **Authentication problems after changing databases:** delete the old service
  data or use a fresh database so the development seed accounts can be
  created.

## Project layout

```text
src/                         React frontend and API client
backend/api-gateway/         Spring Cloud Gateway
backend/auth-service/        Registration, login, and JWT security
backend/auction-service/     Auction catalogue and closing logic
backend/bidding-service/     Bid processing and history
backend/payment-service/     Checkout and mock payment gateway
backend/eureka-server/       Service discovery
backend/init-databases.sql   PostgreSQL database initialization
backend/run-local.sh         macOS/Linux launcher
backend/docker-compose.yml   Containerized backend definition
```

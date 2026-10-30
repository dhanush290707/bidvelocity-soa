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

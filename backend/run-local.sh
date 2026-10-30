#!/usr/bin/env bash
# ==============================================================================
# BIDVELOCITY - LOCAL MICROSERVICES LAUNCHER (macOS / Linux)
# ==============================================================================
# Starts Eureka Server first, waits for health check, then boots all services:
# - Eureka Server  (Port 8761) -> http://localhost:8761
# - Auth Service   (Port 8081) -> http://localhost:8081
# - Auction Service(Port 8082) -> http://localhost:8082
# - Bidding Service(Port 8083) -> http://localhost:8083
# - Payment Service(Port 8084) -> http://localhost:8084
# - API Gateway    (Port 8080) -> http://localhost:8080
# ==============================================================================

set -e

# Change to the backend directory where this script lives
cd "$(dirname "$0")"
SCRIPT_DIR="$(pwd)"
LOG_DIR="$SCRIPT_DIR/logs"
PID_FILE="$SCRIPT_DIR/.pids"

# Load local, uncommitted service settings when present. This keeps database
# credentials out of application.properties while making the launcher usable
# without exporting variables in every terminal session.
ENV_FILE="$SCRIPT_DIR/.env.local"
if [ -f "$ENV_FILE" ]; then
    set -a
    # shellcheck source=/dev/null
    . "$ENV_FILE"
    set +a
fi

mkdir -p "$LOG_DIR"
rm -f "$PID_FILE"
touch "$PID_FILE"

echo "================================================================="
echo "   🚀 STARTING BIDVELOCITY LOCAL MICROSERVICES ECOSYSTEM"
echo "================================================================="

# 1. Prerequisite checks
if ! command -v java &> /dev/null; then
    echo "❌ Error: 'java' command not found. Please install Java 17+ or Java 21."
    exit 1
fi

if ! command -v mvn &> /dev/null; then
    echo "❌ Error: 'mvn' command not found. Please install Apache Maven 3.8+."
    exit 1
fi

echo "✔ Java runtime: $(java -version 2>&1 | head -n 1)"
echo "✔ Maven version: $(mvn -version 2>&1 | head -n 1)"

# Check if PostgreSQL is reachable locally
if command -v nc &> /dev/null; then
    if nc -z localhost 5432 2>/dev/null; then
        echo "✔ PostgreSQL detected running on localhost:5432"
    else
        echo "⚠️ Warning: Could not detect PostgreSQL on localhost:5432."
        echo "   Ensure local PostgreSQL is started and databases are created."
    fi
fi

# Function to launch a module in background
launch_service() {
    local service_name="$1"
    local dir_name="$2"
    local port="$3"

    echo "▶ Starting $service_name on port $port..."
    (
        cd "$SCRIPT_DIR/$dir_name"
        mvn spring-boot:run > "$LOG_DIR/$service_name.log" 2>&1
    ) &
    local pid=$!
    echo "$service_name:$pid" >> "$PID_FILE"
    echo "  → $service_name running (PID: $pid). Logs: backend/logs/$service_name.log"
}

# 2. Start Eureka Server FIRST
echo ""
echo "-----------------------------------------------------------------"
echo "Step 1/3: Starting Eureka Service Registry (Port 8761)..."
echo "-----------------------------------------------------------------"
launch_service "eureka-server" "eureka-server" 8761

# Wait for Eureka to be healthy
echo "⏳ Waiting for Eureka Server to initialize at http://localhost:8761..."
EUREKA_READY=0
for i in {1..35}; do
    if curl -s -f http://localhost:8761 > /dev/null 2>&1 || curl -s -f http://localhost:8761/actuator/health > /dev/null 2>&1; then
        EUREKA_READY=1
        break
    fi
    sleep 2
    printf "."
done
echo ""

if [ $EUREKA_READY -eq 1 ]; then
    echo "✔ Eureka Server is UP and healthy at http://localhost:8761"
else
    echo "⚠️ Eureka startup took longer than expected. Continuing launch sequence..."
fi

# 3. Start Business Domain Services
echo ""
echo "-----------------------------------------------------------------"
echo "Step 2/3: Starting Core Domain Services (Auth, Auction, Bidding, Payment)..."
echo "-----------------------------------------------------------------"
launch_service "auth-service" "auth-service" 8081
sleep 2
launch_service "auction-service" "auction-service" 8082
sleep 2
launch_service "bidding-service" "bidding-service" 8083
sleep 2
launch_service "payment-service" "payment-service" 8084

# 4. Start Reactive API Gateway
echo ""
echo "-----------------------------------------------------------------"
echo "Step 3/3: Starting Reactive API Gateway (Port 8080)..."
echo "-----------------------------------------------------------------"
sleep 3
launch_service "api-gateway" "api-gateway" 8080

echo ""
echo "================================================================="
echo "   🎉 ALL SERVICES LAUNCHED SUCCESSFULLY IN LOCAL MODE"
echo "================================================================="
echo "Service Endpoints:"
echo " • Eureka Dashboard : http://localhost:8761"
echo " • API Gateway      : http://localhost:8080 (Primary entry point)"
echo " • Auth Service     : http://localhost:8081"
echo " • Auction Service  : http://localhost:8082"
echo " • Bidding Service  : http://localhost:8083"
echo " • Payment Service  : http://localhost:8084"
echo ""
echo "Monitoring & Management:"
echo " • Real-time Logs   : tail -f backend/logs/*.log"
echo " • Stop All Services: ./backend/stop-local.sh"
echo "================================================================="

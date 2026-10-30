#!/usr/bin/env bash
# ==============================================================================
# BIDVELOCITY - LOCAL MICROSERVICES STOPPER (macOS / Linux)
# ==============================================================================
# Gracefully stops all microservices and Eureka server started by run-local.sh
# ==============================================================================

cd "$(dirname "$0")"
SCRIPT_DIR="$(pwd)"
PID_FILE="$SCRIPT_DIR/.pids"

echo "================================================================="
echo "   🛑 STOPPING BIDVELOCITY LOCAL MICROSERVICES"
echo "================================================================="

if [ -f "$PID_FILE" ]; then
    while IFS=":" read -r service pid; do
        if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
            echo "Stopping $service (PID: $pid)..."
            kill "$pid" 2>/dev/null || true
        else
            echo "$service (PID: $pid) is already stopped."
        fi
    done < "$PID_FILE"
    rm -f "$PID_FILE"
else
    echo "No .pids file found. Searching for running Spring Boot Java processes by port..."
fi

# Fallback check by ports: 8761, 8080, 8081, 8082, 8083, 8084
PORTS=(8761 8080 8081 8082 8083 8084)
for port in "${PORTS[@]}"; do
    if command -v lsof &> /dev/null; then
        PORT_PID=$(lsof -ti tcp:"$port" 2>/dev/null || true)
        if [ -n "$PORT_PID" ]; then
            echo "Port $port occupied by PID $PORT_PID. Stopping..."
            kill "$PORT_PID" 2>/dev/null || true
        fi
    fi
done

echo ""
echo "✔ All local microservice processes have been terminated."
echo "================================================================="

@echo off
REM ==============================================================================
REM BIDVELOCITY - LOCAL MICROSERVICES LAUNCHER (Windows)
REM ==============================================================================
REM Starts Eureka Server, API Gateway, and all 4 domain services in separate console windows.
REM ==============================================================================

setlocal enabledelayedexpansion

echo =================================================================
echo    STARTING BIDVELOCITY LOCAL MICROSERVICES ECOSYSTEM (WINDOWS)
echo =================================================================

REM Check Java
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] 'java' command not found. Please install Java 17+ or Java 21.
    pause
    exit /b 1
)

REM Check Maven
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] 'mvn' command not found. Please install Maven 3.8+ and add to PATH.
    pause
    exit /b 1
)

set SCRIPT_DIR=%~dp0

echo 1. Launching Eureka Service Discovery (Port 8761)...
start "Eureka-Server (8761)" cmd /k "cd /d %SCRIPT_DIR%eureka-server && mvn spring-boot:run"

echo Waiting 15 seconds for Eureka Server to initialize...
timeout /t 15 /nobreak >nul

echo 2. Launching Auth Service (Port 8081)...
start "Auth-Service (8081)" cmd /k "cd /d %SCRIPT_DIR%auth-service && mvn spring-boot:run"

echo 3. Launching Auction Service (Port 8082)...
start "Auction-Service (8082)" cmd /k "cd /d %SCRIPT_DIR%auction-service && mvn spring-boot:run"

echo 4. Launching Bidding Service (Port 8083)...
start "Bidding-Service (8083)" cmd /k "cd /d %SCRIPT_DIR%bidding-service && mvn spring-boot:run"

echo 5. Launching Payment Service (Port 8084)...
start "Payment-Service (8084)" cmd /k "cd /d %SCRIPT_DIR%payment-service && mvn spring-boot:run"

echo Waiting 5 seconds before launching Gateway...
timeout /t 5 /nobreak >nul

echo 6. Launching API Gateway (Port 8080)...
start "API-Gateway (8080)" cmd /k "cd /d %SCRIPT_DIR%api-gateway && mvn spring-boot:run"

echo =================================================================
echo   ALL SERVICES ARE LAUNCHING IN INDIVIDUAL TERMINAL WINDOWS
echo =================================================================
echo  Eureka Dashboard : http://localhost:8761
echo  API Gateway      : http://localhost:8080
echo  Auth Service     : http://localhost:8081
echo  Auction Service  : http://localhost:8082
echo  Bidding Service  : http://localhost:8083
echo  Payment Service  : http://localhost:8084
echo =================================================================

@echo off
REM ==============================================================================
REM BIDVELOCITY - LOCAL MICROSERVICES STOPPER (Windows)
REM ==============================================================================
REM Stops Spring Boot processes listening on ports 8761, 8080, 8081, 8082, 8083, 8084
REM ==============================================================================

echo =================================================================
echo   STOPPING BIDVELOCITY LOCAL MICROSERVICES (WINDOWS)
echo =================================================================

set PORTS=8761 8080 8081 8082 8083 8084

for %%P in (%PORTS%) do (
    for /f "tokens=5" %%a in ('netstat -aon ^| findstr :%%P') do (
        if "%%a" neq "0" (
            echo Terminating process %%a on port %%P...
            taskkill /F /PID %%a >nul 2>&1
        )
    )
)

echo.
echo All microservice processes on ports 8761, 8080, 8081, 8082, 8083, 8084 stopped.
echo =================================================================

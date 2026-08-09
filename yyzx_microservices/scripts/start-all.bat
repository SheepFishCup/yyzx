@echo off
echo ========================================
echo   Starting all yyzx microservices...
echo ========================================
echo.

cd /d "%~dp0.."

REM ---- Step 1: yyzx-notification (RabbitMQ queues need to be declared first) ----
echo [1/10] yyzx-notification :8087
start "yyzx-notification" cmd /c "scripts\start-notification.bat"
timeout /t 8 /nobreak >nul

REM ---- Step 2: yyzx-auth (auth needed by other services) ----
echo [2/10] yyzx-auth :8081
start "yyzx-auth" cmd /c "scripts\start-auth.bat"
timeout /t 10 /nobreak >nul

REM ---- Step 3~8: Business services (parallel-friendly, sequential for safety) ----
echo [3/10] yyzx-customer :8082
start "yyzx-customer" cmd /c "scripts\start-customer.bat"
timeout /t 8 /nobreak >nul

echo [4/10] yyzx-bed :8083
start "yyzx-bed" cmd /c "scripts\start-bed.bat"
timeout /t 6 /nobreak >nul

echo [5/10] yyzx-nursing :8084
start "yyzx-nursing" cmd /c "scripts\start-nursing.bat"
timeout /t 6 /nobreak >nul

echo [6/10] yyzx-checkinout :8085
start "yyzx-checkinout" cmd /c "scripts\start-checkinout.bat"
timeout /t 6 /nobreak >nul

echo [7/10] yyzx-meal :8086
start "yyzx-meal" cmd /c "scripts\start-meal.bat"
timeout /t 6 /nobreak >nul

echo [8/10] yyzx-report :8088
start "yyzx-report" cmd /c "scripts\start-report.bat"
timeout /t 6 /nobreak >nul

echo [9/10] yyzx-task :8089
start "yyzx-task" cmd /c "scripts\start-task.bat"
timeout /t 6 /nobreak >nul

REM ---- Step 10: yyzx-gateway (last, unified entry point) ----
echo [10/10] yyzx-gateway :8080
start "yyzx-gateway" cmd /c "scripts\start-gateway.bat"

echo.
echo ========================================
echo   All services launching in background.
echo   Check each window for "Started" log.
echo ========================================
echo.
echo   Health check (wait 60s):
echo     curl http://localhost:8080/admin/generate
echo ========================================
pause

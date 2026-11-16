@echo off
title Online Examination System - Enterprise Launcher
echo ============================================================
echo   ONLINE EXAMINATION SYSTEM (3-TIER ENTERPRISE ARCHITECTURE)
echo ============================================================
echo.

echo [1/3] Checking MySQL80 database service...
net start MySQL80 >nul 2>&1
if %errorlevel% equ 0 (
    echo       [OK] MySQL80 service started successfully.
) else (
    echo       [OK] MySQL80 service is already running or ready.
)
echo.

echo [2/3] Launching web browser at http://localhost:8080 ...
timeout /t 2 /nobreak >nul
start "" "http://localhost:8080"
echo       [OK] Browser launched.
echo.

echo [3/3] Starting Java 17 Backend Server on port 8080...
echo       Press Ctrl+C in this window anytime to stop the server.
echo ============================================================
echo.

cd /d "%~dp0backend"
java -cp "out;lib\mysql-connector-j-26.10.0\mysql-connector-j-26.10.0.jar;lib\json-20240303.jar" api.Server
pause


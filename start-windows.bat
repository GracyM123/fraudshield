@echo off
echo.
echo ============================================
echo   FraudShield - Windows Quick Start
echo ============================================
echo.

REM Step 1: Check Docker is running
echo [1/4] Checking Docker...
docker info >nul 2>&1
if errorlevel 1 (
    echo ERROR: Docker is not running!
    echo Please open Docker Desktop and wait for it to show "Engine running"
    echo Then run this script again.
    pause
    exit /b 1
)
echo Docker is running OK

REM Step 2: Check Node is installed
echo [2/4] Checking Node.js...
node --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Node.js is not installed!
    echo Please download from nodejs.org and install it, then run this script again.
    pause
    exit /b 1
)
echo Node.js found

REM Step 3: Build frontend locally (fast - no Docker needed)
echo [3/4] Building frontend (this takes 2-3 minutes)...
cd frontend
call npm install --silent
if errorlevel 1 (
    echo ERROR: npm install failed. Check your internet connection.
    pause
    exit /b 1
)
call npm run build
if errorlevel 1 (
    echo ERROR: Frontend build failed.
    pause
    exit /b 1
)
cd ..
echo Frontend built successfully!

REM Step 4: Start with Docker Compose
echo [4/4] Starting all services...
echo.
echo NOTE: First time starting the backend will take 5-10 minutes
echo      (Maven downloads Java libraries)
echo      Subsequent starts will be much faster.
echo.
docker compose up --build


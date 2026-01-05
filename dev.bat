@echo off
REM Expense Tracker - Windows Development Scripts

if "%1"=="" goto help
if "%1"=="help" goto help
if "%1"=="install" goto install
if "%1"=="dev-backend" goto dev-backend
if "%1"=="dev-frontend" goto dev-frontend
if "%1"=="docker-up" goto docker-up
if "%1"=="docker-down" goto docker-down
if "%1"=="docker-restart" goto docker-restart
if "%1"=="docker-logs" goto docker-logs
if "%1"=="docker-build" goto docker-build
if "%1"=="docker-clean" goto docker-clean
if "%1"=="test-backend" goto test-backend
if "%1"=="test-frontend" goto test-frontend
if "%1"=="clean" goto clean
goto help

:help
echo Expense Tracker - Available Commands
echo ====================================
echo Development:
echo   dev.bat install        - Install all dependencies
echo   dev.bat dev-backend    - Start backend only (Spring Boot)
echo   dev.bat dev-frontend   - Start frontend only (Vite)
echo.
echo Docker:
echo   dev.bat docker-up      - Start all services with docker-compose
echo   dev.bat docker-down    - Stop all services
echo   dev.bat docker-restart - Restart all services
echo   dev.bat docker-logs    - View logs from all services
echo   dev.bat docker-build   - Build docker images
echo   dev.bat docker-clean   - Stop services and remove volumes
echo.
echo Testing:
echo   dev.bat test-backend   - Run backend tests
echo   dev.bat test-frontend  - Run frontend tests
echo.
echo Cleanup:
echo   dev.bat clean          - Clean build artifacts
goto end

:install
echo Installing backend dependencies...
call mvn clean install -DskipTests
echo Installing frontend dependencies...
cd frontend
call npm install
cd ..
echo Dependencies installed successfully!
goto end

:dev-backend
echo Starting Spring Boot backend on port 8080...
call mvn spring-boot:run
goto end

:dev-frontend
echo Starting Vite frontend on port 5173...
cd frontend
call npm run dev
goto end

:docker-up
echo Starting all services with docker-compose...
docker-compose up -d
echo Services started! Frontend: http://localhost, Backend: http://localhost:8080
goto end

:docker-down
echo Stopping all services...
docker-compose down
goto end

:docker-restart
echo Restarting all services...
docker-compose restart
goto end

:docker-logs
echo Showing logs from all services...
docker-compose logs -f
goto end

:docker-build
echo Building docker images...
docker-compose build
goto end

:docker-clean
echo Stopping services and removing volumes...
docker-compose down -v
echo Cleaned up!
goto end

:test-backend
echo Running backend tests...
call mvn test
goto end

:test-frontend
echo Running frontend tests...
cd frontend
call npm test
goto end

:clean
echo Cleaning backend build artifacts...
call mvn clean
echo Cleaning frontend build artifacts...
cd frontend
if exist dist rmdir /s /q dist
if exist node_modules\.vite rmdir /s /q node_modules\.vite
cd ..
echo Cleanup complete!
goto end

:end

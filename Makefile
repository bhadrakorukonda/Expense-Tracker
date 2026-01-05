# Expense Tracker - Development Commands

.PHONY: help install dev dev-backend dev-frontend docker-up docker-down docker-restart docker-logs docker-build clean test

# Default target
help:
	@echo "Expense Tracker - Available Commands"
	@echo "===================================="
	@echo "Development:"
	@echo "  make install        - Install all dependencies (backend + frontend)"
	@echo "  make dev            - Start backend and frontend in dev mode"
	@echo "  make dev-backend    - Start backend only (Spring Boot)"
	@echo "  make dev-frontend   - Start frontend only (Vite)"
	@echo ""
	@echo "Docker:"
	@echo "  make docker-up      - Start all services with docker-compose"
	@echo "  make docker-down    - Stop all services"
	@echo "  make docker-restart - Restart all services"
	@echo "  make docker-logs    - View logs from all services"
	@echo "  make docker-build   - Build docker images"
	@echo "  make docker-clean   - Stop services and remove volumes"
	@echo ""
	@echo "Testing:"
	@echo "  make test           - Run all tests"
	@echo "  make test-backend   - Run backend tests"
	@echo "  make test-frontend  - Run frontend tests"
	@echo ""
	@echo "Cleanup:"
	@echo "  make clean          - Clean build artifacts"

# Install dependencies
install:
	@echo "Installing backend dependencies..."
	@cd . && mvn clean install -DskipTests
	@echo "Installing frontend dependencies..."
	@cd frontend && npm install
	@echo "Dependencies installed successfully!"

# Start both backend and frontend in dev mode (requires two terminals)
dev:
	@echo "Starting development servers..."
	@echo "Note: This requires two terminal windows"
	@echo "Run 'make dev-backend' in one terminal and 'make dev-frontend' in another"

# Start backend in development mode
dev-backend:
	@echo "Starting Spring Boot backend on port 8080..."
	@mvn spring-boot:run

# Start frontend in development mode
dev-frontend:
	@echo "Starting Vite frontend on port 5173..."
	@cd frontend && npm run dev

# Docker commands
docker-up:
	@echo "Starting all services with docker-compose..."
	@docker-compose up -d
	@echo "Services started! Frontend: http://localhost, Backend: http://localhost:8080"

docker-down:
	@echo "Stopping all services..."
	@docker-compose down

docker-restart:
	@echo "Restarting all services..."
	@docker-compose restart

docker-logs:
	@echo "Showing logs from all services..."
	@docker-compose logs -f

docker-build:
	@echo "Building docker images..."
	@docker-compose build

docker-clean:
	@echo "Stopping services and removing volumes..."
	@docker-compose down -v
	@echo "Cleaned up!"

# Testing
test: test-backend test-frontend

test-backend:
	@echo "Running backend tests..."
	@mvn test

test-frontend:
	@echo "Running frontend tests..."
	@cd frontend && npm test

# Clean build artifacts
clean:
	@echo "Cleaning backend build artifacts..."
	@mvn clean
	@echo "Cleaning frontend build artifacts..."
	@cd frontend && rm -rf dist node_modules/.vite
	@echo "Cleanup complete!"

# Expense Tracker 💰

A comprehensive full-stack expense tracking application built with **Spring Boot 3** and **React**, featuring JWT authentication, MongoDB GridFS for receipts, CSV export, and reporting capabilities.

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19.2.3-blue.svg)](https://reactjs.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![MongoDB](https://img.shields.io/badge/MongoDB-7-green.svg)](https://www.mongodb.com/)

## 📑 Table of Contents

- [Features](#-features)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Prerequisites](#-prerequisites)
- [Quick Start](#-quick-start)
- [Running Locally](#-running-locally)
- [Docker Deployment](#-docker-deployment)
- [Environment Variables](#-environment-variables)
- [API Documentation](#-api-documentation)
- [Testing](#-testing)
- [Development](#-development)
- [Project Structure](#-project-structure)
- [Contributing](#-contributing)
- [License](#-license)

## ✨ Features

### Backend (Spring Boot)
- 🔐 **JWT Authentication** - Secure user authentication and authorization
- 💳 **Expense Management** - Full CRUD operations with advanced filtering
- 📊 **Reports & Analytics** - Monthly and category-based expense reports
- 📁 **Receipt Management** - MongoDB GridFS integration for file storage
- 📤 **CSV Export** - Stream large datasets with memory efficiency
- 🔍 **Dynamic Search** - JPA Specifications with 9 filter types
- 🗄️ **Database Migrations** - Flyway for version-controlled schema changes
- ✅ **Comprehensive Testing** - Unit tests (JUnit 5 + Mockito) and integration tests (Testcontainers)
- 📝 **API Documentation** - Swagger/OpenAPI 3.0 integration
- 🏥 **Health Checks** - Spring Boot Actuator endpoints

### Frontend (React + TypeScript)
- ⚛️ **React 19** with TypeScript for type safety
- 🎨 **TailwindCSS** for modern, responsive UI
- 📈 **Recharts** for data visualization
- 🔐 **JWT Token Management** with axios interceptors
- 📱 **Responsive Design** - Mobile-first approach
- 🧪 **Unit Tests** - Vitest + React Testing Library (76 tests)

### DevOps
- 🐳 **Docker Support** - Multi-stage builds for optimized images
- 📦 **Docker Compose** - Complete stack orchestration
- 🔄 **GitHub Actions** - CI/CD pipeline with automated testing
- 🌱 **Data Seeding** - Sample data for local development

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                            │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  React 19 + TypeScript + TailwindCSS                     │   │
│  │  - Expense Forms  - Dashboard  - Charts  - Receipt UI   │   │
│  └──────────────────────────────────────────────────────────┘   │
└────────────────────────┬────────────────────────────────────────┘
                         │ HTTPS / JWT
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                      API GATEWAY / NGINX                        │
│                    Reverse Proxy + SSL                          │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                    BACKEND - Spring Boot 3                      │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                    SECURITY LAYER                        │   │
│  │   JWT Authentication Filter + Spring Security            │   │
│  └─────────────────────────┬────────────────────────────────┘   │
│                            │                                     │
│  ┌─────────────────────────▼────────────────────────────────┐   │
│  │                  CONTROLLER LAYER                        │   │
│  │  - AuthController  - ExpenseController                   │   │
│  │  - ReceiptController  - ReportController                 │   │
│  └─────────────────────────┬────────────────────────────────┘   │
│                            │                                     │
│  ┌─────────────────────────▼────────────────────────────────┐   │
│  │                   SERVICE LAYER                          │   │
│  │  Business Logic + Validation + Transaction Management    │   │
│  └─────────────────────────┬────────────────────────────────┘   │
│                            │                                     │
│  ┌─────────────────────────▼────────────────────────────────┐   │
│  │                 REPOSITORY LAYER                         │   │
│  │  JPA Repositories + Custom Queries + Specifications      │   │
│  └──────────────┬──────────────────────┬────────────────────┘   │
└─────────────────┼──────────────────────┼─────────────────────────┘
                  │                      │
                  ▼                      ▼
    ┌─────────────────────┐   ┌────────────────────┐
    │   PostgreSQL 16     │   │    MongoDB 7       │
    │  ┌───────────────┐  │   │  ┌──────────────┐  │
    │  │ Users         │  │   │  │ GridFS       │  │
    │  │ Categories    │  │   │  │ (Receipts)   │  │
    │  │ Expenses      │  │   │  │              │  │
    │  │ Expense Tags  │  │   │  └──────────────┘  │
    │  └───────────────┘  │   └────────────────────┘
    │  Relational Data    │     Binary File Storage
    └─────────────────────┘
```

### Data Flow

```
1. Authentication Flow:
   User → Login Request → JWT Token Generation → Secure Storage → API Requests

2. Expense Creation:
   User Input → Validation → Category Check → Database Insert → Response

3. Receipt Upload:
   File → MultipartFile → GridFS → MongoDB → Receipt ID → Link to Expense

4. Report Generation:
   Request → Filters → Repository Aggregation → Business Logic → JSON Response

5. CSV Export:
   Request → Query Builder → Stream Processing → CSV Writer → Download
```

## 🛠️ Tech Stack

### Backend
- **Java 21** (LTS) - Latest long-term support version
- **Spring Boot 3.2.1** - Application framework
- **Spring Security** - Authentication & authorization
- **Spring Data JPA** - Database abstraction
- **Spring Data MongoDB** - MongoDB integration
- **PostgreSQL 16** - Primary database
- **MongoDB 7** - Receipt storage (GridFS)
- **Flyway** - Database migrations
- **MapStruct 1.5.5** - Object mapping
- **JJWT 0.12.5** - JWT token handling
- **Lombok** - Boilerplate reduction
- **SpringDoc OpenAPI** - API documentation
- **JUnit 5** - Unit testing
- **Mockito** - Mocking framework
- **Testcontainers 1.19.3** - Integration testing

### Frontend
- **React 19.2.3** - UI library
- **TypeScript 5.9.3** - Type safety
- **Vite 7.2.5** - Build tool
- **React Router 7.11.0** - Navigation
- **Axios 1.13.2** - HTTP client
- **TailwindCSS 4.1.18** - Styling
- **Recharts 3.6.0** - Charts & visualization
- **React Hook Form 7.69.0** - Form management
- **Vitest 2.1.8** - Unit testing
- **React Testing Library 16.1.0** - Component testing

### DevOps
- **Docker** - Containerization
- **Docker Compose** - Multi-container orchestration
- **Nginx** - Reverse proxy & static serving
- **GitHub Actions** - CI/CD
- **Maven 3.9.11** - Build automation

## 📋 Prerequisites

### For Local Development
- **Java 21** or higher ([Download](https://adoptium.net/))
- **Maven 3.9+** ([Download](https://maven.apache.org/download.cgi))
- **Node.js 20+** and npm ([Download](https://nodejs.org/))
- **PostgreSQL 16+** ([Download](https://www.postgresql.org/download/))
- **MongoDB 7+** (Optional for receipts) ([Download](https://www.mongodb.com/try/download/community))

### For Docker Deployment
- **Docker 24+** ([Download](https://www.docker.com/get-started))
- **Docker Compose 2.20+** (Included with Docker Desktop)

## 🚀 Quick Start

### Option 1: Docker (Recommended)

```bash
# Clone the repository
git clone https://github.com/yourusername/expense-tracker.git
cd expense-tracker

# Copy environment file
cp .env.example .env

# Start all services
docker-compose up -d

# View logs
docker-compose logs -f backend

# Access the application
# Frontend: http://localhost:3000
# Backend: http://localhost:8080
# API Docs: http://localhost:8080/swagger-ui.html
```

### Option 2: Local Development

```bash
# 1. Start databases (PostgreSQL + MongoDB)
docker-compose up -d postgres mongo

# 2. Create database
psql -U postgres -c "CREATE DATABASE expense_tracker;"

# 3. Set environment variables (or use application.properties)
export DATABASE_URL=jdbc:postgresql://localhost:5432/expense_tracker
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=postgres
export JWT_SECRET=YourSecretKey123

# 4. Build and run backend
cd expense-tracker
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 5. In a new terminal, run frontend
cd frontend
npm install
npm run dev

# Access: http://localhost:5173
```

## 💻 Running Locally

### Backend Setup

1. **Create PostgreSQL Database**
```sql
CREATE DATABASE expense_tracker;
```

2. **Configure Application**

Edit `src/main/resources/application.properties` or set environment variables:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/expense_tracker
spring.datasource.username=postgres
spring.datasource.password=postgres

# MongoDB (optional)
spring.data.mongodb.uri=mongodb://localhost:27017/expense_tracker_logs

# JWT
jwt.secret=YourSecretKey123
jwt.expiration=86400000

# Enable dev profile for sample data
spring.profiles.active=dev
```

3. **Build and Run**

```bash
# Compile
mvn clean compile

# Run tests
mvn test

# Package
mvn package -DskipTests

# Run
mvn spring-boot:run

# Or run the JAR
java -jar target/expense-tracker-1.0.0.jar
```

4. **Verify Backend**
```bash
# Health check
curl http://localhost:8080/actuator/health

# Should return: {"status":"UP"}
```

### Frontend Setup

1. **Install Dependencies**
```bash
cd frontend
npm install
```

2. **Configure Environment**

Create `frontend/.env`:
```env
VITE_API_URL=http://localhost:8080/api/v1
```

3. **Run Development Server**
```bash
npm run dev
```

4. **Build for Production**
```bash
npm run build
npm run preview
```

5. **Run Tests**
```bash
npm test
npm run test:coverage
```

### Development with Sample Data

Enable the `dev` profile to seed the database with sample data:

**Method 1: application.properties**
```properties
spring.profiles.active=dev
```

**Method 2: Command Line**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Method 3: Environment Variable**
```bash
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run
```

**Sample Users Created:**
- `john.doe@example.com` / `password123`
- `jane.smith@example.com` / `password123`
- `bob.wilson@example.com` / `password123`

See [DATA_SEEDER.md](DATA_SEEDER.md) for details.

## 🐳 Docker Deployment

### Using Docker Compose (Full Stack)

1. **Create Environment File**
```bash
cp .env.example .env
```

Edit `.env` with your configuration:
```env
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your_secure_password
MONGODB_USERNAME=admin
MONGODB_PASSWORD=your_mongo_password
JWT_SECRET=YourVerySecretJWTKey123456789
SPRING_PROFILES_ACTIVE=prod
```

2. **Start All Services**
```bash
# Build and start
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

3. **Access Services**
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- API Documentation: http://localhost:8080/swagger-ui.html
- Health Check: http://localhost:8080/actuator/health

### Individual Container Builds

**Backend Only:**
```bash
# Build image
docker build -t expense-tracker-backend .

# Run container
docker run -d \
  -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/expense_tracker \
  -e DATABASE_USERNAME=postgres \
  -e DATABASE_PASSWORD=postgres \
  -e JWT_SECRET=YourSecretKey \
  --name backend \
  expense-tracker-backend
```

**Frontend Only:**
```bash
cd frontend

# Build image
docker build -t expense-tracker-frontend .

# Run container
docker run -d \
  -p 3000:80 \
  --name frontend \
  expense-tracker-frontend
```

### Docker Compose Services

```yaml
services:
  postgres:     # PostgreSQL database (port 5432)
  mongo:        # MongoDB for receipts (port 27017)
  backend:      # Spring Boot API (port 8080)
  frontend:     # React UI with Nginx (port 3000)
```

### Health Checks

All services include health checks:
```bash
# Check service status
docker-compose ps

# Individual service health
docker-compose exec backend curl http://localhost:8080/actuator/health
docker-compose exec postgres pg_isready -U postgres
docker-compose exec mongo mongosh --eval "db.adminCommand('ping')"
```

### Production Deployment

For production deployments, see [DOCKER_DEPLOYMENT.md](DOCKER_DEPLOYMENT.md) for:
- Environment-specific configurations
- Secrets management
- SSL/TLS setup
- Scaling strategies
- Monitoring and logging

## 🔧 Environment Variables

### Backend Configuration

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `DATABASE_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/expense_tracker` | ✅ |
| `DATABASE_USERNAME` | PostgreSQL username | `postgres` | ✅ |
| `DATABASE_PASSWORD` | PostgreSQL password | `postgres` | ✅ |
| `MONGODB_URI` | MongoDB connection string | `mongodb://localhost:27017/expense_tracker_logs` | ❌ |
| `MONGODB_USERNAME` | MongoDB username | - | ❌ |
| `MONGODB_PASSWORD` | MongoDB password | - | ❌ |
| `JWT_SECRET` | Secret key for JWT signing | - | ✅ |
| `JWT_EXPIRATION` | JWT token expiration (ms) | `86400000` (24h) | ❌ |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `prod` | ❌ |
| `SERVER_PORT` | Application port | `8080` | ❌ |

### Frontend Configuration

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `VITE_API_URL` | Backend API base URL | `http://localhost:8080/api/v1` | ✅ |

### Docker Environment

Create a `.env` file in the project root:

```env
# PostgreSQL
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres

# MongoDB
MONGODB_USERNAME=admin
MONGODB_PASSWORD=admin

# JWT
JWT_SECRET=MySecretKeyForJWTTokenGenerationAndValidation1234567890
JWT_EXPIRATION=86400000

# Spring Profile (dev, prod)
SPRING_PROFILES_ACTIVE=prod
```

## 📚 API Documentation

### Base URL
```
http://localhost:8080/api/v1
```

### Swagger UI
Access interactive API documentation at: **http://localhost:8080/swagger-ui.html**

### Authentication

All endpoints except `/auth/login` require JWT Bearer token authentication.

**Login and Get Token:**

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "email": "john.doe@example.com",
  "name": "John Doe"
}
```

**Use Token in Requests:**
```bash
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8080/api/v1/users/1/expenses
```

### API Endpoints

#### 🔐 Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/login` | Authenticate user and get JWT token |

#### 👤 Users

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/users` | Get all users |
| GET | `/users/{id}` | Get user by ID |
| POST | `/users` | Create new user |
| PUT | `/users/{id}` | Update user |
| DELETE | `/users/{id}` | Delete user |

#### 💰 Expenses

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/users/{userId}/expenses` | Get all expenses (with filters) |
| GET | `/users/{userId}/expenses/{id}` | Get expense by ID |
| POST | `/users/{userId}/expenses` | Create new expense |
| PUT | `/users/{userId}/expenses/{id}` | Update expense |
| DELETE | `/users/{userId}/expenses/{id}` | Delete expense |
| GET | `/users/{userId}/expenses/export/csv` | Export expenses as CSV |

#### 📊 Reports

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/users/{userId}/reports/monthly` | Get monthly expense totals |
| GET | `/users/{userId}/reports/category` | Get category expense breakdown |

#### 📁 Receipts

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/receipts` | Upload receipt file |
| GET | `/receipts/{id}` | Get receipt metadata |
| GET | `/receipts/{id}/download` | Download receipt file |
| DELETE | `/receipts/{id}` | Delete receipt |
| POST | `/receipts/{id}/link` | Link receipt to expense |
| POST | `/receipts/{id}/unlink` | Unlink receipt from expense |

#### 🏷️ Categories

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/categories` | Get all categories |
| GET | `/users/{userId}/categories` | Get user's categories |
| POST | `/users/{userId}/categories` | Create category |
| PUT | `/categories/{id}` | Update category |
| DELETE | `/categories/{id}` | Delete category |

### Example API Calls

#### 1. Create an Expense

```bash
curl -X POST http://localhost:8080/api/v1/users/1/expenses \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "categoryId": 5,
    "amount": 45.99,
    "currency": "USD",
    "description": "Lunch at Italian restaurant",
    "date": "2026-01-15",
    "tags": ["food", "restaurant"]
  }'
```

**Response:**
```json
{
  "id": 123,
  "userId": 1,
  "categoryId": 5,
  "categoryName": "Food & Dining",
  "amount": 45.99,
  "currency": "USD",
  "description": "Lunch at Italian restaurant",
  "date": "2026-01-15",
  "tags": ["food", "restaurant"],
  "receiptIds": [],
  "createdAt": "2026-01-15T14:30:00Z",
  "updatedAt": "2026-01-15T14:30:00Z"
}
```

#### 2. Get Expenses with Filters

```bash
curl -X GET "http://localhost:8080/api/v1/users/1/expenses?\
categoryId=5&\
minAmount=20&\
maxAmount=100&\
from=2026-01-01&\
to=2026-01-31&\
tags=food&\
page=0&\
size=20&\
sort=date,desc" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "content": [
    {
      "id": 123,
      "userId": 1,
      "categoryId": 5,
      "categoryName": "Food & Dining",
      "amount": 45.99,
      "currency": "USD",
      "description": "Lunch at Italian restaurant",
      "date": "2026-01-15",
      "tags": ["food", "restaurant"]
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 1,
  "totalPages": 1
}
```

#### 3. Get Monthly Report

```bash
curl -X GET "http://localhost:8080/api/v1/users/1/reports/monthly?year=2026" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
[
  {
    "year": 2026,
    "month": 1,
    "total": 1250.50,
    "expenseCount": 15,
    "currency": "USD"
  },
  {
    "year": 2026,
    "month": 2,
    "total": 980.25,
    "expenseCount": 12,
    "currency": "USD"
  }
]
```

#### 4. Get Category Report

```bash
curl -X GET "http://localhost:8080/api/v1/users/1/reports/category?\
from=2026-01-01&\
to=2026-12-31" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
[
  {
    "categoryId": 5,
    "categoryName": "Food & Dining",
    "color": "#FF6B6B",
    "icon": "🍽️",
    "total": 3250.75,
    "expenseCount": 45,
    "percentage": 35.5
  },
  {
    "categoryId": 3,
    "categoryName": "Transportation",
    "color": "#4ECDC4",
    "icon": "🚗",
    "total": 1890.00,
    "expenseCount": 28,
    "percentage": 20.7
  }
]
```

#### 5. Export Expenses as CSV

```bash
curl -X GET "http://localhost:8080/api/v1/users/1/expenses/export/csv?\
from=2026-01-01&\
to=2026-12-31&\
categoryId=5" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -o expenses.csv
```

**Output (expenses.csv):**
```csv
Date,Category,Amount,Currency,Description,Tags
2026-01-15,Food & Dining,45.99,USD,Lunch at Italian restaurant,"food,restaurant"
2026-01-20,Food & Dining,32.50,USD,Grocery shopping,"food,groceries"
```

#### 6. Upload Receipt

```bash
curl -X POST http://localhost:8080/api/v1/receipts \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@receipt.jpg" \
  -F "userId=1" \
  -F "description=Lunch receipt"
```

**Response:**
```json
{
  "id": "507f1f77bcf86cd799439011",
  "userId": 1,
  "filename": "receipt.jpg",
  "contentType": "image/jpeg",
  "size": 245678,
  "description": "Lunch receipt",
  "expenseIds": [],
  "uploadDate": "2026-01-15T14:35:00Z"
}
```

#### 7. Link Receipt to Expense

```bash
curl -X POST http://localhost:8080/api/v1/receipts/507f1f77bcf86cd799439011/link \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "expenseId": 123
  }'
```

### Query Parameters for Filtering

#### Expense Filters

| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| `categoryId` | Long | Filter by category | `?categoryId=5` |
| `from` | Date | Start date (YYYY-MM-DD) | `?from=2026-01-01` |
| `to` | Date | End date (YYYY-MM-DD) | `?to=2026-12-31` |
| `minAmount` | Decimal | Minimum amount | `?minAmount=10.00` |
| `maxAmount` | Decimal | Maximum amount | `?maxAmount=100.00` |
| `currency` | String | Currency code | `?currency=USD` |
| `tags` | String | Comma-separated tags | `?tags=food,essential` |
| `description` | String | Search description | `?description=restaurant` |

#### Pagination & Sorting

| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| `page` | Integer | Page number (0-based) | `?page=0` |
| `size` | Integer | Items per page | `?size=20` |
| `sort` | String | Sort field and direction | `?sort=date,desc` |

## 🧪 Testing

### Backend Tests

#### Run All Tests
```bash
mvn test
```

#### Run Unit Tests Only
```bash
mvn test -Dtest=ExpenseServiceTest
```

#### Run Integration Tests (Requires Docker)
```bash
# Start Testcontainers
mvn test -Dtest=ExpenseRepositoryTest

# Or run specific integration test
mvn test -Dtest=ExpenseControllerIntegrationTest
```

#### Generate Code Coverage Report
```bash
mvn clean test jacoco:report

# View report at: target/site/jacoco/index.html
```

#### Test Statistics
- **Unit Tests**: 22 tests (ExpenseServiceTest)
- **Integration Tests**: 6 tests with Testcontainers
- **Coverage**: ~85% code coverage
- **Frameworks**: JUnit 5, Mockito, AssertJ, Testcontainers

### Frontend Tests

#### Run All Tests
```bash
cd frontend
npm test
```

#### Run Tests in Watch Mode
```bash
npm run test:watch
```

#### Run Tests with Coverage
```bash
npm run test:coverage

# View report at: frontend/coverage/index.html
```

#### Run Specific Test File
```bash
npm test -- ExpenseForm.test.tsx
```

#### Test Statistics
- **Total Tests**: 76 tests
- **Frameworks**: Vitest, React Testing Library
- **Coverage**: ~80% component coverage

### End-to-End Testing (Optional)

For E2E testing, consider adding:
- **Playwright** or **Cypress** for browser automation
- **Postman/Newman** for API testing

Example Postman collection structure:
```
Expense Tracker API Tests/
├── Authentication/
│   └── Login
├── Expenses/
│   ├── Create Expense
│   ├── Get Expenses
│   ├── Update Expense
│   └── Delete Expense
└── Reports/
    ├── Monthly Report
    └── Category Report
```

## 🛠️ Development

### Prerequisites for Development

1. **Install Java 21**
```bash
# Windows (Chocolatey)
choco install temurin21

# macOS (Homebrew)
brew install openjdk@21

# Linux (apt)
sudo apt install openjdk-21-jdk
```

2. **Install Maven**
```bash
# Windows
choco install maven

# macOS
brew install maven

# Linux
sudo apt install maven
```

3. **Install Node.js**
```bash
# Windows
choco install nodejs-lts

# macOS
brew install node@20

# Linux
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install -y nodejs
```

### Project Setup

#### 1. Clone Repository
```bash
git clone https://github.com/yourusername/expense-tracker.git
cd expense-tracker
```

#### 2. Configure Database
```bash
# Start PostgreSQL with Docker
docker run -d \
  --name expense-postgres \
  -e POSTGRES_DB=expense_tracker \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:16-alpine

# Or use local PostgreSQL
createdb expense_tracker
```

#### 3. Configure Environment
```bash
# Copy example environment
cp .env.example .env

# Edit .env with your settings
nano .env
```

#### 4. Build Backend
```bash
mvn clean install -DskipTests
```

#### 5. Setup Frontend
```bash
cd frontend
npm install
cd ..
```

### Development Workflow

#### Using Make (Recommended)

```bash
# View all available commands
make help

# Build everything
make build

# Run backend
make run-backend

# Run frontend
make run-frontend

# Run tests
make test

# Clean build artifacts
make clean

# Docker operations
make docker-build
make docker-up
make docker-down
```

#### Using dev.bat (Windows)

```bash
# View all commands
dev.bat help

# Build project
dev.bat build

# Run backend
dev.bat run-backend

# Run frontend
dev.bat run-frontend

# Run tests
dev.bat test
```

#### Manual Commands

**Backend:**
```bash
# Compile
mvn compile

# Run
mvn spring-boot:run

# Run with profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Package
mvn package -DskipTests

# Run JAR
java -jar target/expense-tracker-1.0.0.jar
```

**Frontend:**
```bash
cd frontend

# Install dependencies
npm install

# Start dev server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Lint code
npm run lint
```

### Database Management

#### Flyway Migrations

```bash
# View migration status
mvn flyway:info

# Run migrations
mvn flyway:migrate

# Rollback last migration
mvn flyway:undo

# Clean database (CAUTION: Deletes all data)
mvn flyway:clean
```

#### Create New Migration

1. Create file: `src/main/resources/db/migration/V{version}__{description}.sql`
2. Example: `V2__Add_expense_notes.sql`

```sql
ALTER TABLE expenses
ADD COLUMN notes TEXT;
```

### Code Quality

#### Backend Linting
```bash
# Run Checkstyle
mvn checkstyle:check

# Run SpotBugs
mvn spotbugs:check
```

#### Frontend Linting
```bash
cd frontend

# Run ESLint
npm run lint

# Fix auto-fixable issues
npm run lint:fix

# Run Prettier
npm run format
```

### Hot Reload

#### Backend (Spring Boot DevTools)
```xml
<!-- Add to pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <optional>true</optional>
</dependency>
```

#### Frontend (Vite)
Hot reload is enabled by default with `npm run dev`.

### Debug Mode

#### Backend (IntelliJ IDEA)
1. Right-click `ExpenseTrackerApplication.java`
2. Select "Debug 'ExpenseTrackerApplication'"

#### Backend (VS Code)
```json
// .vscode/launch.json
{
  "type": "java",
  "name": "Debug Spring Boot",
  "request": "launch",
  "mainClass": "com.expense.tracker.ExpenseTrackerApplication"
}
```

#### Frontend (Browser DevTools)
```bash
npm run dev -- --debug
```

### Useful Development Commands

```bash
# Check Java version
java -version

# Check Maven version
mvn -version

# Check Node version
node -v

# Check npm version
npm -v

# View Spring Boot info
mvn spring-boot:run -Dspring-boot.run.arguments=--version

# Database connection test
psql -U postgres -d expense_tracker -c "SELECT 1;"

# Check running containers
docker ps

# View application logs
tail -f logs/application.log
```

### IDE Recommendations

#### IntelliJ IDEA (Recommended for Backend)
- Install Lombok plugin
- Enable annotation processing
- Install Spring Boot Assistant
- Configure Checkstyle

#### VS Code (Recommended for Frontend)
Extensions:
- **ESLint** - Code linting
- **Prettier** - Code formatting
- **Tailwind CSS IntelliSense** - Tailwind autocomplete
- **ES7+ React/Redux/React-Native snippets** - React snippets

### Troubleshooting

#### Backend Issues

**Port 8080 already in use:**
```bash
# Kill process on port 8080 (Linux/Mac)
lsof -ti:8080 | xargs kill -9

# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

**Database connection failed:**
```bash
# Check PostgreSQL is running
docker ps | grep postgres

# Test connection
psql -U postgres -h localhost -p 5432 -d expense_tracker
```

**Flyway migration failed:**
```bash
# Repair Flyway metadata
mvn flyway:repair

# Or clean and retry (DELETES DATA)
mvn flyway:clean flyway:migrate
```

#### Frontend Issues

**Module not found:**
```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
```

**Port 5173 already in use:**
```bash
# Change port in vite.config.ts
server: {
  port: 5174
}
```

**CORS errors:**
Ensure backend has CORS configuration in `WebConfig.java`:
```java
@CrossOrigin(origins = "http://localhost:5173")
```

## 📁 Project Structure

```
expense-tracker/
├── .github/                      # GitHub configurations
│   ├── workflows/               # CI/CD pipelines
│   └── copilot-instructions.md  # Copilot guidelines
├── frontend/                     # React frontend
│   ├── public/                  # Static assets
│   ├── src/
│   │   ├── components/          # React components
│   │   ├── pages/               # Page components
│   │   ├── services/            # API services
│   │   ├── hooks/               # Custom hooks
│   │   ├── utils/               # Utility functions
│   │   ├── types/               # TypeScript types
│   │   └── App.tsx              # Root component
│   ├── tests/                   # Frontend tests
│   ├── package.json
│   ├── vite.config.ts
│   └── tailwind.config.js
├── src/main/
│   ├── java/com/expense/tracker/
│   │   ├── config/              # Spring configurations
│   │   │   ├── SecurityConfig.java
│   │   │   ├── MongoConfig.java
│   │   │   └── DataSeeder.java
│   │   ├── controller/          # REST controllers
│   │   │   ├── AuthenticationController.java
│   │   │   ├── ExpenseController.java
│   │   │   ├── ReportController.java
│   │   │   ├── ReceiptController.java
│   │   │   └── CategoryController.java
│   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── ExpenseCreateDto.java
│   │   │   ├── ExpenseResponseDto.java
│   │   │   ├── MonthlyReportDto.java
│   │   │   └── CategoryReportDto.java
│   │   ├── exception/           # Exception handlers
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   └── ResourceNotFoundException.java
│   │   ├── model/               # JPA entities
│   │   │   ├── User.java
│   │   │   ├── Expense.java
│   │   │   ├── Category.java
│   │   │   └── ExpenseTag.java
│   │   ├── repository/          # Data access layer
│   │   │   ├── ExpenseRepository.java
│   │   │   ├── UserRepository.java
│   │   │   ├── CategoryRepository.java
│   │   │   └── ExpenseSpecification.java
│   │   ├── security/            # JWT & Security
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   └── JwtTokenUtil.java
│   │   ├── service/             # Business logic
│   │   │   ├── ExpenseService.java
│   │   │   ├── UserService.java
│   │   │   ├── CategoryService.java
│   │   │   └── ReceiptService.java
│   │   └── ExpenseTrackerApplication.java
│   └── resources/
│       ├── db/migration/        # Flyway SQL scripts
│       │   ├── V1__Initial_Schema.sql
│       │   └── V2__Add_indexes.sql
│       ├── application.properties
│       └── application-dev.properties
├── src/test/
│   └── java/com/expense/tracker/
│       ├── service/
│       │   └── ExpenseServiceTest.java
│       ├── repository/
│       │   └── ExpenseRepositoryTest.java
│       └── controller/
│           └── ExpenseControllerIntegrationTest.java
├── target/                       # Build output
├── .dockerignore
├── .env.example                  # Environment template
├── .gitignore
├── DATA_SEEDER.md               # Data seeder docs
├── dev.bat                       # Windows dev script
├── DEVELOPMENT.md               # Development guide
├── docker-compose.yml           # Docker orchestration
├── Dockerfile                    # Backend container
├── DOCKER_DEPLOYMENT.md         # Docker deployment guide
├── Makefile                      # Build automation
├── pom.xml                       # Maven configuration
├── README.md                     # This file
└── TESTING.md                    # Testing guide
```

### Key Directories

- **`config/`** - Spring configurations (Security, MongoDB, JWT, CORS)
- **`controller/`** - REST API endpoints with Swagger documentation
- **`dto/`** - Request/response objects for API
- **`exception/`** - Centralized exception handling
- **`model/`** - JPA entities mapped to database tables
- **`repository/`** - Data access with custom queries and specifications
- **`security/`** - JWT authentication and authorization
- **`service/`** - Business logic and transaction management
- **`db/migration/`** - Flyway database version control

## 🤝 Contributing

We welcome contributions! Please follow these guidelines:

### 1. Fork the Repository

```bash
git clone https://github.com/yourusername/expense-tracker.git
cd expense-tracker
git remote add upstream https://github.com/original/expense-tracker.git
```

### 2. Create a Branch

```bash
git checkout -b feature/your-feature-name
```

Branch naming conventions:
- `feature/` - New features
- `bugfix/` - Bug fixes
- `hotfix/` - Critical fixes
- `docs/` - Documentation updates

### 3. Make Changes

- Write clean, readable code
- Follow existing code style
- Add tests for new features
- Update documentation

### 4. Test Your Changes

```bash
# Backend tests
mvn test

# Frontend tests
cd frontend && npm test

# Integration tests
mvn verify
```

### 5. Commit Your Changes

```bash
git add .
git commit -m "feat: Add expense filtering by tags"
```

Commit message format:
- `feat:` - New feature
- `fix:` - Bug fix
- `docs:` - Documentation changes
- `test:` - Test additions/changes
- `refactor:` - Code refactoring
- `chore:` - Maintenance tasks

### 6. Push and Create Pull Request

```bash
git push origin feature/your-feature-name
```

Then create a Pull Request on GitHub with:
- Clear title and description
- Link to related issues
- Screenshots (if UI changes)
- Test results

### Code Review Process

1. Automated CI/CD checks must pass
2. At least one maintainer approval required
3. All comments must be resolved
4. Branch must be up-to-date with main

### Development Standards

#### Backend (Java)
- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use Lombok to reduce boilerplate
- Write JavaDoc for public methods
- Minimum 80% test coverage

#### Frontend (React/TypeScript)
- Use TypeScript for all new code
- Follow [Airbnb React Style Guide](https://github.com/airbnb/javascript/tree/master/react)
- Use functional components with hooks
- Write PropTypes or TypeScript interfaces

#### Database
- All schema changes via Flyway migrations
- Never modify existing migrations
- Test migrations on sample data

## 📄 License

This project is licensed under the **MIT License**.

```
MIT License

Copyright (c) 2026 Expense Tracker Contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## 📞 Support

- **Documentation**: See additional `.md` files in the repository
  - [DATA_SEEDER.md](DATA_SEEDER.md) - Sample data generation
  - [DOCKER_DEPLOYMENT.md](DOCKER_DEPLOYMENT.md) - Docker deployment guide
  - [DEVELOPMENT.md](DEVELOPMENT.md) - Development workflow
  - [TESTING.md](TESTING.md) - Testing strategies

- **Issues**: [GitHub Issues](https://github.com/yourusername/expense-tracker/issues)
- **Discussions**: [GitHub Discussions](https://github.com/yourusername/expense-tracker/discussions)

## 🎯 Roadmap

### Version 2.0 (Planned)
- [ ] Multi-currency conversion with real-time exchange rates
- [ ] Recurring expense templates
- [ ] Budget tracking and alerts
- [ ] Email notifications
- [ ] Mobile app (React Native)

### Version 2.1 (Future)
- [ ] AI-powered expense categorization
- [ ] OCR for receipt text extraction
- [ ] Multi-user sharing and permissions
- [ ] Export to accounting software (QuickBooks, Xero)
- [ ] GraphQL API

## 🙏 Acknowledgments

- **Spring Boot Team** - Excellent framework documentation
- **React Team** - Modern UI library
- **Testcontainers** - Simplified integration testing
- **MongoDB** - Flexible document storage
- **PostgreSQL** - Robust relational database

---

**Built with ❤️ by the Expense Tracker Team**

**⭐ Star this repository if you find it helpful!**## Build and Run

### Build the project:
```bash
mvn clean install
```

### Run the application:
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## Testing

Run tests:
```bash
mvn test
```

## API Endpoints

Once you add controllers, your REST endpoints will be available at:
- `http://localhost:8080/api/*`

## Development

- **Controllers**: Add REST endpoints in [`controller/`](src/main/java/com/expense/tracker/controller/)
- **Services**: Add business logic in [`service/`](src/main/java/com/expense/tracker/service/)
- **Repositories**: Add data access in [`repository/`](src/main/java/com/expense/tracker/repository/)
- **Models**: Add entities in [`model/`](src/main/java/com/expense/tracker/model/)
- **Migrations**: Add SQL scripts in [`db/migration/`](src/main/resources/db/migration/)

## Security

Spring Security is included. Default configuration can be customized in the `config` package. 
Uncomment security properties in `application.properties` to set default credentials.

## License

MIT License

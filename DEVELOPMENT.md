# Development Guide

## Prerequisites

- Java 21 (Eclipse Adoptium JDK recommended)
- Maven 3.9+
- Node.js 20+
- Docker and Docker Compose (for containerized deployment)
- PostgreSQL 16+ (for local development without Docker)
- MongoDB 7+ (for local development without Docker)

## Quick Start - Development Mode

### Option 1: Using Scripts (Windows)

```bash
# Install dependencies
dev.bat install

# Terminal 1: Start backend
dev.bat dev-backend

# Terminal 2: Start frontend
dev.bat dev-frontend
```

### Option 2: Using Makefile (Unix/Mac/WSL)

```bash
# Install dependencies
make install

# Terminal 1: Start backend
make dev-backend

# Terminal 2: Start frontend
make dev-frontend
```

### Option 3: Manual Commands

**Backend:**
```bash
# Set JAVA_HOME (Windows)
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot

# Run backend
mvn spring-boot:run
```

**Frontend:**
```bash
cd frontend
npm run dev
```

## Quick Start - Docker Mode

### Start All Services

```bash
# Windows
dev.bat docker-up

# Unix/Mac/WSL
make docker-up

# Or directly
docker-compose up -d
```

### View Logs

```bash
# Windows
dev.bat docker-logs

# Unix/Mac/WSL
make docker-logs

# Or directly
docker-compose logs -f
```

### Stop Services

```bash
# Windows
dev.bat docker-down

# Unix/Mac/WSL
make docker-down

# Or directly
docker-compose down
```

## Access Points

### Development Mode
- **Frontend:** http://localhost:5173
- **Backend API:** http://localhost:8080/api/v1
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **PostgreSQL:** localhost:5432
- **MongoDB:** localhost:27017

### Docker Mode
- **Frontend:** http://localhost:80
- **Backend API:** http://localhost:8080/api/v1
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **PostgreSQL:** localhost:5432
- **MongoDB:** localhost:27017

## Database Setup (Local Development)

### PostgreSQL

```sql
-- Create database
CREATE DATABASE expense_tracker;

-- Create user (optional)
CREATE USER expense_user WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE expense_tracker TO expense_user;
```

### MongoDB

```bash
# Start MongoDB with authentication
mongod --auth

# Create admin user
mongosh
use admin
db.createUser({
  user: "admin",
  pwd: "password",
  roles: ["root"]
})

# Create database
use expense_tracker_logs
```

### Environment Configuration

Create/update `src/main/resources/application-dev.properties`:

```properties
# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/expense_tracker
spring.datasource.username=postgres
spring.datasource.password=postgres

# MongoDB
spring.data.mongodb.uri=mongodb://admin:password@localhost:27017/expense_tracker_logs?authSource=admin

# JWT
jwt.secret=your-development-secret-key
jwt.expiration=86400000
```

## Testing

### Backend Tests

```bash
# Windows
dev.bat test-backend

# Unix/Mac/WSL
make test-backend

# Or directly
mvn test
```

### Frontend Tests

```bash
# Windows
dev.bat test-frontend

# Unix/Mac/WSL
make test-frontend

# Or directly
cd frontend && npm test
```

## Integration Testing with Docker

Use Docker Compose for integration testing with all services:

```bash
# Start services
docker-compose up -d

# Run integration tests (when implemented)
mvn verify -P integration-tests

# Clean up
docker-compose down -v
```

## Building for Production

### Backend JAR

```bash
mvn clean package -DskipTests
# Output: target/expense-tracker-1.0.0.jar
```

### Frontend Build

```bash
cd frontend
npm run build
# Output: frontend/dist/
```

### Docker Images

```bash
# Build all images
docker-compose build

# Or individually
docker build -t expense-tracker-backend .
docker build -t expense-tracker-frontend ./frontend
```

## Available Scripts

### Windows (dev.bat)
- `dev.bat install` - Install all dependencies
- `dev.bat dev-backend` - Start backend
- `dev.bat dev-frontend` - Start frontend
- `dev.bat docker-up` - Start Docker services
- `dev.bat docker-down` - Stop Docker services
- `dev.bat docker-logs` - View Docker logs
- `dev.bat docker-build` - Build Docker images
- `dev.bat docker-clean` - Clean Docker volumes
- `dev.bat test-backend` - Run backend tests
- `dev.bat test-frontend` - Run frontend tests
- `dev.bat clean` - Clean build artifacts

### Unix/Mac/WSL (Makefile)
- `make install` - Install all dependencies
- `make dev-backend` - Start backend
- `make dev-frontend` - Start frontend
- `make docker-up` - Start Docker services
- `make docker-down` - Stop Docker services
- `make docker-logs` - View Docker logs
- `make docker-build` - Build Docker images
- `make docker-clean` - Clean Docker volumes
- `make test-backend` - Run backend tests
- `make test-frontend` - Run frontend tests
- `make clean` - Clean build artifacts

## Troubleshooting

### Backend Issues

**Port 8080 already in use:**
```bash
# Windows: Find and kill process
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Unix/Mac: Find and kill process
lsof -i :8080
kill -9 <PID>
```

**Database connection failed:**
- Verify PostgreSQL/MongoDB is running
- Check credentials in application.properties
- Ensure databases exist

**Flyway migration errors:**
```bash
# Clean and re-run migrations
mvn flyway:clean
mvn spring-boot:run
```

### Frontend Issues

**Port 5173 already in use:**
- Change port in vite.config.ts
- Or kill existing process

**API calls failing:**
- Verify backend is running on port 8080
- Check VITE_API_BASE_URL in .env
- Check browser console for CORS errors

### Docker Issues

**Services not starting:**
```bash
# Check logs
docker-compose logs

# Rebuild images
docker-compose build --no-cache

# Clean everything and restart
docker-compose down -v
docker-compose up -d --build
```

**Database not initializing:**
```bash
# Remove volumes and restart
docker-compose down -v
docker-compose up -d
```

## Hot Reload

### Backend
- Spring Boot DevTools enabled
- Automatic restart on code changes
- Fast reload for most changes

### Frontend
- Vite HMR (Hot Module Replacement)
- Instant updates on code changes
- Preserves component state

## Code Quality

### Backend
```bash
# Format code
mvn spotless:apply

# Check style
mvn checkstyle:check

# Generate Javadoc
mvn javadoc:javadoc
```

### Frontend
```bash
cd frontend

# Lint code
npm run lint

# Type check
npx tsc --noEmit
```

## Performance Profiling

### Backend
- Enable JMX monitoring
- Use VisualVM or JProfiler
- Monitor with Actuator endpoints

### Frontend
- Use React DevTools
- Chrome Performance tab
- Lighthouse audits

## Debugging

### Backend
- Use IDE debugger with Maven debug mode:
```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
```

### Frontend
- Use browser DevTools
- React Developer Tools extension
- Source maps enabled in development

## CI/CD Integration

The project is ready for CI/CD with:
- Docker Compose for integration tests
- Separate dev/prod profiles
- Health check endpoints
- Automated migrations

Example GitHub Actions workflow:
```yaml
- Run tests: mvn test
- Build backend: mvn package
- Build frontend: cd frontend && npm run build
- Docker build: docker-compose build
- Integration tests: docker-compose up -d && mvn verify
```

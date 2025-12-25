# Expense Tracker - Docker Deployment Guide

## Quick Start

### Prerequisites
- Docker and Docker Compose installed
- Ports 80, 8080, 5432, and 27017 available

### Start All Services

```bash
# Copy environment template
cp .env.example .env

# Edit .env with your configurations (optional)

# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v
```

## Services

### PostgreSQL (postgres)
- **Port:** 5432
- **Database:** expense_tracker
- **Default Credentials:** postgres/postgres (configurable via .env)
- **Volume:** postgres_data
- **Health Check:** pg_isready every 10s

### MongoDB (mongo)
- **Port:** 27017
- **Database:** expense_tracker_logs
- **Default Credentials:** admin/admin (configurable via .env)
- **Volumes:** mongo_data, mongo_config
- **Health Check:** mongosh ping every 10s

### Backend (Spring Boot)
- **Port:** 8080
- **Technology:** Java 21, Spring Boot 3.2.1
- **Features:**
  - REST API endpoints
  - JWT authentication
  - Flyway migrations (runs automatically on startup)
  - PostgreSQL for main data
  - MongoDB GridFS for receipts
- **Health Check:** /actuator/health every 30s
- **Depends On:** postgres (healthy), mongo (healthy)
- **Environment Variables:**
  - DATABASE_URL
  - DATABASE_USERNAME
  - DATABASE_PASSWORD
  - MONGODB_URI
  - JWT_SECRET
  - JWT_EXPIRATION

### Frontend (React + Vite)
- **Port:** 80 (HTTP), 443 (HTTPS)
- **Technology:** React 18, TypeScript, Vite, TailwindCSS
- **Features:**
  - Single Page Application (SPA)
  - React Router for navigation
  - Axios API client with JWT interceptor
  - Recharts for data visualization
  - Responsive design
- **Nginx Configuration:**
  - API proxy to backend (/api/* → backend:8080)
  - React Router support (SPA fallback)
  - Static asset caching
  - Gzip compression
  - Security headers
- **Health Check:** / every 30s
- **Depends On:** backend (healthy)

## Database Migrations

Flyway migrations run automatically when the backend starts:

1. Backend waits for PostgreSQL to be healthy
2. Flyway checks migration status
3. Executes pending migrations from `src/main/resources/db/migration/`
4. Creates tables: users, categories, expenses, expense_tags
5. Creates indexes for optimal query performance

### Migration Files
- `V1__create_tables.sql` - Initial schema with users, categories, expenses

## Environment Variables

Create a `.env` file in the root directory:

```env
# PostgreSQL
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your_secure_password

# MongoDB
MONGODB_USERNAME=admin
MONGODB_PASSWORD=your_secure_password

# JWT
JWT_SECRET=your_jwt_secret_key_min_256_bits
JWT_EXPIRATION=86400000

# Spring Profile
SPRING_PROFILES_ACTIVE=prod
```

## Networking

All services are connected via `expense-tracker-network` bridge network:
- Services can communicate using service names as hostnames
- Frontend proxies API requests to backend via Nginx
- External access:
  - Frontend: http://localhost:80
  - Backend API: http://localhost:8080
  - PostgreSQL: localhost:5432
  - MongoDB: localhost:27017

## Data Persistence

Three named volumes store persistent data:
- `expense-tracker-postgres-data` - PostgreSQL data
- `expense-tracker-mongo-data` - MongoDB data
- `expense-tracker-mongo-config` - MongoDB configuration

## Build Process

### Backend Build
1. Maven downloads dependencies
2. Compiles Java 21 source code
3. Runs MapStruct annotation processors
4. Packages as JAR file
5. Creates Docker image with JRE 21

### Frontend Build
1. npm installs dependencies
2. Vite builds optimized production bundle
3. Outputs to dist/ directory
4. Nginx serves static files

## Troubleshooting

### Check Service Status
```bash
docker-compose ps
```

### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f frontend
```

### Restart Service
```bash
docker-compose restart backend
```

### Rebuild Images
```bash
# Rebuild all
docker-compose build

# Rebuild specific service
docker-compose build backend

# Build and start
docker-compose up -d --build
```

### Database Connection Issues
```bash
# Check PostgreSQL is ready
docker-compose exec postgres pg_isready -U postgres

# Check MongoDB is ready
docker-compose exec mongo mongosh --eval "db.adminCommand('ping')"
```

### Backend Not Starting
- Check database connections
- Verify environment variables
- Check Flyway migration logs
- Ensure Java 21 compatibility

### Frontend Not Loading
- Verify backend is healthy
- Check API proxy configuration in nginx.conf
- Ensure VITE_API_BASE_URL is correct
- Check browser console for errors

## Production Considerations

### Security
- Change default passwords in .env
- Use strong JWT secret (min 256 bits)
- Configure HTTPS with SSL certificates
- Enable firewall rules
- Use secrets management (Docker Swarm secrets, Kubernetes secrets)

### Performance
- Adjust PostgreSQL shared_buffers and work_mem
- Configure MongoDB WiredTiger cache size
- Enable connection pooling in backend
- Use CDN for static assets
- Enable HTTP/2 in Nginx

### Monitoring
- Add Prometheus metrics endpoint
- Configure log aggregation (ELK stack)
- Set up health check alerts
- Monitor resource usage

### Scaling
- Use Docker Swarm or Kubernetes for orchestration
- Scale backend horizontally
- Use managed database services (AWS RDS, MongoDB Atlas)
- Implement Redis for session management
- Use load balancer for frontend

## API Access

Once running, access the application:

- **Frontend:** http://localhost
- **Backend API:** http://localhost:8080/api/v1
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **Health Check:** http://localhost:8080/actuator/health

## Default Routes

- `/dashboard` - Analytics and charts
- `/expenses` - List and filter expenses
- `/expenses/new` - Create new expense
- `/categories` - Manage categories
- `/receipts` - Upload and view receipts

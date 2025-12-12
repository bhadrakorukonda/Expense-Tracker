# Expense Tracker

A Spring Boot 3 application for tracking expenses with PostgreSQL and MongoDB support.

## Technologies

- **Spring Boot 3.2.1**
- **Java 17**
- **Maven**
- **PostgreSQL** (Primary database)
- **MongoDB** (Logging/Analytics)
- **Flyway** (Database migrations)
- **Lombok** (Code generation)
- **Spring Security** (Authentication/Authorization)

## Dependencies

- `spring-boot-starter-web` - REST API support
- `spring-boot-starter-data-jpa` - PostgreSQL integration
- `spring-boot-starter-validation` - Bean validation
- `spring-boot-starter-data-mongodb` - MongoDB integration
- `spring-boot-starter-security` - Security features
- `postgresql` - PostgreSQL driver
- `flyway-core` - Database migrations
- `lombok` - Reduce boilerplate code

## Project Structure

```
expense-tracker/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/expense/tracker/
│   │   │       ├── config/          # Configuration classes
│   │   │       ├── controller/      # REST controllers
│   │   │       ├── dto/             # Data Transfer Objects
│   │   │       ├── exception/       # Exception handlers
│   │   │       ├── model/           # Entity classes
│   │   │       ├── repository/      # Data access layer
│   │   │       ├── service/         # Business logic
│   │   │       └── ExpenseTrackerApplication.java
│   │   └── resources/
│   │       ├── db/migration/        # Flyway SQL scripts
│   │       └── application.properties
│   └── test/
│       └── java/
│           └── com/expense/tracker/ # Test classes
├── .github/
│   └── copilot-instructions.md
└── pom.xml
```

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+
- MongoDB 4.4+ (optional)

## Configuration

Update [`application.properties`](src/main/resources/application.properties) with your database credentials:

```properties
# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/expense_tracker
spring.datasource.username=your_username
spring.datasource.password=your_password

# MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017/expense_tracker_logs
```

## Database Setup

1. Create PostgreSQL database:
```sql
CREATE DATABASE expense_tracker;
```

2. Flyway will automatically run migrations on startup

## Build and Run

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

# Data Seeder Documentation

## Overview

The `DataSeeder` is a CommandLineRunner that automatically populates the database with sample data when the application starts in **development mode**. This is useful for:

- Local development and testing
- Demo purposes
- Frontend development without manual data entry
- Testing with realistic data

## Activation

The seeder only runs when the `dev` profile is active.

### Method 1: Via application.properties
```properties
spring.profiles.active=dev
```

### Method 2: Command Line
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Method 3: Environment Variable
```bash
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run
```

### Method 4: IDE Configuration
In IntelliJ IDEA or Eclipse, add `dev` to active profiles in run configuration.

## Generated Data

### Users (3 accounts)
- **john.doe@example.com** / password123
- **jane.smith@example.com** / password123
- **bob.wilson@example.com** / password123

All passwords are BCrypt encoded.

### Categories (8-12 per user)
Randomly selected from 15 category types:
- Food & Dining
- Groceries
- Transportation
- Shopping
- Entertainment
- Healthcare
- Utilities
- Rent
- Insurance
- Education
- Travel
- Fitness
- Personal Care
- Gifts
- Subscriptions

### Expenses (50-100 per user)
Each expense has:
- **Amount**: Random between $5.00 and $500.00
- **Date**: Random date within last 90 days
- **Currency**: USD, EUR, GBP, CAD, or AUD
- **Description**: Category-appropriate description (e.g., "Lunch at restaurant" for Food & Dining)
- **Tags**: 0-3 random tags (essential, recurring, one-time, luxury, emergency, etc.)
- **Category**: Linked to one of the user's categories

## Important Notes

### Idempotency
The seeder checks if data exists before running:
```java
if (userRepository.count() > 0) {
    log.info("Database already contains data. Skipping seeding.");
    return;
}
```

This means:
- ✅ Safe to restart the application multiple times
- ✅ Won't duplicate data
- ✅ Only runs on empty database

### Resetting Data
To re-run the seeder:
1. Drop and recreate the database
2. Restart the application with `dev` profile

```sql
DROP DATABASE expense_tracker;
CREATE DATABASE expense_tracker;
```

### Production Safety
- ✅ **Never runs in production** (requires `dev` profile)
- ✅ Profile-based activation prevents accidental seeding
- ✅ Transactional - rolls back on error

## Customization

### Add More Users
Edit `seedUsers()` method in `DataSeeder.java`:
```java
users.add(createUser("newuser@example.com", "New User", "password123"));
```

### Adjust Expense Count
Modify the range in `seedExpenses()`:
```java
// Currently 50-100 expenses per user
int expenseCount = 50 + random.nextInt(51);

// Change to 100-200 expenses
int expenseCount = 100 + random.nextInt(101);
```

### Add Categories
Add to `CATEGORY_NAMES` array:
```java
private static final String[] CATEGORY_NAMES = {
    "Food & Dining", 
    "New Category",  // Add here
    // ...
};
```

### Add Descriptions
Update `createExpenseDescriptions()` method:
```java
map.put("New Category", List.of("Description 1", "Description 2", "Description 3"));
```

### Change Date Range
Modify the date calculation in `seedExpenses()`:
```java
// Currently last 90 days
LocalDate date = LocalDate.now().minusDays(random.nextInt(90));

// Change to last 365 days
LocalDate date = LocalDate.now().minusDays(random.nextInt(365));
```

## Logging

The seeder provides detailed logging:

```
================================================================================
Starting data seeding for DEV profile...
================================================================================
✅ Created 3 users
✅ Created 28 categories across all users
  Created 73 expenses for user: john.doe@example.com
  Created 62 expenses for user: jane.smith@example.com
  Created 85 expenses for user: bob.wilson@example.com
✅ Created 220 expenses across all users
================================================================================
Data seeding completed successfully!
Test accounts:
  📧 john.doe@example.com - password: password123
  📧 jane.smith@example.com - password: password123
  📧 bob.wilson@example.com - password: password123
================================================================================
```

## Testing with Seeded Data

### API Examples

```bash
# Login as John Doe
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john.doe@example.com","password":"password123"}'

# Get expenses (use JWT token from login)
curl http://localhost:8080/api/v1/users/1/expenses \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Export to CSV
curl http://localhost:8080/api/v1/users/1/expenses/export/csv \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -o expenses.csv

# Get monthly report
curl http://localhost:8080/api/v1/users/1/reports/monthly?year=2026 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Troubleshooting

### Seeder doesn't run
- ✅ Check if `dev` profile is active: `logging.level.com.expense.tracker.config.DataSeeder=DEBUG`
- ✅ Verify database is empty: `SELECT COUNT(*) FROM users;`
- ✅ Check application logs for seeding messages

### Duplicate key errors
- Database already has data - seeder skipped
- Solution: Drop and recreate database

### Seeder runs in production
- **This should never happen** - `@Profile("dev")` annotation prevents it
- If it does, check that no production environment has `SPRING_PROFILES_ACTIVE=dev`

## File Location

```
src/main/java/com/expense/tracker/config/DataSeeder.java
```

## Dependencies

The seeder requires:
- `UserRepository`
- `CategoryRepository`
- `ExpenseRepository`
- `PasswordEncoder` (for BCrypt hashing)

All injected via constructor injection using Lombok's `@RequiredArgsConstructor`.

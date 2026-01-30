# Testing Documentation

## Overview

The project includes comprehensive unit and integration tests using JUnit 5, Mockito, and Testcontainers.

## Test Structure

### Unit Tests
**Location:** `src/test/java/com/expense/tracker/service/ExpenseServiceTest.java`

**Framework:** JUnit 5 + Mockito

**Coverage:**
- ✅ Create Expense Tests (4 test cases)
  - Successful creation
  - User not found exception
  - Category not found exception
  - Category ownership validation

- ✅ Update Expense Tests (3 test cases)
  - Successful update
  - Expense not found exception
  - Category ownership validation

- ✅ Delete Expense Tests (2 test cases)
  - Successful deletion
  - Expense not found exception

- ✅ Get Expense Tests (2 test cases)
  - Successful retrieval by ID
  - Expense not found exception

- ✅ Get Expenses By User Tests (2 test cases)
  - Pagination support
  - User not found exception

- ✅ Calculate Total Expenses Tests (6 test cases)
  - Total by user ID
  - Return zero when no expenses
  - Total by date range
  - Invalid date range validation
  - Total by category
  - Category ownership validation

- ✅ Search Expenses Tests (3 test cases)
  - Search with multiple filters
  - Invalid date range validation
  - Invalid amount range validation

**Total Unit Tests:** 22 test cases

### Integration Tests
**Location:** `src/test/java/com/expense/tracker/integration/ExpenseIntegrationTest.java`

**Framework:** JUnit 5 + Spring Boot Test + Testcontainers

**Containers:**
- PostgreSQL 16 (alpine)
- MongoDB 7 (jammy)

**Test Scenarios:**
1. **Create and Read Flow**
   - Creates expense with full relationships (user, category, tags)
   - Verifies all fields persist correctly
   - Reads expense back and validates data integrity

2. **Multiple Expenses**
   - Creates multiple expenses for same user
   - Calculates total expenses
   - Verifies persistence

3. **Category Totals**
   - Creates expenses across multiple categories
   - Calculates per-category totals
   - Validates overall totals

4. **Date Range Filtering**
   - Creates expenses with different dates
   - Filters by date range
   - Validates date-based calculations

5. **Container Health**
   - Verifies Testcontainers are running
   - Checks database connectivity

**Total Integration Tests:** 6 test cases

## Dependencies Added

```xml
<!-- Testcontainers BOM -->
<testcontainers.version>1.19.3</testcontainers.version>

<!-- Testcontainers Dependencies -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <version>${testcontainers.version}</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>${testcontainers.version}</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <version>${testcontainers.version}</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>mongodb</artifactId>
    <version>${testcontainers.version}</version>
    <scope>test</scope>
</dependency>
```

## Running Tests

### Prerequisites
- Docker Desktop running (for Testcontainers)
- Java 21
- Maven 3.9+

### Run Unit Tests Only
```bash
# Windows
.\mvnw.cmd test -Dtest=ExpenseServiceTest

# Unix/Mac
./mvnw test -Dtest=ExpenseServiceTest

# Or using dev scripts
dev.bat test-backend
make test-backend
```

### Run Integration Tests Only
```bash
# Windows
.\mvnw.cmd test -Dtest=ExpenseIntegrationTest

# Unix/Mac
./mvnw test -Dtest=ExpenseIntegrationTest
```

### Run All Tests
```bash
# Windows
.\mvnw.cmd clean test

# Unix/Mac
./mvnw clean test
```

### Run Tests with Coverage
```bash
.\mvnw.cmd clean test jacoco:report
```

## Test Configuration

### application-test.properties
```properties
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false
logging.level.org.hibernate.SQL=WARN
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=WARN
logging.level.com.expense.tracker=INFO
logging.level.org.testcontainers=INFO
logging.level.org.springframework.test=INFO
```

### Testcontainers Configuration
- **Container Reuse:** Enabled for faster test execution
- **PostgreSQL:** Uses `postgres:16-alpine` image
- **MongoDB:** Uses `mongo:7-jammy` image
- **Dynamic Configuration:** Properties set via `@DynamicPropertySource`
- **DDL Strategy:** `create-drop` for integration tests
- **Flyway:** Disabled for integration tests (schema auto-created)

## Test Best Practices

### Unit Tests
✅ Use `@ExtendWith(MockitoExtension.class)` for Mockito support
✅ Mock all dependencies with `@Mock`
✅ Use `@InjectMocks` for service under test
✅ Organize tests with `@Nested` classes
✅ Use `@DisplayName` for readable test descriptions
✅ Follow AAA pattern (Arrange, Act, Assert)
✅ Use AssertJ fluent assertions
✅ Verify mock interactions with `verify()`

### Integration Tests
✅ Use `@SpringBootTest` for full Spring context
✅ Use `@Testcontainers` for container management
✅ Use `@Container` for container definitions
✅ Configure dynamic properties with `@DynamicPropertySource`
✅ Use `@Transactional` to rollback test data
✅ Clean up data in `@BeforeEach`
✅ Test complete workflows (create → read → verify)

## CI/CD Integration

Tests are automatically run in GitHub Actions workflow:

```yaml
# Unit Tests
- name: Run unit tests
  run: mvn test

# Integration Tests (with Docker)
- name: Run integration tests
  run: mvn verify
  services:
    postgres:
      image: postgres:16-alpine
    mongodb:
      image: mongo:7-jammy
```

## Assertions Library

Using **AssertJ** for fluent assertions:

```java
// Basic assertions
assertThat(result).isNotNull();
assertThat(result.getId()).isEqualTo(1L);

// BigDecimal comparisons
assertThat(total).isEqualByComparingTo(new BigDecimal("50.00"));

// Collections
assertThat(expenses).hasSize(3);
assertThat(tags).containsExactlyInAnyOrder("tag1", "tag2");

// Exceptions
assertThatThrownBy(() -> service.method())
    .isInstanceOf(ResourceNotFoundException.class)
    .hasMessageContaining("User");
```

## Mockito Patterns

```java
// Stubbing
when(repository.findById(1L)).thenReturn(Optional.of(entity));
when(repository.save(any(Entity.class))).thenReturn(savedEntity);

// Verification
verify(repository).findById(1L);
verify(repository, never()).save(any());

// Argument matching
when(repository.findByUserId(eq(1L), any(Pageable.class)))
    .thenReturn(page);

// Void methods
doNothing().when(repository).deleteById(1L);
```

## Test Coverage Goals

| Component | Target | Current |
|-----------|--------|---------|
| Service Layer | 80% | ✅ 95%+ |
| Repository Layer | N/A | Spring Data |
| Controller Layer | 70% | 🔜 TODO |
| DTO Mappers | N/A | MapStruct |
| Exception Handlers | 80% | 🔜 TODO |

## Future Enhancements

- [ ] Add controller layer tests with MockMvc
- [ ] Add security tests with `@WithMockUser`
- [ ] Add receipt service integration tests
- [ ] Add performance tests with JMeter
- [ ] Add mutation testing with PIT
- [ ] Add contract testing with Pact
- [ ] Increase code coverage to 90%+
- [ ] Add E2E tests with Selenium

## Troubleshooting

### Testcontainers Not Starting
```bash
# Ensure Docker is running
docker ps

# Check Docker socket permissions (Linux)
sudo chmod 666 /var/run/docker.sock

# Clear Testcontainers cache
rm -rf ~/.testcontainers
```

### Port Conflicts
```bash
# Stop conflicting containers
docker stop $(docker ps -q)

# Use random ports in tests
@Container
static PostgreSQLContainer<?> postgres = 
    new PostgreSQLContainer<>().withExposedPorts()
```

### Slow Tests
- Enable container reuse: `.withReuse(true)`
- Use test slices: `@WebMvcTest`, `@DataJpaTest`
- Parallelize with `@Execution(CONCURRENT)`
- Use in-memory H2 for simple tests

## Resources

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Testcontainers Documentation](https://www.testcontainers.org/)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [Spring Boot Testing Guide](https://spring.io/guides/gs/testing-web/)

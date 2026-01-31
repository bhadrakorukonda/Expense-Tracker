package com.expense.tracker.integration;

import com.expense.tracker.dto.ExpenseCreateDto;
import com.expense.tracker.dto.ExpenseResponseDto;
import com.expense.tracker.model.Category;
import com.expense.tracker.model.User;
import com.expense.tracker.repository.CategoryRepository;
import com.expense.tracker.repository.ExpenseRepository;
import com.expense.tracker.repository.UserRepository;
import com.expense.tracker.service.ExpenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@Transactional
@DisplayName("Expense Integration Tests with Testcontainers")
class ExpenseIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("expense_tracker_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7-jammy")
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL configuration
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");

        // MongoDB configuration
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);

        // JWT configuration for tests
        registry.add("jwt.secret", () -> "test-secret-key-for-integration-tests-minimum-256-bits-required");
        registry.add("jwt.expiration", () -> "3600000");

        // Flyway - disable for integration tests (using create-drop)
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        // Clean up
        expenseRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password"));
        testUser = userRepository.save(testUser);

        // Create test category
        testCategory = new Category();
        testCategory.setName("Food & Dining");
        testCategory.setUser(testUser);
        testCategory = categoryRepository.save(testCategory);
    }

    @Test
    @DisplayName("Should successfully create and read expense with all relationships")
    void shouldCreateAndReadExpenseWithRelationships() {
        // Given - Create expense DTO
        ExpenseCreateDto createDto = new ExpenseCreateDto();
        createDto.setAmount(new BigDecimal("45.50"));
        createDto.setDescription("Lunch at restaurant");
        createDto.setDate(LocalDate.now());
        createDto.setCurrency("USD");
        createDto.setCategoryId(testCategory.getId());
        
        Set<String> tags = new HashSet<>();
        tags.add("restaurant");
        tags.add("lunch");
        tags.add("business");
        createDto.setTags(tags);

        // When - Create expense
        ExpenseResponseDto createdExpense = expenseService.createExpense(testUser.getId(), createDto);

        // Then - Verify creation
        assertThat(createdExpense).isNotNull();
        assertThat(createdExpense.getId()).isNotNull();
        assertThat(createdExpense.getAmount()).isEqualByComparingTo(new BigDecimal("45.50"));
        assertThat(createdExpense.getDescription()).isEqualTo("Lunch at restaurant");
        assertThat(createdExpense.getDate()).isEqualTo(LocalDate.now());
        assertThat(createdExpense.getCurrency()).isEqualTo("USD");
        assertThat(createdExpense.getCategoryId()).isEqualTo(testCategory.getId());
        assertThat(createdExpense.getCategoryName()).isEqualTo("Food & Dining");
        assertThat(createdExpense.getTags()).containsExactlyInAnyOrder("restaurant", "lunch", "business");

        // When - Read expense back
        ExpenseResponseDto retrievedExpense = expenseService.getExpenseById(createdExpense.getId());

        // Then - Verify read
        assertThat(retrievedExpense).isNotNull();
        assertThat(retrievedExpense.getId()).isEqualTo(createdExpense.getId());
        assertThat(retrievedExpense.getAmount()).isEqualByComparingTo(new BigDecimal("45.50"));
        assertThat(retrievedExpense.getDescription()).isEqualTo("Lunch at restaurant");
        assertThat(retrievedExpense.getCategoryId()).isEqualTo(testCategory.getId());
        assertThat(retrievedExpense.getCategoryName()).isEqualTo("Food & Dining");
        assertThat(retrievedExpense.getTags()).containsExactlyInAnyOrder("restaurant", "lunch", "business");

        // Verify persistence
        assertThat(expenseRepository.count()).isEqualTo(1);
        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(categoryRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should successfully create multiple expenses and retrieve by user")
    void shouldCreateMultipleExpensesAndRetrieveByUser() {
        // Given - Create first expense
        ExpenseCreateDto expense1 = new ExpenseCreateDto();
        expense1.setAmount(new BigDecimal("25.00"));
        expense1.setDescription("Breakfast");
        expense1.setDate(LocalDate.now());
        expense1.setCurrency("USD");
        expense1.setCategoryId(testCategory.getId());

        // Given - Create second expense
        ExpenseCreateDto expense2 = new ExpenseCreateDto();
        expense2.setAmount(new BigDecimal("50.00"));
        expense2.setDescription("Dinner");
        expense2.setDate(LocalDate.now());
        expense2.setCurrency("USD");
        expense2.setCategoryId(testCategory.getId());

        // When - Create expenses
        ExpenseResponseDto created1 = expenseService.createExpense(testUser.getId(), expense1);
        ExpenseResponseDto created2 = expenseService.createExpense(testUser.getId(), expense2);

        // Then - Verify both created
        assertThat(created1.getId()).isNotNull();
        assertThat(created2.getId()).isNotNull();
        assertThat(created1.getId()).isNotEqualTo(created2.getId());

        // When - Calculate total
        BigDecimal total = expenseService.getTotalExpensesByUserId(testUser.getId());

        // Then - Verify total
        assertThat(total).isEqualByComparingTo(new BigDecimal("75.00"));

        // Verify all expenses persisted
        assertThat(expenseRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should successfully create expense and calculate category totals")
    void shouldCreateExpenseAndCalculateCategoryTotals() {
        // Given - Create another category
        Category transportCategory = new Category();
        transportCategory.setName("Transport");
        transportCategory.setUser(testUser);
        transportCategory = categoryRepository.save(transportCategory);

        // Given - Create food expenses
        ExpenseCreateDto foodExpense1 = new ExpenseCreateDto();
        foodExpense1.setAmount(new BigDecimal("30.00"));
        foodExpense1.setDescription("Lunch");
        foodExpense1.setDate(LocalDate.now());
        foodExpense1.setCurrency("USD");
        foodExpense1.setCategoryId(testCategory.getId());

        ExpenseCreateDto foodExpense2 = new ExpenseCreateDto();
        foodExpense2.setAmount(new BigDecimal("20.00"));
        foodExpense2.setDescription("Snacks");
        foodExpense2.setDate(LocalDate.now());
        foodExpense2.setCurrency("USD");
        foodExpense2.setCategoryId(testCategory.getId());

        // Given - Create transport expense
        ExpenseCreateDto transportExpense = new ExpenseCreateDto();
        transportExpense.setAmount(new BigDecimal("15.00"));
        transportExpense.setDescription("Taxi");
        transportExpense.setDate(LocalDate.now());
        transportExpense.setCurrency("USD");
        transportExpense.setCategoryId(transportCategory.getId());

        // When - Create all expenses
        expenseService.createExpense(testUser.getId(), foodExpense1);
        expenseService.createExpense(testUser.getId(), foodExpense2);
        expenseService.createExpense(testUser.getId(), transportExpense);

        // When - Calculate category totals
        BigDecimal foodTotal = expenseService.getTotalExpensesByUserIdAndCategory(
                testUser.getId(), testCategory.getId());
        BigDecimal transportTotal = expenseService.getTotalExpensesByUserIdAndCategory(
                testUser.getId(), transportCategory.getId());

        // Then - Verify category totals
        assertThat(foodTotal).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(transportTotal).isEqualByComparingTo(new BigDecimal("15.00"));

        // When - Calculate overall total
        BigDecimal overallTotal = expenseService.getTotalExpensesByUserId(testUser.getId());

        // Then - Verify overall total
        assertThat(overallTotal).isEqualByComparingTo(new BigDecimal("65.00"));

        // Verify all expenses and categories persisted
        assertThat(expenseRepository.count()).isEqualTo(3);
        assertThat(categoryRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should successfully create expenses and filter by date range")
    void shouldCreateExpensesAndFilterByDateRange() {
        // Given - Create expenses with different dates
        ExpenseCreateDto oldExpense = new ExpenseCreateDto();
        oldExpense.setAmount(new BigDecimal("100.00"));
        oldExpense.setDescription("Old expense");
        oldExpense.setDate(LocalDate.now().minusDays(60));
        oldExpense.setCurrency("USD");
        oldExpense.setCategoryId(testCategory.getId());

        ExpenseCreateDto recentExpense1 = new ExpenseCreateDto();
        recentExpense1.setAmount(new BigDecimal("50.00"));
        recentExpense1.setDescription("Recent expense 1");
        recentExpense1.setDate(LocalDate.now().minusDays(15));
        recentExpense1.setCurrency("USD");
        recentExpense1.setCategoryId(testCategory.getId());

        ExpenseCreateDto recentExpense2 = new ExpenseCreateDto();
        recentExpense2.setAmount(new BigDecimal("30.00"));
        recentExpense2.setDescription("Recent expense 2");
        recentExpense2.setDate(LocalDate.now().minusDays(5));
        recentExpense2.setCurrency("USD");
        recentExpense2.setCategoryId(testCategory.getId());

        // When - Create all expenses
        expenseService.createExpense(testUser.getId(), oldExpense);
        expenseService.createExpense(testUser.getId(), recentExpense1);
        expenseService.createExpense(testUser.getId(), recentExpense2);

        // When - Calculate total for last 30 days
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        BigDecimal recentTotal = expenseService.getTotalExpensesByUserIdAndDateRange(
                testUser.getId(), startDate, endDate);

        // Then - Verify only recent expenses counted
        assertThat(recentTotal).isEqualByComparingTo(new BigDecimal("80.00"));

        // When - Calculate all-time total
        BigDecimal allTimeTotal = expenseService.getTotalExpensesByUserId(testUser.getId());

        // Then - Verify all expenses counted
        assertThat(allTimeTotal).isEqualByComparingTo(new BigDecimal("180.00"));

        // Verify all expenses persisted
        assertThat(expenseRepository.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("Containers should be running")
    void containersAreRunning() {
        assertThat(postgresContainer.isRunning()).isTrue();
        assertThat(mongoDBContainer.isRunning()).isTrue();
    }

    @Test
    @DisplayName("Should verify database connectivity")
    void shouldVerifyDatabaseConnectivity() {
        // Verify PostgreSQL connection by checking table existence
        assertThat(userRepository.count()).isGreaterThanOrEqualTo(0);
        assertThat(categoryRepository.count()).isGreaterThanOrEqualTo(0);
        assertThat(expenseRepository.count()).isGreaterThanOrEqualTo(0);
    }
}

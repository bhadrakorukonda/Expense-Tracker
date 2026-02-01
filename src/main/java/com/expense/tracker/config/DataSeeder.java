package com.expense.tracker.config;

import com.expense.tracker.model.Category;
import com.expense.tracker.model.Expense;
import com.expense.tracker.model.User;
import com.expense.tracker.repository.CategoryRepository;
import com.expense.tracker.repository.ExpenseRepository;
import com.expense.tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Data seeder for local development
 * Seeds sample users, categories, and expenses
 * Only runs when 'dev' profile is active
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String[] CATEGORY_NAMES = {
            "Food & Dining", "Groceries", "Transportation", "Shopping", "Entertainment",
            "Healthcare", "Utilities", "Rent", "Insurance", "Education",
            "Travel", "Fitness", "Personal Care", "Gifts", "Subscriptions"
    };

    private static final String[] CURRENCIES = {"USD", "EUR", "GBP", "CAD", "AUD"};

    private static final Map<String, List<String>> EXPENSE_DESCRIPTIONS = createExpenseDescriptions();

    private static Map<String, List<String>> createExpenseDescriptions() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("Food & Dining", List.of("Lunch at restaurant", "Coffee shop", "Dinner with friends", "Fast food", "Pizza delivery"));
        map.put("Groceries", List.of("Weekly grocery shopping", "Fresh vegetables", "Meat and dairy", "Snacks and beverages", "Organic produce"));
        map.put("Transportation", List.of("Gas station", "Uber ride", "Public transit pass", "Car maintenance", "Parking fee"));
        map.put("Shopping", List.of("Clothing store", "Electronics purchase", "Home goods", "Online shopping", "Department store"));
        map.put("Entertainment", List.of("Movie tickets", "Concert", "Streaming service", "Gaming", "Sports event"));
        map.put("Healthcare", List.of("Pharmacy", "Doctor visit", "Dental checkup", "Health supplements", "Medical supplies"));
        map.put("Utilities", List.of("Electricity bill", "Water bill", "Internet service", "Phone bill", "Gas utility"));
        map.put("Rent", List.of("Monthly rent", "Property tax", "HOA fees", "Renters insurance", "Security deposit"));
        map.put("Insurance", List.of("Car insurance", "Health insurance", "Life insurance", "Home insurance", "Dental insurance"));
        map.put("Education", List.of("Textbooks", "Online course", "School supplies", "Tuition payment", "Workshop fee"));
        map.put("Travel", List.of("Flight tickets", "Hotel booking", "Car rental", "Travel insurance", "Vacation package"));
        map.put("Fitness", List.of("Gym membership", "Yoga class", "Sports equipment", "Personal trainer", "Fitness app"));
        map.put("Personal Care", List.of("Haircut", "Spa treatment", "Cosmetics", "Skincare products", "Salon visit"));
        map.put("Gifts", List.of("Birthday gift", "Wedding present", "Holiday gifts", "Thank you gift", "Anniversary present"));
        map.put("Subscriptions", List.of("Netflix", "Spotify", "Amazon Prime", "Magazine subscription", "Cloud storage"));
        return map;
    }

    private static final String[] TAGS = {
            "essential", "recurring", "one-time", "luxury", "emergency",
            "business", "personal", "tax-deductible", "weekly", "monthly"
    };

    @Override
    @Transactional
    public void run(String... args) {
        log.info("=".repeat(80));
        log.info("Starting data seeding for DEV profile...");
        log.info("=".repeat(80));

        // Check if data already exists
        if (userRepository.count() > 0) {
            log.info("Database already contains data. Skipping seeding.");
            log.info("Current counts - Users: {}, Categories: {}, Expenses: {}",
                    userRepository.count(),
                    categoryRepository.count(),
                    expenseRepository.count());
            return;
        }

        try {
            // Seed users
            List<User> users = seedUsers();
            log.info("✅ Created {} users", users.size());

            // Seed categories for each user
            Map<User, List<Category>> categoriesByUser = seedCategories(users);
            log.info("✅ Created {} categories across all users", 
                    categoriesByUser.values().stream().mapToInt(List::size).sum());

            // Seed expenses for each user
            int totalExpenses = seedExpenses(categoriesByUser);
            log.info("✅ Created {} expenses across all users", totalExpenses);

            log.info("=".repeat(80));
            log.info("Data seeding completed successfully!");
            log.info("Test accounts:");
            users.forEach(user -> log.info("  📧 {} - password: password123", user.getEmail()));
            log.info("=".repeat(80));

        } catch (Exception e) {
            log.error("❌ Error during data seeding", e);
            throw new RuntimeException("Data seeding failed", e);
        }
    }

    /**
     * Seed sample users
     */
    private List<User> seedUsers() {
        List<User> users = new ArrayList<>();

        // Create main test users
        users.add(createUser("john.doe@example.com", "John Doe", "password123"));
        users.add(createUser("jane.smith@example.com", "Jane Smith", "password123"));
        users.add(createUser("bob.wilson@example.com", "Bob Wilson", "password123"));

        return userRepository.saveAll(users);
    }

    /**
     * Create a user with encoded password
     */
    private User createUser(String email, String name, String password) {
        return User.builder()
                .email(email)
                .name(name)
                .password(passwordEncoder.encode(password))
                .build();
    }

    /**
     * Seed categories for each user
     */
    private Map<User, List<Category>> seedCategories(List<User> users) {
        Map<User, List<Category>> categoriesByUser = new HashMap<>();
        Random random = new Random();

        for (User user : users) {
            List<Category> userCategories = new ArrayList<>();

            // Each user gets 8-12 random categories
            int categoryCount = 8 + random.nextInt(5);
            Set<String> selectedCategories = new HashSet<>();

            while (selectedCategories.size() < categoryCount) {
                String categoryName = CATEGORY_NAMES[random.nextInt(CATEGORY_NAMES.length)];
                selectedCategories.add(categoryName);
            }

            for (String categoryName : selectedCategories) {
                Category category = Category.builder()
                        .name(categoryName)
                        .user(user)
                        .build();
                userCategories.add(category);
            }

            List<Category> savedCategories = categoryRepository.saveAll(userCategories);
            categoriesByUser.put(user, savedCategories);
        }

        return categoriesByUser;
    }

    /**
     * Seed random expenses for each user
     */
    private int seedExpenses(Map<User, List<Category>> categoriesByUser) {
        Random random = new Random();
        int totalExpenses = 0;

        for (Map.Entry<User, List<Category>> entry : categoriesByUser.entrySet()) {
            User user = entry.getKey();
            List<Category> categories = entry.getValue();

            // Generate 50-100 expenses per user over the last 90 days
            int expenseCount = 50 + random.nextInt(51);
            List<Expense> expenses = new ArrayList<>();

            for (int i = 0; i < expenseCount; i++) {
                Category category = categories.get(random.nextInt(categories.size()));
                String currency = CURRENCIES[random.nextInt(CURRENCIES.length)];

                // Generate amount between 5 and 500
                BigDecimal amount = BigDecimal.valueOf(5 + random.nextDouble() * 495)
                        .setScale(2, BigDecimal.ROUND_HALF_UP);

                // Random date within last 90 days
                LocalDate date = LocalDate.now().minusDays(random.nextInt(90));

                // Get random description based on category
                String description = getRandomDescription(category.getName(), random);

                // Random tags (0-3 tags)
                Set<String> expenseTags = getRandomTags(random);

                Expense expense = Expense.builder()
                        .user(user)
                        .category(category)
                        .amount(amount)
                        .currency(currency)
                        .date(date)
                        .description(description)
                        .tags(expenseTags)
                        .build();

                expenses.add(expense);
            }

            expenseRepository.saveAll(expenses);
            totalExpenses += expenses.size();

            log.info("  Created {} expenses for user: {}", expenses.size(), user.getEmail());
        }

        return totalExpenses;
    }

    /**
     * Get random description based on category
     */
    private String getRandomDescription(String categoryName, Random random) {
        List<String> descriptions = EXPENSE_DESCRIPTIONS.getOrDefault(categoryName, 
                List.of("General expense", "Purchase", "Payment", "Transaction", "Service"));
        return descriptions.get(random.nextInt(descriptions.size()));
    }

    /**
     * Get 0-3 random tags
     */
    private Set<String> getRandomTags(Random random) {
        Set<String> tags = new HashSet<>();
        int tagCount = random.nextInt(4); // 0 to 3 tags

        while (tags.size() < tagCount && tags.size() < TAGS.length) {
            tags.add(TAGS[random.nextInt(TAGS.length)]);
        }

        return tags;
    }
}

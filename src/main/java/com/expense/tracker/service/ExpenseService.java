package com.expense.tracker.service;

import com.expense.tracker.dto.CategoryReportDto;
import com.expense.tracker.dto.ExpenseCreateDto;
import com.expense.tracker.dto.ExpenseResponseDto;
import com.expense.tracker.dto.ExpenseUpdateDto;
import com.expense.tracker.dto.MonthlyReportDto;
import com.expense.tracker.exception.ResourceNotFoundException;
import com.expense.tracker.mapper.EntityMapper;
import com.expense.tracker.model.Category;
import com.expense.tracker.model.Expense;
import com.expense.tracker.model.User;
import com.expense.tracker.repository.CategoryRepository;
import com.expense.tracker.repository.ExpenseRepository;
import com.expense.tracker.repository.UserRepository;
import com.expense.tracker.repository.specification.ExpenseSpecification;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing expenses
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Validated
@Transactional(readOnly = true)
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EntityMapper entityMapper;

    /**
     * Create a new expense
     *
     * @param userId the ID of the user creating the expense
     * @param expenseCreateDto the expense data
     * @return the created expense
     * @throws ResourceNotFoundException if user or category not found
     */
    @Transactional
    public ExpenseResponseDto createExpense(Long userId, @Valid ExpenseCreateDto expenseCreateDto) {
        log.info("Creating expense for user ID: {}", userId);
        
        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Validate category exists and belongs to the user
        Category category = categoryRepository.findById(expenseCreateDto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", expenseCreateDto.getCategoryId()));
        
        if (!category.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Category does not belong to the user");
        }
        
        // Create expense entity
        Expense expense = entityMapper.toExpense(expenseCreateDto);
        expense.setUser(user);
        expense.setCategory(category);
        
        // Initialize tags if null
        if (expense.getTags() == null) {
            expense.setTags(java.util.Collections.emptySet());
        }
        
        // Save expense
        Expense savedExpense = expenseRepository.save(expense);
        log.info("Expense created with ID: {}", savedExpense.getId());
        
        return entityMapper.toExpenseResponseDto(savedExpense);
    }

    /**
     * Update an existing expense
     *
     * @param expenseId the ID of the expense to update
     * @param expenseUpdateDto the updated expense data
     * @return the updated expense
     * @throws ResourceNotFoundException if expense not found
     */
    @Transactional
    public ExpenseResponseDto updateExpense(Long expenseId, @Valid ExpenseUpdateDto expenseUpdateDto) {
        log.info("Updating expense ID: {}", expenseId);
        
        // Find existing expense
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", expenseId));
        
        // If category is being updated, validate it belongs to the same user
        if (expenseUpdateDto.getCategoryId() != null) {
            Category newCategory = categoryRepository.findById(expenseUpdateDto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", expenseUpdateDto.getCategoryId()));
            
            if (!newCategory.getUser().getId().equals(expense.getUser().getId())) {
                throw new IllegalArgumentException("Category does not belong to the expense owner");
            }
            
            expense.setCategory(newCategory);
        }
        
        // Update other fields using mapper (only non-null values)
        entityMapper.updateExpenseFromDto(expenseUpdateDto, expense);
        
        // Save updated expense
        Expense updatedExpense = expenseRepository.save(expense);
        log.info("Expense updated with ID: {}", updatedExpense.getId());
        
        return entityMapper.toExpenseResponseDto(updatedExpense);
    }

    /**
     * Delete an expense
     *
     * @param expenseId the ID of the expense to delete
     * @throws ResourceNotFoundException if expense not found
     */
    @Transactional
    public void deleteExpense(Long expenseId) {
        log.info("Deleting expense ID: {}", expenseId);
        
        if (!expenseRepository.existsById(expenseId)) {
            throw new ResourceNotFoundException("Expense", "id", expenseId);
        }
        
        expenseRepository.deleteById(expenseId);
        log.info("Expense deleted with ID: {}", expenseId);
    }

    /**
     * Get an expense by ID
     *
     * @param expenseId the ID of the expense
     * @return the expense
     * @throws ResourceNotFoundException if expense not found
     */
    public ExpenseResponseDto getExpenseById(Long expenseId) {
        log.debug("Fetching expense ID: {}", expenseId);
        
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", expenseId));
        
        return entityMapper.toExpenseResponseDto(expense);
    }

    /**
     * Search expenses with multiple filters and pagination using JPA Specifications
     *
     * @param userId the ID of the user
     * @param fromDate optional start date filter
     * @param toDate optional end date filter
     * @param categoryId optional category filter
     * @param minAmount optional minimum amount filter
     * @param maxAmount optional maximum amount filter
     * @param searchText optional text search (searches description, tags, category name)
     * @param currency optional currency filter
     * @param tag optional tag filter
     * @param pageable pagination information
     * @return page of matching expenses
     */
    public Page<ExpenseResponseDto> searchExpenses(
            Long userId,
            Optional<LocalDate> fromDate,
            Optional<LocalDate> toDate,
            Optional<Long> categoryId,
            Optional<BigDecimal> minAmount,
            Optional<BigDecimal> maxAmount,
            Optional<String> searchText,
            Optional<String> currency,
            Optional<String> tag,
            Pageable pageable) {
        
        log.debug("Searching expenses for user ID: {} with filters - fromDate: {}, toDate: {}, categoryId: {}, " +
                "minAmount: {}, maxAmount: {}, searchText: {}, currency: {}, tag: {}",
                userId, fromDate, toDate, categoryId, minAmount, maxAmount, searchText, currency, tag);
        
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        
        // Validate category belongs to user if specified
        if (categoryId.isPresent()) {
            Category category = categoryRepository.findById(categoryId.get())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId.get()));
            
            if (!category.getUser().getId().equals(userId)) {
                throw new IllegalArgumentException("Category does not belong to the user");
            }
        }
        
        // Validate date range
        if (fromDate.isPresent() && toDate.isPresent() && fromDate.get().isAfter(toDate.get())) {
            throw new IllegalArgumentException("From date cannot be after to date");
        }
        
        // Validate amount range
        if (minAmount.isPresent() && maxAmount.isPresent() && minAmount.get().compareTo(maxAmount.get()) > 0) {
            throw new IllegalArgumentException("Minimum amount cannot be greater than maximum amount");
        }
        
        // Build dynamic specification
        Specification<Expense> specification = ExpenseSpecification.buildSpecification(
                userId,
                fromDate.orElse(null),
                toDate.orElse(null),
                categoryId.orElse(null),
                minAmount.orElse(null),
                maxAmount.orElse(null),
                searchText.orElse(null),
                currency.orElse(null),
                tag.orElse(null)
        );
        
        // Apply specification and fetch expenses
        Page<Expense> expenses = expenseRepository.findAll(specification, pageable);
        
        return expenses.map(entityMapper::toExpenseResponseDto);
    }

    /**
     * Search expenses with multiple filters (backward compatibility method)
     */
    public Page<ExpenseResponseDto> searchExpenses(
            Long userId,
            Optional<LocalDate> fromDate,
            Optional<LocalDate> toDate,
            Optional<Long> categoryId,
            Optional<BigDecimal> minAmount,
            Optional<BigDecimal> maxAmount,
            Pageable pageable) {
        
        return searchExpenses(userId, fromDate, toDate, categoryId, minAmount, maxAmount, 
                Optional.empty(), Optional.empty(), Optional.empty(), pageable);
    }

    /**
     * Helper method to find expenses with various filters (deprecated - using Specifications now)
     */
    @Deprecated
    private Page<Expense> findExpensesWithFilters(
            Long userId,
            Optional<LocalDate> fromDate,
            Optional<LocalDate> toDate,
            Optional<Long> categoryId,
            Optional<BigDecimal> minAmount,
            Optional<BigDecimal> maxAmount,
            Pageable pageable) {
        
        // Apply different query strategies based on provided filters
        if (categoryId.isPresent() && fromDate.isPresent() && toDate.isPresent()) {
            // Category + date range filter
            return expenseRepository.findByUserIdAndCategoryIdAndDateBetween(
                    userId, categoryId.get(), fromDate.get(), toDate.get(), pageable);
        } else if (categoryId.isPresent()) {
            // Category only filter
            return expenseRepository.findByUserIdAndCategoryId(userId, categoryId.get(), pageable);
        } else if (fromDate.isPresent() && toDate.isPresent()) {
            // Date range only filter
            return expenseRepository.findByUserIdAndDateBetween(userId, fromDate.get(), toDate.get(), pageable);
        } else if (minAmount.isPresent() && maxAmount.isPresent()) {
            // Amount range filter
            return expenseRepository.findByUserIdAndAmountBetween(userId, minAmount.get(), maxAmount.get(), pageable);
        } else if (minAmount.isPresent()) {
            // Minimum amount filter
            return expenseRepository.findByUserIdAndAmountGreaterThanEqual(userId, minAmount.get(), pageable);
        } else {
            // No filters, just user ID
            return expenseRepository.findByUserId(userId, pageable);
        }
    }

    /**
     * Get all expenses for a user with pagination
     *
     * @param userId the user ID
     * @param pageable pagination information
     * @return page of expenses
     */
    public Page<ExpenseResponseDto> getExpensesByUserId(Long userId, Pageable pageable) {
        log.debug("Fetching expenses for user ID: {}", userId);
        
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        
        Page<Expense> expenses = expenseRepository.findByUserId(userId, pageable);
        return expenses.map(entityMapper::toExpenseResponseDto);
    }

    /**
     * Calculate total expenses for a user
     *
     * @param userId the user ID
     * @return total amount
     */
    public BigDecimal getTotalExpensesByUserId(Long userId) {
        log.debug("Calculating total expenses for user ID: {}", userId);
        
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        
        BigDecimal total = expenseRepository.sumAmountByUserId(userId);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Calculate total expenses for a user within a date range
     *
     * @param userId the user ID
     * @param startDate the start date
     * @param endDate the end date
     * @return total amount
     */
    public BigDecimal getTotalExpensesByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        log.debug("Calculating total expenses for user ID: {} from {} to {}", userId, startDate, endDate);
        
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        
        BigDecimal total = expenseRepository.sumAmountByUserIdAndDateBetween(userId, startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Calculate total expenses for a user by category
     *
     * @param userId the user ID
     * @param categoryId the category ID
     * @return total amount
     */
    public BigDecimal getTotalExpensesByUserIdAndCategory(Long userId, Long categoryId) {
        log.debug("Calculating total expenses for user ID: {} and category ID: {}", userId, categoryId);
        
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
        
        if (!category.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Category does not belong to the user");
        }
        
        BigDecimal total = expenseRepository.sumAmountByUserIdAndCategoryId(userId, categoryId);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Get monthly expense report for a specific year
     *
     * @param userId the user ID
     * @param year the year
     * @return list of monthly totals
     */
    public List<MonthlyReportDto> getMonthlyReport(Long userId, Integer year) {
        log.debug("Generating monthly report for user ID: {} and year: {}", userId, year);
        
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        
        if (year == null || year < 1900 || year > 2100) {
            throw new IllegalArgumentException("Invalid year provided");
        }
        
        List<Object[]> results = expenseRepository.getMonthlyTotals(userId, year);
        
        return results.stream()
                .map(row -> new MonthlyReportDto(
                        (Integer) row[0],  // year
                        (Integer) row[1],  // month
                        (BigDecimal) row[2], // total
                        (Long) row[3],     // count
                        (String) row[4]    // currency
                ))
                .collect(Collectors.toList());
    }

    /**
     * Get category expense report within date range
     *
     * @param userId the user ID
     * @param startDate the start date
     * @param endDate the end date
     * @return list of category totals with percentages
     */
    public List<CategoryReportDto> getCategoryReport(Long userId, LocalDate startDate, LocalDate endDate) {
        log.debug("Generating category report for user ID: {} from {} to {}", userId, startDate, endDate);
        
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        
        List<Object[]> results = expenseRepository.getCategoryTotals(userId, startDate, endDate);
        
        // Calculate grand total for percentage calculations
        BigDecimal grandTotal = results.stream()
                .map(row -> (BigDecimal) row[4])
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return results.stream()
                .map(row -> {
                    Long categoryId = (Long) row[0];
                    String categoryName = (String) row[1];
                    String categoryColor = (String) row[2];
                    String categoryIcon = (String) row[3];
                    BigDecimal total = (BigDecimal) row[4];
                    Long count = (Long) row[5];
                    
                    // Calculate percentage
                    Double percentage = grandTotal.compareTo(BigDecimal.ZERO) > 0
                            ? total.divide(grandTotal, 4, RoundingMode.HALF_UP)
                                   .multiply(BigDecimal.valueOf(100))
                                   .doubleValue()
                            : 0.0;
                    
                    return new CategoryReportDto(
                            categoryId,
                            categoryName,
                            categoryColor,
                            categoryIcon,
                            total,
                            count,
                            percentage
                    );
                })
                .collect(Collectors.toList());
    }
}

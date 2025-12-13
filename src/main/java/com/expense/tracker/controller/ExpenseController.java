package com.expense.tracker.controller;

import com.expense.tracker.dto.ExpenseCreateDto;
import com.expense.tracker.dto.ExpenseResponseDto;
import com.expense.tracker.dto.ExpenseUpdateDto;
import com.expense.tracker.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

/**
 * REST controller for expense management
 */
@RestController
@RequestMapping("/api/v1/users/{userId}/expenses")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Expense Management", description = "APIs for managing user expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    /**
     * Create a new expense
     *
     * @param userId the user ID
     * @param expenseCreateDto the expense data
     * @return the created expense
     */
    @PostMapping
    @Operation(summary = "Create a new expense", description = "Creates a new expense for the specified user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Expense created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "User or category not found")
    })
    public ResponseEntity<ExpenseResponseDto> createExpense(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Expense data", required = true)
            @Valid @RequestBody ExpenseCreateDto expenseCreateDto) {
        
        log.info("POST /api/v1/users/{}/expenses - Creating expense", userId);
        
        // Ensure userId in path matches userId in request body
        if (!userId.equals(expenseCreateDto.getUserId())) {
            throw new IllegalArgumentException("User ID in path must match user ID in request body");
        }
        
        ExpenseResponseDto createdExpense = expenseService.createExpense(userId, expenseCreateDto);
        return new ResponseEntity<>(createdExpense, HttpStatus.CREATED);
    }

    /**
     * Update an existing expense
     *
     * @param userId the user ID
     * @param expenseId the expense ID
     * @param expenseUpdateDto the updated expense data
     * @return the updated expense
     */
    @PutMapping("/{expenseId}")
    @Operation(summary = "Update an expense", description = "Updates an existing expense")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expense updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Expense or category not found")
    })
    public ResponseEntity<ExpenseResponseDto> updateExpense(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Expense ID", required = true)
            @PathVariable Long expenseId,
            @Parameter(description = "Updated expense data", required = true)
            @Valid @RequestBody ExpenseUpdateDto expenseUpdateDto) {
        
        log.info("PUT /api/v1/users/{}/expenses/{} - Updating expense", userId, expenseId);
        
        // Verify expense belongs to the user
        ExpenseResponseDto existingExpense = expenseService.getExpenseById(expenseId);
        if (!existingExpense.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Expense does not belong to the specified user");
        }
        
        ExpenseResponseDto updatedExpense = expenseService.updateExpense(expenseId, expenseUpdateDto);
        return ResponseEntity.ok(updatedExpense);
    }

    /**
     * Delete an expense
     *
     * @param userId the user ID
     * @param expenseId the expense ID
     * @return no content
     */
    @DeleteMapping("/{expenseId}")
    @Operation(summary = "Delete an expense", description = "Deletes an existing expense")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Expense deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Expense not found")
    })
    public ResponseEntity<Void> deleteExpense(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Expense ID", required = true)
            @PathVariable Long expenseId) {
        
        log.info("DELETE /api/v1/users/{}/expenses/{} - Deleting expense", userId, expenseId);
        
        // Verify expense belongs to the user
        ExpenseResponseDto existingExpense = expenseService.getExpenseById(expenseId);
        if (!existingExpense.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Expense does not belong to the specified user");
        }
        
        expenseService.deleteExpense(expenseId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get an expense by ID
     *
     * @param userId the user ID
     * @param expenseId the expense ID
     * @return the expense
     */
    @GetMapping("/{expenseId}")
    @Operation(summary = "Get expense by ID", description = "Retrieves a specific expense by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expense retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Expense not found")
    })
    public ResponseEntity<ExpenseResponseDto> getExpenseById(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Expense ID", required = true)
            @PathVariable Long expenseId) {
        
        log.info("GET /api/v1/users/{}/expenses/{} - Fetching expense", userId, expenseId);
        
        ExpenseResponseDto expense = expenseService.getExpenseById(expenseId);
        
        // Verify expense belongs to the user
        if (!expense.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Expense does not belong to the specified user");
        }
        
        return ResponseEntity.ok(expense);
    }

    /**
     * List expenses with pagination and filters
     *
     * @param userId the user ID
     * @param fromDate optional start date filter (format: yyyy-MM-dd)
     * @param toDate optional end date filter (format: yyyy-MM-dd)
     * @param categoryId optional category filter
     * @param minAmount optional minimum amount filter
     * @param maxAmount optional maximum amount filter
     * @param q optional search text (searches description, tags, category name)
     * @param currency optional currency filter
     * @param tag optional tag filter
     * @param pageable pagination parameters
     * @return page of expenses
     */
    @GetMapping
    @Operation(summary = "List expenses", description = "Retrieves expenses with pagination and optional filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expenses retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid filter parameters"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Page<ExpenseResponseDto>> listExpenses(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            
            @Parameter(description = "Start date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            
            @Parameter(description = "End date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            
            @Parameter(description = "Category ID")
            @RequestParam(required = false) Long categoryId,
            
            @Parameter(description = "Minimum amount")
            @RequestParam(required = false) BigDecimal minAmount,
            
            @Parameter(description = "Maximum amount")
            @RequestParam(required = false) BigDecimal maxAmount,
            
            @Parameter(description = "Search text (searches description, tags, category name)")
            @RequestParam(required = false) String q,
            
            @Parameter(description = "Currency code (e.g., USD, EUR)")
            @RequestParam(required = false) String currency,
            
            @Parameter(description = "Filter by specific tag")
            @RequestParam(required = false) String tag,
            
            @Parameter(description = "Pagination parameters (page, size, sort)")
            @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("GET /api/v1/users/{}/expenses - Listing expenses with filters", userId);
        log.debug("Filters - fromDate: {}, toDate: {}, categoryId: {}, minAmount: {}, maxAmount: {}, q: {}, " +
                "currency: {}, tag: {}, page: {}, size: {}, sort: {}",
                fromDate, toDate, categoryId, minAmount, maxAmount, q, currency, tag, 
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        
        Page<ExpenseResponseDto> expenses = expenseService.searchExpenses(
                userId,
                Optional.ofNullable(fromDate),
                Optional.ofNullable(toDate),
                Optional.ofNullable(categoryId),
                Optional.ofNullable(minAmount),
                Optional.ofNullable(maxAmount),
                Optional.ofNullable(q),
                Optional.ofNullable(currency),
                Optional.ofNullable(tag),
                pageable
        );
        
        return ResponseEntity.ok(expenses);
    }

    /**
     * Get total expenses for a user
     *
     * @param userId the user ID
     * @return total amount
     */
    @GetMapping("/total")
    @Operation(summary = "Get total expenses", description = "Calculates the total amount of all expenses for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Total calculated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<BigDecimal> getTotalExpenses(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId) {
        
        log.info("GET /api/v1/users/{}/expenses/total - Calculating total expenses", userId);
        
        BigDecimal total = expenseService.getTotalExpensesByUserId(userId);
        return ResponseEntity.ok(total);
    }

    /**
     * Get total expenses for a date range
     *
     * @param userId the user ID
     * @param fromDate start date
     * @param toDate end date
     * @return total amount
     */
    @GetMapping("/total/date-range")
    @Operation(summary = "Get total expenses by date range", 
               description = "Calculates the total amount of expenses within a date range")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Total calculated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date range"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<BigDecimal> getTotalExpensesByDateRange(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            
            @Parameter(description = "Start date (yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            
            @Parameter(description = "End date (yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        
        log.info("GET /api/v1/users/{}/expenses/total/date-range - Calculating total from {} to {}", 
                userId, fromDate, toDate);
        
        BigDecimal total = expenseService.getTotalExpensesByUserIdAndDateRange(userId, fromDate, toDate);
        return ResponseEntity.ok(total);
    }

    /**
     * Get total expenses by category
     *
     * @param userId the user ID
     * @param categoryId the category ID
     * @return total amount
     */
    @GetMapping("/total/category/{categoryId}")
    @Operation(summary = "Get total expenses by category", 
               description = "Calculates the total amount of expenses for a specific category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Total calculated successfully"),
            @ApiResponse(responseCode = "404", description = "User or category not found")
    })
    public ResponseEntity<BigDecimal> getTotalExpensesByCategory(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            
            @Parameter(description = "Category ID", required = true)
            @PathVariable Long categoryId) {
        
        log.info("GET /api/v1/users/{}/expenses/total/category/{} - Calculating total by category", 
                userId, categoryId);
        
        BigDecimal total = expenseService.getTotalExpensesByUserIdAndCategory(userId, categoryId);
        return ResponseEntity.ok(total);
    }
}

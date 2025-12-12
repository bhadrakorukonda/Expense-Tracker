package com.expense.tracker.repository;

import com.expense.tracker.model.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    /**
     * Find all expenses for a specific user
     * @param userId the user ID
     * @return list of expenses
     */
    List<Expense> findByUserId(Long userId);

    /**
     * Find all expenses for a specific user with pagination
     * @param userId the user ID
     * @param pageable pagination information
     * @return page of expenses
     */
    Page<Expense> findByUserId(Long userId, Pageable pageable);

    /**
     * Find expenses by user ID and date range
     * @param userId the user ID
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @param pageable pagination information
     * @return page of expenses
     */
    Page<Expense> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * Find expenses by user ID and date range (without pagination)
     * @param userId the user ID
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of expenses
     */
    List<Expense> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * Find expenses by category ID
     * @param categoryId the category ID
     * @param pageable pagination information
     * @return page of expenses
     */
    Page<Expense> findByCategoryId(Long categoryId, Pageable pageable);

    /**
     * Find expenses by user ID and category ID
     * @param userId the user ID
     * @param categoryId the category ID
     * @param pageable pagination information
     * @return page of expenses
     */
    Page<Expense> findByUserIdAndCategoryId(Long userId, Long categoryId, Pageable pageable);

    /**
     * Find expenses by user ID, category ID, and date range
     * @param userId the user ID
     * @param categoryId the category ID
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @param pageable pagination information
     * @return page of expenses
     */
    Page<Expense> findByUserIdAndCategoryIdAndDateBetween(
        Long userId, Long categoryId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * Find expenses by user ID and currency
     * @param userId the user ID
     * @param currency the currency code
     * @param pageable pagination information
     * @return page of expenses
     */
    Page<Expense> findByUserIdAndCurrency(Long userId, String currency, Pageable pageable);

    /**
     * Find expenses by user ID where amount is greater than or equal to specified amount
     * @param userId the user ID
     * @param minAmount the minimum amount
     * @param pageable pagination information
     * @return page of expenses
     */
    Page<Expense> findByUserIdAndAmountGreaterThanEqual(Long userId, BigDecimal minAmount, Pageable pageable);

    /**
     * Find expenses by user ID where amount is between min and max
     * @param userId the user ID
     * @param minAmount the minimum amount
     * @param maxAmount the maximum amount
     * @param pageable pagination information
     * @return page of expenses
     */
    Page<Expense> findByUserIdAndAmountBetween(Long userId, BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);

    /**
     * Find expenses containing specific tag
     * @param userId the user ID
     * @param tag the tag to search for
     * @param pageable pagination information
     * @return page of expenses
     */
    @Query("SELECT e FROM Expense e JOIN e.tags t WHERE e.user.id = :userId AND t = :tag")
    Page<Expense> findByUserIdAndTag(@Param("userId") Long userId, @Param("tag") String tag, Pageable pageable);

    /**
     * Calculate total expenses for a user
     * @param userId the user ID
     * @return sum of all expenses
     */
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user.id = :userId")
    BigDecimal sumAmountByUserId(@Param("userId") Long userId);

    /**
     * Calculate total expenses for a user by currency
     * @param userId the user ID
     * @param currency the currency code
     * @return sum of expenses in specified currency
     */
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user.id = :userId AND e.currency = :currency")
    BigDecimal sumAmountByUserIdAndCurrency(@Param("userId") Long userId, @Param("currency") String currency);

    /**
     * Calculate total expenses for a user within date range
     * @param userId the user ID
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return sum of expenses in date range
     */
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user.id = :userId AND e.date BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserIdAndDateBetween(
        @Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Calculate total expenses for a user by category
     * @param userId the user ID
     * @param categoryId the category ID
     * @return sum of expenses for category
     */
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user.id = :userId AND e.category.id = :categoryId")
    BigDecimal sumAmountByUserIdAndCategoryId(@Param("userId") Long userId, @Param("categoryId") Long categoryId);

    /**
     * Count expenses for a user
     * @param userId the user ID
     * @return count of expenses
     */
    long countByUserId(Long userId);

    /**
     * Count expenses for a user within date range
     * @param userId the user ID
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return count of expenses
     */
    long countByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * Delete all expenses for a specific category
     * @param categoryId the category ID
     */
    void deleteByCategoryId(Long categoryId);
}

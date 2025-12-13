package com.expense.tracker.repository;

import com.expense.tracker.model.ReceiptDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for receipt documents in MongoDB
 */
@Repository
public interface ReceiptRepository extends MongoRepository<ReceiptDocument, String> {

    /**
     * Find all receipts for a user
     */
    List<ReceiptDocument> findByUserId(Long userId);

    /**
     * Find all receipts for a user with pagination
     */
    Page<ReceiptDocument> findByUserId(Long userId, Pageable pageable);

    /**
     * Find receipt by expense ID
     */
    Optional<ReceiptDocument> findByExpenseId(Long expenseId);

    /**
     * Find all receipts for a specific expense
     */
    List<ReceiptDocument> findAllByExpenseId(Long expenseId);

    /**
     * Find receipts by user ID and expense ID is null (unassigned receipts)
     */
    List<ReceiptDocument> findByUserIdAndExpenseIdIsNull(Long userId);

    /**
     * Find receipts by user ID and expense ID is null with pagination
     */
    Page<ReceiptDocument> findByUserIdAndExpenseIdIsNull(Long userId, Pageable pageable);

    /**
     * Check if receipt exists for user
     */
    boolean existsByIdAndUserId(String id, Long userId);

    /**
     * Delete receipts by expense ID
     */
    void deleteByExpenseId(Long expenseId);

    /**
     * Count receipts for a user
     */
    long countByUserId(Long userId);

    /**
     * Count unassigned receipts for a user
     */
    long countByUserIdAndExpenseIdIsNull(Long userId);
}

package com.expense.tracker.repository.specification;

import com.expense.tracker.model.Category;
import com.expense.tracker.model.Expense;
import com.expense.tracker.model.User;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for dynamic Expense queries
 */
public class ExpenseSpecification {

    /**
     * Create specification for filtering expenses by user ID
     */
    public static Specification<Expense> hasUserId(Long userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("user").get("id"), userId);
        };
    }

    /**
     * Create specification for filtering by date range
     */
    public static Specification<Expense> hasDateBetween(LocalDate fromDate, LocalDate toDate) {
        return (root, query, criteriaBuilder) -> {
            if (fromDate == null && toDate == null) {
                return criteriaBuilder.conjunction();
            }
            
            if (fromDate != null && toDate != null) {
                return criteriaBuilder.between(root.get("date"), fromDate, toDate);
            } else if (fromDate != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("date"), fromDate);
            } else {
                return criteriaBuilder.lessThanOrEqualTo(root.get("date"), toDate);
            }
        };
    }

    /**
     * Create specification for filtering by category ID
     */
    public static Specification<Expense> hasCategoryId(Long categoryId) {
        return (root, query, criteriaBuilder) -> {
            if (categoryId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("category").get("id"), categoryId);
        };
    }

    /**
     * Create specification for filtering by amount range
     */
    public static Specification<Expense> hasAmountBetween(BigDecimal minAmount, BigDecimal maxAmount) {
        return (root, query, criteriaBuilder) -> {
            if (minAmount == null && maxAmount == null) {
                return criteriaBuilder.conjunction();
            }
            
            if (minAmount != null && maxAmount != null) {
                return criteriaBuilder.between(root.get("amount"), minAmount, maxAmount);
            } else if (minAmount != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), minAmount);
            } else {
                return criteriaBuilder.lessThanOrEqualTo(root.get("amount"), maxAmount);
            }
        };
    }

    /**
     * Create specification for text search across description and tags
     */
    public static Specification<Expense> hasSearchText(String searchText) {
        return (root, query, criteriaBuilder) -> {
            if (searchText == null || searchText.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            
            String likePattern = "%" + searchText.toLowerCase() + "%";
            
            // Search in description
            Predicate descriptionPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")), 
                    likePattern
            );
            
            // Search in tags (ElementCollection)
            Join<Expense, String> tagsJoin = root.join("tags", JoinType.LEFT);
            Predicate tagsPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(tagsJoin), 
                    likePattern
            );
            
            // Search in category name
            Predicate categoryPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("category").get("name")), 
                    likePattern
            );
            
            // Combine with OR
            return criteriaBuilder.or(descriptionPredicate, tagsPredicate, categoryPredicate);
        };
    }

    /**
     * Create specification for filtering by currency
     */
    public static Specification<Expense> hasCurrency(String currency) {
        return (root, query, criteriaBuilder) -> {
            if (currency == null || currency.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(
                    criteriaBuilder.upper(root.get("currency")), 
                    currency.toUpperCase()
            );
        };
    }

    /**
     * Create specification for filtering by specific tag
     */
    public static Specification<Expense> hasTag(String tag) {
        return (root, query, criteriaBuilder) -> {
            if (tag == null || tag.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            
            Join<Expense, String> tagsJoin = root.join("tags", JoinType.INNER);
            return criteriaBuilder.equal(
                    criteriaBuilder.lower(tagsJoin), 
                    tag.toLowerCase()
            );
        };
    }

    /**
     * Combine multiple specifications with AND
     */
    public static Specification<Expense> buildSpecification(
            Long userId,
            LocalDate fromDate,
            LocalDate toDate,
            Long categoryId,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String searchText,
            String currency,
            String tag) {
        
        return Specification.where(hasUserId(userId))
                .and(hasDateBetween(fromDate, toDate))
                .and(hasCategoryId(categoryId))
                .and(hasAmountBetween(minAmount, maxAmount))
                .and(hasSearchText(searchText))
                .and(hasCurrency(currency))
                .and(hasTag(tag));
    }
}

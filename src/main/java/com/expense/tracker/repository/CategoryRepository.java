package com.expense.tracker.repository;

import com.expense.tracker.model.Category;
import com.expense.tracker.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Find all categories for a specific user
     * @param user the user
     * @return list of categories
     */
    List<Category> findByUser(User user);

    /**
     * Find all categories for a specific user by user ID
     * @param userId the user ID
     * @return list of categories
     */
    List<Category> findByUserId(Long userId);

    /**
     * Find all categories for a specific user with pagination
     * @param userId the user ID
     * @param pageable pagination information
     * @return page of categories
     */
    Page<Category> findByUserId(Long userId, Pageable pageable);

    /**
     * Find a category by name for a specific user
     * @param name the category name
     * @param userId the user ID
     * @return Optional containing the category if found
     */
    Optional<Category> findByNameAndUserId(String name, Long userId);

    /**
     * Find categories by name containing a search term (case-insensitive)
     * @param name the search term
     * @param userId the user ID
     * @return list of matching categories
     */
    List<Category> findByNameContainingIgnoreCaseAndUserId(String name, Long userId);

    /**
     * Check if a category exists with the given name for a user
     * @param name the category name
     * @param userId the user ID
     * @return true if exists, false otherwise
     */
    boolean existsByNameAndUserId(String name, Long userId);

    /**
     * Count categories for a specific user
     * @param userId the user ID
     * @return count of categories
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE c.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
}

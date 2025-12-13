package com.expense.tracker.mapper;

import com.expense.tracker.dto.*;
import com.expense.tracker.model.Category;
import com.expense.tracker.model.Expense;
import com.expense.tracker.model.User;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MapStruct mapper for converting between entities and DTOs
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
@Component
public interface EntityMapper {

    // ==================== User Mappings ====================

    /**
     * Convert User entity to UserDto
     */
    UserDto toUserDto(User user);

    /**
     * Convert list of User entities to list of UserDto
     */
    List<UserDto> toUserDtoList(List<User> users);

    /**
     * Convert UserDto to User entity
     */
    @Mapping(target = "password", ignore = true)
    User toUser(UserDto userDto);

    // ==================== Category Mappings ====================

    /**
     * Convert Category entity to CategoryDto
     */
    @Mapping(source = "user.id", target = "userId")
    CategoryDto toCategoryDto(Category category);

    /**
     * Convert list of Category entities to list of CategoryDto
     */
    List<CategoryDto> toCategoryDtoList(List<Category> categories);

    /**
     * Convert CategoryDto to Category entity
     */
    @Mapping(source = "userId", target = "user.id")
    Category toCategory(CategoryDto categoryDto);

    // ==================== Expense Mappings ====================

    /**
     * Convert Expense entity to ExpenseResponseDto
     */
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.name", target = "userName")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    ExpenseResponseDto toExpenseResponseDto(Expense expense);

    /**
     * Convert list of Expense entities to list of ExpenseResponseDto
     */
    List<ExpenseResponseDto> toExpenseResponseDtoList(List<Expense> expenses);

    /**
     * Convert ExpenseCreateDto to Expense entity
     */
    @Mapping(source = "userId", target = "user.id")
    @Mapping(source = "categoryId", target = "category.id")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Expense toExpense(ExpenseCreateDto expenseCreateDto);

    /**
     * Update existing Expense entity from ExpenseUpdateDto
     * Only updates non-null fields from the DTO
     */
    @Mapping(source = "categoryId", target = "category.id")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateExpenseFromDto(ExpenseUpdateDto expenseUpdateDto, @MappingTarget Expense expense);

    /**
     * Partial update for Expense - only updates provided fields
     */
    @Mapping(source = "categoryId", target = "category.id")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Expense partialUpdate(ExpenseUpdateDto expenseUpdateDto, @MappingTarget Expense expense);
}

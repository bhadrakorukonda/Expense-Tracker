package com.expense.tracker.service;

import com.expense.tracker.dto.ExpenseCreateDto;
import com.expense.tracker.dto.ExpenseResponseDto;
import com.expense.tracker.dto.ExpenseUpdateDto;
import com.expense.tracker.exception.ResourceNotFoundException;
import com.expense.tracker.mapper.EntityMapper;
import com.expense.tracker.model.Category;
import com.expense.tracker.model.Expense;
import com.expense.tracker.model.User;
import com.expense.tracker.repository.CategoryRepository;
import com.expense.tracker.repository.ExpenseRepository;
import com.expense.tracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExpenseService Unit Tests")
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private EntityMapper entityMapper;

    @InjectMocks
    private ExpenseService expenseService;

    private User testUser;
    private Category testCategory;
    private Expense testExpense;
    private ExpenseCreateDto createDto;
    private ExpenseUpdateDto updateDto;
    private ExpenseResponseDto responseDto;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");

        // Create test category
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Food");
        testCategory.setUser(testUser);

        // Create test expense
        testExpense = new Expense();
        testExpense.setId(1L);
        testExpense.setAmount(new BigDecimal("50.00"));
        testExpense.setDescription("Lunch");
        testExpense.setDate(LocalDate.now());
        testExpense.setCurrency("USD");
        testExpense.setUser(testUser);
        testExpense.setCategory(testCategory);
        testExpense.setTags(new HashSet<>(Arrays.asList("restaurant", "lunch")));

        // Create DTOs
        createDto = new ExpenseCreateDto();
        createDto.setAmount(new BigDecimal("50.00"));
        createDto.setDescription("Lunch");
        createDto.setDate(LocalDate.now());
        createDto.setCurrency("USD");
        createDto.setCategoryId(1L);
        createDto.setTags(new HashSet<>(Arrays.asList("restaurant", "lunch")));

        updateDto = new ExpenseUpdateDto();
        updateDto.setAmount(new BigDecimal("60.00"));
        updateDto.setDescription("Updated Lunch");
        updateDto.setCategoryId(1L);

        responseDto = new ExpenseResponseDto();
        responseDto.setId(1L);
        responseDto.setAmount(new BigDecimal("50.00"));
        responseDto.setDescription("Lunch");
        responseDto.setDate(LocalDate.now());
        responseDto.setCurrency("USD");
        responseDto.setCategoryId(1L);
        responseDto.setCategoryName("Food");
        responseDto.setTags(new HashSet<>(Arrays.asList("restaurant", "lunch")));
    }

    @Nested
    @DisplayName("Create Expense Tests")
    class CreateExpenseTests {

        @Test
        @DisplayName("Should create expense successfully when all data is valid")
        void shouldCreateExpenseSuccessfully() {
            // Arrange
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(entityMapper.toExpense(createDto)).thenReturn(testExpense);
            when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);
            when(entityMapper.toExpenseResponseDto(testExpense)).thenReturn(responseDto);

            // Act
            ExpenseResponseDto result = expenseService.createExpense(1L, createDto);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
            assertThat(result.getDescription()).isEqualTo("Lunch");

            verify(userRepository).findById(1L);
            verify(categoryRepository).findById(1L);
            verify(expenseRepository).save(any(Expense.class));
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Arrange
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> expenseService.createExpense(999L, createDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User")
                    .hasMessageContaining("999");

            verify(userRepository).findById(999L);
            verify(categoryRepository, never()).findById(anyLong());
            verify(expenseRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when category not found")
        void shouldThrowExceptionWhenCategoryNotFound() {
            // Arrange
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(categoryRepository.findById(999L)).thenReturn(Optional.empty());
            createDto.setCategoryId(999L);

            // Act & Assert
            assertThatThrownBy(() -> expenseService.createExpense(1L, createDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Category")
                    .hasMessageContaining("999");

            verify(userRepository).findById(1L);
            verify(categoryRepository).findById(999L);
            verify(expenseRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when category belongs to different user")
        void shouldThrowExceptionWhenCategoryBelongsToDifferentUser() {
            // Arrange
            User anotherUser = new User();
            anotherUser.setId(2L);
            anotherUser.setUsername("another");
            anotherUser.setEmail("another@example.com");

            Category anotherCategory = new Category();
            anotherCategory.setId(2L);
            anotherCategory.setName("Transport");
            anotherCategory.setUser(anotherUser);

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(anotherCategory));
            createDto.setCategoryId(2L);

            // Act & Assert
            assertThatThrownBy(() -> expenseService.createExpense(1L, createDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Category does not belong to the user");

            verify(userRepository).findById(1L);
            verify(categoryRepository).findById(2L);
            verify(expenseRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Update Expense Tests")
    class UpdateExpenseTests {

        @Test
        @DisplayName("Should update expense successfully")
        void shouldUpdateExpenseSuccessfully() {
            // Arrange
            when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);
            when(entityMapper.toExpenseResponseDto(testExpense)).thenReturn(responseDto);
            doNothing().when(entityMapper).updateExpenseFromDto(updateDto, testExpense);

            // Act
            ExpenseResponseDto result = expenseService.updateExpense(1L, updateDto);

            // Assert
            assertThat(result).isNotNull();
            verify(expenseRepository).findById(1L);
            verify(categoryRepository).findById(1L);
            verify(entityMapper).updateExpenseFromDto(updateDto, testExpense);
            verify(expenseRepository).save(testExpense);
        }

        @Test
        @DisplayName("Should throw exception when expense not found")
        void shouldThrowExceptionWhenExpenseNotFound() {
            // Arrange
            when(expenseRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> expenseService.updateExpense(999L, updateDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Expense")
                    .hasMessageContaining("999");

            verify(expenseRepository).findById(999L);
            verify(expenseRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when updating with category from different user")
        void shouldThrowExceptionWhenUpdatingWithCategoryFromDifferentUser() {
            // Arrange
            User anotherUser = new User();
            anotherUser.setId(2L);

            Category anotherCategory = new Category();
            anotherCategory.setId(2L);
            anotherCategory.setUser(anotherUser);

            when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(anotherCategory));
            updateDto.setCategoryId(2L);

            // Act & Assert
            assertThatThrownBy(() -> expenseService.updateExpense(1L, updateDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Category does not belong to the expense owner");

            verify(expenseRepository).findById(1L);
            verify(categoryRepository).findById(2L);
            verify(expenseRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Delete Expense Tests")
    class DeleteExpenseTests {

        @Test
        @DisplayName("Should delete expense successfully")
        void shouldDeleteExpenseSuccessfully() {
            // Arrange
            when(expenseRepository.existsById(1L)).thenReturn(true);
            doNothing().when(expenseRepository).deleteById(1L);

            // Act
            expenseService.deleteExpense(1L);

            // Assert
            verify(expenseRepository).existsById(1L);
            verify(expenseRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw exception when expense not found")
        void shouldThrowExceptionWhenExpenseNotFound() {
            // Arrange
            when(expenseRepository.existsById(999L)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> expenseService.deleteExpense(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Expense")
                    .hasMessageContaining("999");

            verify(expenseRepository).existsById(999L);
            verify(expenseRepository, never()).deleteById(anyLong());
        }
    }

    @Nested
    @DisplayName("Get Expense Tests")
    class GetExpenseTests {

        @Test
        @DisplayName("Should get expense by ID successfully")
        void shouldGetExpenseByIdSuccessfully() {
            // Arrange
            when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));
            when(entityMapper.toExpenseResponseDto(testExpense)).thenReturn(responseDto);

            // Act
            ExpenseResponseDto result = expenseService.getExpenseById(1L);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(expenseRepository).findById(1L);
            verify(entityMapper).toExpenseResponseDto(testExpense);
        }

        @Test
        @DisplayName("Should throw exception when expense not found")
        void shouldThrowExceptionWhenExpenseNotFound() {
            // Arrange
            when(expenseRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> expenseService.getExpenseById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Expense")
                    .hasMessageContaining("999");

            verify(expenseRepository).findById(999L);
        }
    }

    @Nested
    @DisplayName("Get Expenses By User Tests")
    class GetExpensesByUserTests {

        @Test
        @DisplayName("Should get expenses by user ID with pagination")
        void shouldGetExpensesByUserIdWithPagination() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<Expense> expenses = Arrays.asList(testExpense);
            Page<Expense> expensePage = new PageImpl<>(expenses, pageable, 1);

            when(userRepository.existsById(1L)).thenReturn(true);
            when(expenseRepository.findByUserId(1L, pageable)).thenReturn(expensePage);
            when(entityMapper.toExpenseResponseDto(testExpense)).thenReturn(responseDto);

            // Act
            Page<ExpenseResponseDto> result = expenseService.getExpensesByUserId(1L, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(userRepository).existsById(1L);
            verify(expenseRepository).findByUserId(1L, pageable);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            when(userRepository.existsById(999L)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> expenseService.getExpensesByUserId(999L, pageable))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User")
                    .hasMessageContaining("999");

            verify(userRepository).existsById(999L);
            verify(expenseRepository, never()).findByUserId(anyLong(), any());
        }
    }

    @Nested
    @DisplayName("Calculate Total Expenses Tests")
    class CalculateTotalExpensesTests {

        @Test
        @DisplayName("Should calculate total expenses by user ID")
        void shouldCalculateTotalExpensesByUserId() {
            // Arrange
            BigDecimal expectedTotal = new BigDecimal("150.00");
            when(userRepository.existsById(1L)).thenReturn(true);
            when(expenseRepository.sumAmountByUserId(1L)).thenReturn(expectedTotal);

            // Act
            BigDecimal result = expenseService.getTotalExpensesByUserId(1L);

            // Assert
            assertThat(result).isEqualByComparingTo(expectedTotal);
            verify(userRepository).existsById(1L);
            verify(expenseRepository).sumAmountByUserId(1L);
        }

        @Test
        @DisplayName("Should return zero when no expenses found")
        void shouldReturnZeroWhenNoExpensesFound() {
            // Arrange
            when(userRepository.existsById(1L)).thenReturn(true);
            when(expenseRepository.sumAmountByUserId(1L)).thenReturn(null);

            // Act
            BigDecimal result = expenseService.getTotalExpensesByUserId(1L);

            // Assert
            assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
            verify(expenseRepository).sumAmountByUserId(1L);
        }

        @Test
        @DisplayName("Should calculate total expenses by date range")
        void shouldCalculateTotalExpensesByDateRange() {
            // Arrange
            LocalDate startDate = LocalDate.now().minusDays(30);
            LocalDate endDate = LocalDate.now();
            BigDecimal expectedTotal = new BigDecimal("200.00");

            when(userRepository.existsById(1L)).thenReturn(true);
            when(expenseRepository.sumAmountByUserIdAndDateBetween(1L, startDate, endDate))
                    .thenReturn(expectedTotal);

            // Act
            BigDecimal result = expenseService.getTotalExpensesByUserIdAndDateRange(1L, startDate, endDate);

            // Assert
            assertThat(result).isEqualByComparingTo(expectedTotal);
            verify(expenseRepository).sumAmountByUserIdAndDateBetween(1L, startDate, endDate);
        }

        @Test
        @DisplayName("Should throw exception when start date is after end date")
        void shouldThrowExceptionWhenStartDateIsAfterEndDate() {
            // Arrange
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = LocalDate.now().minusDays(30);

            when(userRepository.existsById(1L)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> 
                    expenseService.getTotalExpensesByUserIdAndDateRange(1L, startDate, endDate))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Start date cannot be after end date");

            verify(expenseRepository, never()).sumAmountByUserIdAndDateBetween(anyLong(), any(), any());
        }

        @Test
        @DisplayName("Should calculate total expenses by category")
        void shouldCalculateTotalExpensesByCategory() {
            // Arrange
            BigDecimal expectedTotal = new BigDecimal("75.00");

            when(userRepository.existsById(1L)).thenReturn(true);
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(expenseRepository.sumAmountByUserIdAndCategoryId(1L, 1L)).thenReturn(expectedTotal);

            // Act
            BigDecimal result = expenseService.getTotalExpensesByUserIdAndCategory(1L, 1L);

            // Assert
            assertThat(result).isEqualByComparingTo(expectedTotal);
            verify(categoryRepository).findById(1L);
            verify(expenseRepository).sumAmountByUserIdAndCategoryId(1L, 1L);
        }

        @Test
        @DisplayName("Should throw exception when category belongs to different user")
        void shouldThrowExceptionWhenCategoryBelongsToDifferentUser() {
            // Arrange
            User anotherUser = new User();
            anotherUser.setId(2L);

            Category anotherCategory = new Category();
            anotherCategory.setId(2L);
            anotherCategory.setUser(anotherUser);

            when(userRepository.existsById(1L)).thenReturn(true);
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(anotherCategory));

            // Act & Assert
            assertThatThrownBy(() -> expenseService.getTotalExpensesByUserIdAndCategory(1L, 2L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Category does not belong to the user");

            verify(categoryRepository).findById(2L);
            verify(expenseRepository, never()).sumAmountByUserIdAndCategoryId(anyLong(), anyLong());
        }
    }

    @Nested
    @DisplayName("Search Expenses Tests")
    class SearchExpensesTests {

        @Test
        @DisplayName("Should search expenses with filters")
        void shouldSearchExpensesWithFilters() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<Expense> expenses = Arrays.asList(testExpense);
            Page<Expense> expensePage = new PageImpl<>(expenses, pageable, 1);

            when(userRepository.existsById(1L)).thenReturn(true);
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(expenseRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(expensePage);
            when(entityMapper.toExpenseResponseDto(testExpense)).thenReturn(responseDto);

            // Act
            Page<ExpenseResponseDto> result = expenseService.searchExpenses(
                    1L,
                    Optional.of(LocalDate.now().minusDays(30)),
                    Optional.of(LocalDate.now()),
                    Optional.of(1L),
                    Optional.of(new BigDecimal("10.00")),
                    Optional.of(new BigDecimal("100.00")),
                    Optional.of("lunch"),
                    Optional.of("USD"),
                    Optional.of("restaurant"),
                    pageable
            );

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(userRepository).existsById(1L);
            verify(categoryRepository).findById(1L);
            verify(expenseRepository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("Should throw exception when date range is invalid")
        void shouldThrowExceptionWhenDateRangeIsInvalid() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            LocalDate fromDate = LocalDate.now();
            LocalDate toDate = LocalDate.now().minusDays(30);

            when(userRepository.existsById(1L)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> expenseService.searchExpenses(
                    1L,
                    Optional.of(fromDate),
                    Optional.of(toDate),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    pageable
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("From date cannot be after to date");

            verify(expenseRepository, never()).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should throw exception when amount range is invalid")
        void shouldThrowExceptionWhenAmountRangeIsInvalid() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);

            when(userRepository.existsById(1L)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> expenseService.searchExpenses(
                    1L,
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.of(new BigDecimal("100.00")),
                    Optional.of(new BigDecimal("10.00")),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    pageable
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Minimum amount cannot be greater than maximum amount");

            verify(expenseRepository, never()).findAll(any(Specification.class), any(Pageable.class));
        }
    }
}

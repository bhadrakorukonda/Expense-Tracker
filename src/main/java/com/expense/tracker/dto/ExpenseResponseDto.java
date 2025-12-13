package com.expense.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseResponseDto {

    private Long id;
    private Long userId;
    private String userName;
    private Long categoryId;
    private String categoryName;
    private BigDecimal amount;
    private String currency;
    private LocalDate date;
    private String description;
    private String receiptMongoId;
    private Set<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

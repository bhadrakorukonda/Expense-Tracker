package com.expense.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for category expense report
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryReportDto {
    private Long categoryId;
    private String categoryName;
    private String categoryColor;
    private String categoryIcon;
    private BigDecimal total;
    private Long expenseCount;
    private Double percentage;
}

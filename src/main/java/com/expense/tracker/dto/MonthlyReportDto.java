package com.expense.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for monthly expense report
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyReportDto {
    private Integer year;
    private Integer month;
    private BigDecimal total;
    private Long expenseCount;
    private String currency;
}

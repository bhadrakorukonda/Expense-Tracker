package com.expense.tracker.controller;

import com.expense.tracker.dto.CategoryReportDto;
import com.expense.tracker.dto.MonthlyReportDto;
import com.expense.tracker.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for expense reports and analytics
 */
@RestController
@RequestMapping("/api/v1/users/{userId}/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Expense Reports", description = "APIs for expense reports and analytics")
public class ReportController {

    private final ExpenseService expenseService;

    /**
     * Get monthly expense report for a specific year
     *
     * @param userId the user ID
     * @param year the year (e.g., 2026)
     * @return list of monthly totals
     */
    @GetMapping("/monthly")
    @Operation(
        summary = "Get monthly expense report",
        description = "Returns total expenses per month for the specified year, grouped by currency"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Report generated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid year provided"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<List<MonthlyReportDto>> getMonthlyReport(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            
            @Parameter(description = "Year (e.g., 2026)", required = true, example = "2026")
            @RequestParam Integer year) {
        
        log.info("Generating monthly report for user ID: {} and year: {}", userId, year);
        
        List<MonthlyReportDto> report = expenseService.getMonthlyReport(userId, year);
        
        return ResponseEntity.ok(report);
    }

    /**
     * Get category expense report within date range
     *
     * @param userId the user ID
     * @param from start date (inclusive)
     * @param to end date (inclusive)
     * @return list of category totals with percentages
     */
    @GetMapping("/category")
    @Operation(
        summary = "Get category expense report",
        description = "Returns expense totals by category within the specified date range, including percentages"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Report generated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid date range provided"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<List<CategoryReportDto>> getCategoryReport(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            
            @Parameter(description = "Start date (YYYY-MM-DD)", required = true, example = "2026-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            
            @Parameter(description = "End date (YYYY-MM-DD)", required = true, example = "2026-12-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        
        log.info("Generating category report for user ID: {} from {} to {}", userId, from, to);
        
        List<CategoryReportDto> report = expenseService.getCategoryReport(userId, from, to);
        
        return ResponseEntity.ok(report);
    }
}

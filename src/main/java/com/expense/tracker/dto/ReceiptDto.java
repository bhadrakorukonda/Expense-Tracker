package com.expense.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for receipt metadata
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptDto {

    private String id;
    private Long userId;
    private Long expenseId;
    private String fileName;
    private String mimeType;
    private Long fileSize;
    private String notes;
    private LocalDateTime uploadedAt;
    private LocalDateTime updatedAt;
}

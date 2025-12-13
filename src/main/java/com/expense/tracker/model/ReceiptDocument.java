package com.expense.tracker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * MongoDB document for storing receipt metadata
 * Actual file data is stored in GridFS
 */
@Document(collection = "receipts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptDocument {

    @Id
    private String id; // MongoDB ObjectId as String

    @Indexed
    private Long userId;

    @Indexed
    private Long expenseId; // Optional - can be null if receipt uploaded before expense created

    private String fileName;

    private String mimeType;

    private String gridFsFileId; // Reference to GridFS file

    private Long fileSize; // File size in bytes

    private String notes; // Free-form notes about the receipt

    @Builder.Default
    private LocalDateTime uploadedAt = LocalDateTime.now();

    private LocalDateTime updatedAt;
}

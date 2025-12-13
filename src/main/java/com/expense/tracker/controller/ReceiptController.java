package com.expense.tracker.controller;

import com.expense.tracker.dto.ReceiptDto;
import com.expense.tracker.service.ReceiptService;
import com.mongodb.client.gridfs.model.GridFSFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * REST controller for receipt management
 */
@RestController
@RequestMapping("/api/v1/users/{userId}/receipts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Receipt Management", description = "APIs for managing receipt files and metadata")
public class ReceiptController {

    private final ReceiptService receiptService;

    /**
     * Upload a receipt file
     *
     * @param userId the user ID
     * @param file the receipt file
     * @param expenseId optional expense ID to associate with
     * @param notes optional notes about the receipt
     * @return the created receipt metadata
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload receipt file", description = "Uploads a receipt file to MongoDB GridFS")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Receipt uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or file"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ReceiptDto> uploadReceipt(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            
            @Parameter(description = "Receipt file", required = true)
            @RequestParam("file") MultipartFile file,
            
            @Parameter(description = "Expense ID to associate with")
            @RequestParam(required = false) Long expenseId,
            
            @Parameter(description = "Notes about the receipt")
            @RequestParam(required = false) String notes) {
        
        log.info("POST /api/v1/users/{}/receipts - Uploading receipt file: {}", userId, file.getOriginalFilename());
        
        ReceiptDto receipt = receiptService.uploadReceipt(userId, file, expenseId, notes);
        
        return new ResponseEntity<>(receipt, HttpStatus.CREATED);
    }

    /**
     * Download/stream receipt file
     *
     * @param userId the user ID
     * @param receiptId the receipt ID
     * @return the receipt file stream
     */
    @GetMapping("/{receiptId}")
    @Operation(summary = "Download receipt file", description = "Downloads a receipt file from MongoDB GridFS")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Receipt downloaded successfully"),
            @ApiResponse(responseCode = "404", description = "Receipt not found")
    })
    public ResponseEntity<InputStreamResource> downloadReceipt(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            
            @Parameter(description = "Receipt ID", required = true)
            @PathVariable String receiptId) {
        
        log.info("GET /api/v1/users/{}/receipts/{} - Downloading receipt", userId, receiptId);
        
        // Get receipt metadata
        ReceiptDto receiptMetadata = receiptService.getReceiptMetadata(receiptId, userId);
        
        // Get file stream
        InputStream fileStream = receiptService.downloadReceipt(receiptId, userId);
        
        // Prepare response with appropriate headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                receiptMetadata.getMimeType() != null ? receiptMetadata.getMimeType() : MediaType.APPLICATION_OCTET_STREAM_VALUE
        ));
        headers.setContentLength(receiptMetadata.getFileSize());
        headers.setContentDispositionFormData("attachment", receiptMetadata.getFileName());
        
        return ResponseEntity
                .ok()
                .headers(headers)
                .body(new InputStreamResource(fileStream));
    }

    /**
     * Delete receipt
     *
     * @param userId the user ID
     * @param receiptId the receipt ID
     * @return no content
     */
    @DeleteMapping("/{receiptId}")
    @Operation(summary = "Delete receipt", description = "Deletes a receipt file and its metadata")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Receipt deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Receipt not found")
    })
    public ResponseEntity<Void> deleteReceipt(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            
            @Parameter(description = "Receipt ID", required = true)
            @PathVariable String receiptId) {
        
        log.info("DELETE /api/v1/users/{}/receipts/{} - Deleting receipt", userId, receiptId);
        
        receiptService.deleteReceipt(receiptId, userId);
        
        return ResponseEntity.noContent().build();
    }

    /**
     * Get receipt metadata
     *
     * @param userId the user ID
     * @param receiptId the receipt ID
     * @return receipt metadata
     */
    @GetMapping("/{receiptId}/metadata")
    @Operation(summary = "Get receipt metadata", description = "Retrieves receipt metadata without downloading the file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Metadata retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Receipt not found")
    })
    public ResponseEntity<ReceiptDto> getReceiptMetadata(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            
            @Parameter(description = "Receipt ID", required = true)
            @PathVariable String receiptId) {
        
        log.info("GET /api/v1/users/{}/receipts/{}/metadata - Fetching metadata", userId, receiptId);
        
        ReceiptDto receipt = receiptService.getReceiptMetadata(receiptId, userId);
        
        return ResponseEntity.ok(receipt);
    }

    /**
     * List all receipts for a user
     *
     * @param userId the user ID
     * @param pageable pagination parameters
     * @return page of receipt metadata
     */
    @GetMapping
    @Operation(summary = "List receipts", description = "Lists all receipts for a user with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Receipts retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Page<ReceiptDto>> listReceipts(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            
            @Parameter(description = "Pagination parameters (page, size, sort)")
            @PageableDefault(size = 20, sort = "uploadedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("GET /api/v1/users/{}/receipts - Listing receipts", userId);
        
        Page<ReceiptDto> receipts = receiptService.getReceiptsByUserId(userId, pageable);
        
        return ResponseEntity.ok(receipts);
    }

    /**
     * Update receipt metadata
     *
     * @param userId the user ID
     * @param receiptId the receipt ID
     * @param expenseId optional new expense ID
     * @param notes optional new notes
     * @return updated receipt metadata
     */
    @PatchMapping("/{receiptId}")
    @Operation(summary = "Update receipt metadata", description = "Updates receipt notes or links to an expense")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Receipt updated successfully"),
            @ApiResponse(responseCode = "404", description = "Receipt not found")
    })
    public ResponseEntity<ReceiptDto> updateReceipt(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            
            @Parameter(description = "Receipt ID", required = true)
            @PathVariable String receiptId,
            
            @Parameter(description = "Expense ID to link")
            @RequestParam(required = false) Long expenseId,
            
            @Parameter(description = "Notes about the receipt")
            @RequestParam(required = false) String notes) {
        
        log.info("PATCH /api/v1/users/{}/receipts/{} - Updating receipt metadata", userId, receiptId);
        
        ReceiptDto updatedReceipt = receiptService.updateReceiptMetadata(receiptId, userId, expenseId, notes);
        
        return ResponseEntity.ok(updatedReceipt);
    }

    /**
     * Get unassigned receipts
     *
     * @param userId the user ID
     * @param pageable pagination parameters
     * @return page of unassigned receipts
     */
    @GetMapping("/unassigned")
    @Operation(summary = "Get unassigned receipts", description = "Lists receipts not linked to any expense")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unassigned receipts retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Page<ReceiptDto>> getUnassignedReceipts(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            
            @Parameter(description = "Pagination parameters (page, size, sort)")
            @PageableDefault(size = 20, sort = "uploadedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("GET /api/v1/users/{}/receipts/unassigned - Fetching unassigned receipts", userId);
        
        Page<ReceiptDto> receipts = receiptService.getUnassignedReceipts(userId, pageable);
        
        return ResponseEntity.ok(receipts);
    }

    /**
     * Link receipt to expense
     *
     * @param userId the user ID
     * @param receiptId the receipt ID
     * @param expenseId the expense ID
     * @return updated receipt metadata
     */
    @PostMapping("/{receiptId}/link")
    @Operation(summary = "Link receipt to expense", description = "Associates a receipt with an expense")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Receipt linked successfully"),
            @ApiResponse(responseCode = "404", description = "Receipt or expense not found")
    })
    public ResponseEntity<ReceiptDto> linkReceiptToExpense(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            
            @Parameter(description = "Receipt ID", required = true)
            @PathVariable String receiptId,
            
            @Parameter(description = "Expense ID", required = true)
            @RequestParam Long expenseId) {
        
        log.info("POST /api/v1/users/{}/receipts/{}/link - Linking to expense {}", userId, receiptId, expenseId);
        
        ReceiptDto linkedReceipt = receiptService.linkReceiptToExpense(receiptId, expenseId, userId);
        
        return ResponseEntity.ok(linkedReceipt);
    }

    /**
     * Unlink receipt from expense
     *
     * @param userId the user ID
     * @param receiptId the receipt ID
     * @return updated receipt metadata
     */
    @PostMapping("/{receiptId}/unlink")
    @Operation(summary = "Unlink receipt from expense", description = "Removes association between receipt and expense")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Receipt unlinked successfully"),
            @ApiResponse(responseCode = "404", description = "Receipt not found")
    })
    public ResponseEntity<ReceiptDto> unlinkReceiptFromExpense(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            
            @Parameter(description = "Receipt ID", required = true)
            @PathVariable String receiptId) {
        
        log.info("POST /api/v1/users/{}/receipts/{}/unlink - Unlinking from expense", userId, receiptId);
        
        ReceiptDto unlinkedReceipt = receiptService.unlinkReceiptFromExpense(receiptId, userId);
        
        return ResponseEntity.ok(unlinkedReceipt);
    }

    /**
     * Get receipts for a specific expense
     *
     * @param userId the user ID
     * @param expenseId the expense ID
     * @return list of receipts
     */
    @GetMapping("/expense/{expenseId}")
    @Operation(summary = "Get receipts for expense", description = "Lists all receipts associated with a specific expense")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Receipts retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Expense not found")
    })
    public ResponseEntity<List<ReceiptDto>> getReceiptsByExpense(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            
            @Parameter(description = "Expense ID", required = true)
            @PathVariable Long expenseId) {
        
        log.info("GET /api/v1/users/{}/receipts/expense/{} - Fetching receipts for expense", userId, expenseId);
        
        List<ReceiptDto> receipts = receiptService.getAllReceiptsByExpenseId(expenseId, userId);
        
        return ResponseEntity.ok(receipts);
    }
}

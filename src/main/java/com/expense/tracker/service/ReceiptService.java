package com.expense.tracker.service;

import com.expense.tracker.dto.ReceiptDto;
import com.expense.tracker.exception.ResourceNotFoundException;
import com.expense.tracker.model.ReceiptDocument;
import com.expense.tracker.repository.ReceiptRepository;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing receipt documents in MongoDB with GridFS
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final GridFsTemplate gridFsTemplate;
    private final GridFsOperations gridFsOperations;

    /**
     * Upload a receipt file
     *
     * @param userId the user ID
     * @param file the file to upload
     * @param expenseId optional expense ID to associate with
     * @param notes optional notes about the receipt
     * @return the saved receipt metadata
     */
    @Transactional
    public ReceiptDto uploadReceipt(Long userId, MultipartFile file, Long expenseId, String notes) {
        log.info("Uploading receipt for user ID: {}, expense ID: {}, filename: {}", 
                userId, expenseId, file.getOriginalFilename());
        
        try {
            // Validate file
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File is empty");
            }
            
            // Store file in GridFS
            ObjectId gridFsFileId = gridFsTemplate.store(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    file.getContentType()
            );
            
            log.debug("Stored file in GridFS with ID: {}", gridFsFileId);
            
            // Create receipt document
            ReceiptDocument receiptDocument = ReceiptDocument.builder()
                    .userId(userId)
                    .expenseId(expenseId)
                    .fileName(file.getOriginalFilename())
                    .mimeType(file.getContentType())
                    .gridFsFileId(gridFsFileId.toString())
                    .fileSize(file.getSize())
                    .notes(notes)
                    .uploadedAt(LocalDateTime.now())
                    .build();
            
            ReceiptDocument savedReceipt = receiptRepository.save(receiptDocument);
            log.info("Receipt saved with ID: {}", savedReceipt.getId());
            
            return toDto(savedReceipt);
            
        } catch (IOException e) {
            log.error("Error uploading receipt: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload receipt: " + e.getMessage(), e);
        }
    }

    /**
     * Get receipt metadata by ID
     *
     * @param receiptId the receipt ID
     * @param userId the user ID (for authorization)
     * @return the receipt metadata
     */
    public ReceiptDto getReceiptMetadata(String receiptId, Long userId) {
        log.debug("Fetching receipt metadata for ID: {}, user ID: {}", receiptId, userId);
        
        ReceiptDocument receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt", "id", receiptId));
        
        // Verify ownership
        if (!receipt.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Receipt does not belong to the user");
        }
        
        return toDto(receipt);
    }

    /**
     * Download receipt file
     *
     * @param receiptId the receipt ID
     * @param userId the user ID (for authorization)
     * @return input stream of the file
     */
    public InputStream downloadReceipt(String receiptId, Long userId) {
        log.debug("Downloading receipt ID: {}, user ID: {}", receiptId, userId);
        
        ReceiptDocument receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt", "id", receiptId));
        
        // Verify ownership
        if (!receipt.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Receipt does not belong to the user");
        }
        
        // Retrieve file from GridFS
        GridFSFile gridFsFile = gridFsTemplate.findOne(
                new Query(Criteria.where("_id").is(new ObjectId(receipt.getGridFsFileId())))
        );
        
        if (gridFsFile == null) {
            throw new ResourceNotFoundException("File", "gridFsId", receipt.getGridFsFileId());
        }
        
        try {
            return gridFsOperations.getResource(gridFsFile).getInputStream();
        } catch (IOException e) {
            log.error("Error downloading receipt: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to download receipt: " + e.getMessage(), e);
        }
    }

    /**
     * Update receipt metadata
     *
     * @param receiptId the receipt ID
     * @param userId the user ID (for authorization)
     * @param expenseId optional new expense ID
     * @param notes optional new notes
     * @return updated receipt metadata
     */
    @Transactional
    public ReceiptDto updateReceiptMetadata(String receiptId, Long userId, Long expenseId, String notes) {
        log.info("Updating receipt metadata for ID: {}, user ID: {}", receiptId, userId);
        
        ReceiptDocument receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt", "id", receiptId));
        
        // Verify ownership
        if (!receipt.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Receipt does not belong to the user");
        }
        
        // Update fields
        if (expenseId != null) {
            receipt.setExpenseId(expenseId);
        }
        if (notes != null) {
            receipt.setNotes(notes);
        }
        receipt.setUpdatedAt(LocalDateTime.now());
        
        ReceiptDocument updatedReceipt = receiptRepository.save(receipt);
        log.info("Receipt metadata updated for ID: {}", receiptId);
        
        return toDto(updatedReceipt);
    }

    /**
     * Delete receipt
     *
     * @param receiptId the receipt ID
     * @param userId the user ID (for authorization)
     */
    @Transactional
    public void deleteReceipt(String receiptId, Long userId) {
        log.info("Deleting receipt ID: {}, user ID: {}", receiptId, userId);
        
        ReceiptDocument receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt", "id", receiptId));
        
        // Verify ownership
        if (!receipt.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Receipt does not belong to the user");
        }
        
        // Delete file from GridFS
        gridFsTemplate.delete(new Query(Criteria.where("_id").is(new ObjectId(receipt.getGridFsFileId()))));
        log.debug("Deleted file from GridFS: {}", receipt.getGridFsFileId());
        
        // Delete receipt document
        receiptRepository.delete(receipt);
        log.info("Receipt deleted: {}", receiptId);
    }

    /**
     * Get all receipts for a user
     *
     * @param userId the user ID
     * @param pageable pagination information
     * @return page of receipts
     */
    public Page<ReceiptDto> getReceiptsByUserId(Long userId, Pageable pageable) {
        log.debug("Fetching receipts for user ID: {}", userId);
        
        Page<ReceiptDocument> receipts = receiptRepository.findByUserId(userId, pageable);
        return receipts.map(this::toDto);
    }

    /**
     * Get all receipts for a user (without pagination)
     *
     * @param userId the user ID
     * @return list of receipts
     */
    public List<ReceiptDto> getReceiptsByUserId(Long userId) {
        log.debug("Fetching all receipts for user ID: {}", userId);
        
        List<ReceiptDocument> receipts = receiptRepository.findByUserId(userId);
        return receipts.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get receipt by expense ID
     *
     * @param expenseId the expense ID
     * @param userId the user ID (for authorization)
     * @return the receipt metadata
     */
    public ReceiptDto getReceiptByExpenseId(Long expenseId, Long userId) {
        log.debug("Fetching receipt for expense ID: {}, user ID: {}", expenseId, userId);
        
        ReceiptDocument receipt = receiptRepository.findByExpenseId(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt", "expenseId", expenseId));
        
        // Verify ownership
        if (!receipt.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Receipt does not belong to the user");
        }
        
        return toDto(receipt);
    }

    /**
     * Get all receipts for a specific expense
     *
     * @param expenseId the expense ID
     * @param userId the user ID (for authorization)
     * @return list of receipts
     */
    public List<ReceiptDto> getAllReceiptsByExpenseId(Long expenseId, Long userId) {
        log.debug("Fetching all receipts for expense ID: {}, user ID: {}", expenseId, userId);
        
        List<ReceiptDocument> receipts = receiptRepository.findAllByExpenseId(expenseId);
        
        // Verify all receipts belong to the user
        receipts.forEach(receipt -> {
            if (!receipt.getUserId().equals(userId)) {
                throw new IllegalArgumentException("Receipt does not belong to the user");
            }
        });
        
        return receipts.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get unassigned receipts (not linked to any expense)
     *
     * @param userId the user ID
     * @param pageable pagination information
     * @return page of unassigned receipts
     */
    public Page<ReceiptDto> getUnassignedReceipts(Long userId, Pageable pageable) {
        log.debug("Fetching unassigned receipts for user ID: {}", userId);
        
        Page<ReceiptDocument> receipts = receiptRepository.findByUserIdAndExpenseIdIsNull(userId, pageable);
        return receipts.map(this::toDto);
    }

    /**
     * Link receipt to an expense
     *
     * @param receiptId the receipt ID
     * @param expenseId the expense ID
     * @param userId the user ID (for authorization)
     * @return updated receipt metadata
     */
    @Transactional
    public ReceiptDto linkReceiptToExpense(String receiptId, Long expenseId, Long userId) {
        log.info("Linking receipt ID: {} to expense ID: {}", receiptId, expenseId);
        
        return updateReceiptMetadata(receiptId, userId, expenseId, null);
    }

    /**
     * Unlink receipt from expense
     *
     * @param receiptId the receipt ID
     * @param userId the user ID (for authorization)
     * @return updated receipt metadata
     */
    @Transactional
    public ReceiptDto unlinkReceiptFromExpense(String receiptId, Long userId) {
        log.info("Unlinking receipt ID: {} from expense", receiptId);
        
        ReceiptDocument receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt", "id", receiptId));
        
        // Verify ownership
        if (!receipt.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Receipt does not belong to the user");
        }
        
        receipt.setExpenseId(null);
        receipt.setUpdatedAt(LocalDateTime.now());
        
        ReceiptDocument updatedReceipt = receiptRepository.save(receipt);
        return toDto(updatedReceipt);
    }

    /**
     * Get receipt file metadata
     *
     * @param receiptId the receipt ID
     * @param userId the user ID (for authorization)
     * @return GridFS file metadata
     */
    public GridFSFile getReceiptFile(String receiptId, Long userId) {
        log.debug("Fetching file metadata for receipt ID: {}", receiptId);
        
        ReceiptDocument receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt", "id", receiptId));
        
        // Verify ownership
        if (!receipt.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Receipt does not belong to the user");
        }
        
        GridFSFile gridFsFile = gridFsTemplate.findOne(
                new Query(Criteria.where("_id").is(new ObjectId(receipt.getGridFsFileId())))
        );
        
        if (gridFsFile == null) {
            throw new ResourceNotFoundException("File", "gridFsId", receipt.getGridFsFileId());
        }
        
        return gridFsFile;
    }

    /**
     * Convert entity to DTO
     */
    private ReceiptDto toDto(ReceiptDocument receipt) {
        return ReceiptDto.builder()
                .id(receipt.getId())
                .userId(receipt.getUserId())
                .expenseId(receipt.getExpenseId())
                .fileName(receipt.getFileName())
                .mimeType(receipt.getMimeType())
                .fileSize(receipt.getFileSize())
                .notes(receipt.getNotes())
                .uploadedAt(receipt.getUploadedAt())
                .updatedAt(receipt.getUpdatedAt())
                .build();
    }
}

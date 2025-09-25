package com.avivse.retailfileservice.service;

import com.avivse.retailfileservice.dto.CreateRetailFileRequest;
import com.avivse.retailfileservice.dto.UpdateRetailFileRequest;
import com.avivse.retailfileservice.entity.RetailFile;
import com.avivse.retailfileservice.enums.FileProcessingStatus;
import com.avivse.retailfileservice.exception.RetailFileNotFoundException;
import com.avivse.retailfileservice.repository.RetailFileRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class RetailFileService {

    private final RetailFileRepository retailFileRepository;
    private final Counter filesCreatedCounter;
    private final Counter duplicateFilesCounter;

    @Autowired
    public RetailFileService(RetailFileRepository retailFileRepository, MeterRegistry meterRegistry) {
        this.retailFileRepository = retailFileRepository;

        this.filesCreatedCounter = Counter.builder("retail_files_created_total")
                .description("Total number of retail files created")
                .register(meterRegistry);

        this.duplicateFilesCounter = Counter.builder("duplicate_files_detected_total")
                .description("Total number of duplicate files detected")
                .register(meterRegistry);
    }

    /**
     * Create retail file from CreateRetailFileRequest DTO with duplicate detection
     */
    public RetailFile createRetailFile(CreateRetailFileRequest request) {
        // Generate checksum if provided
        String checksum = null;
        if (request.getChecksum() != null) {
            checksum = request.getChecksum();
        } else if (request.getFileUrl() != null) {
            checksum = generateChecksumFromUrl(request.getFileUrl());
        }

        // Check for duplicates by checksum if available
        if (checksum != null && retailFileRepository.existsByChecksum(checksum)) {
            duplicateFilesCounter.increment();
            throw new IllegalArgumentException("Duplicate file detected: file with same checksum already exists");
        }

        RetailFile retailFile = new RetailFile();
        retailFile.setFileName(request.getFileName());
        retailFile.setFileUrl(request.getFileUrl());
        retailFile.setFileSize(request.getFileSize());
        retailFile.setChecksum(checksum);

        // Set upload date to now if not provided
        if (request.getUploadDate() != null) {
            retailFile.setUploadDate(request.getUploadDate());
        } else {
            retailFile.setUploadDate(LocalDateTime.now());
        }

        // Set processing status
        if (request.getStatus() != null) {
            retailFile.setStatus(request.getStatus());
        } else {
            retailFile.setStatus(FileProcessingStatus.PENDING);
        }

        RetailFile savedFile = retailFileRepository.save(retailFile);
        filesCreatedCounter.increment();
        return savedFile;
    }

    /**
     * Update retail file from UpdateRetailFileRequest DTO
     */
    public RetailFile updateRetailFile(UUID id, UpdateRetailFileRequest request) {
        Optional<RetailFile> existingFile = retailFileRepository.findById(id);

        if (existingFile.isEmpty()) {
            throw new RetailFileNotFoundException("Retail file not found with id: " + id);
        }

        RetailFile fileToUpdate = existingFile.get();

        // Update only the fields that are provided (not null and not blank)
        if (request.getFileName() != null && !request.getFileName().trim().isEmpty()) {
            fileToUpdate.setFileName(request.getFileName());
        }
        if (request.getFileUrl() != null && !request.getFileUrl().trim().isEmpty()) {
            fileToUpdate.setFileUrl(request.getFileUrl());
        }
        if (request.getFileSize() != null) {
            fileToUpdate.setFileSize(request.getFileSize());
        }
        if (request.getUploadDate() != null) {
            fileToUpdate.setUploadDate(request.getUploadDate());
        }
        if (request.getStatus() != null) {
            fileToUpdate.setStatus(request.getStatus());
        }
        if (request.getChecksum() != null && !request.getChecksum().trim().isEmpty()) {
            fileToUpdate.setChecksum(request.getChecksum());
        }

        return retailFileRepository.save(fileToUpdate);
    }

    /**
     * Find retail file by ID
     */
    @Transactional(readOnly = true)
    public Optional<RetailFile> findById(UUID id) {
        return retailFileRepository.findById(id);
    }

    /**
     * Find all retail files with optional filters and pagination
     */
    @Transactional(readOnly = true)
    public Page<RetailFile> findAllWithFilters(FileProcessingStatus status, int page, int limit) {
        // Create pageable with sorting by upload date (newest first)
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("uploadDate").descending());

        return retailFileRepository.findWithFilters(status, pageable);
    }


    /**
     * Find files by processing status
     */
    @Transactional(readOnly = true)
    public List<RetailFile> findByProcessingStatus(FileProcessingStatus status) {
        return retailFileRepository.findByStatus(status);
    }

    /**
     * Update file processing status
     */
    public RetailFile updateFileStatus(UUID id, FileProcessingStatus status) {
        Optional<RetailFile> existingFile = retailFileRepository.findById(id);

        if (existingFile.isEmpty()) {
            throw new RetailFileNotFoundException("Retail file not found with id: " + id);
        }

        RetailFile file = existingFile.get();
        file.setStatus(status);
        return retailFileRepository.save(file);
    }


    /**
     * Check for duplicate files by checksum
     */
    @Transactional(readOnly = true)
    public boolean isDuplicateFileByChecksum(String checksum) {
        return checksum != null && retailFileRepository.existsByChecksum(checksum);
    }

    /**
     * Delete a retail file
     */
    public boolean deleteRetailFile(UUID id) {
        if (retailFileRepository.existsById(id)) {
            retailFileRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Check if a file exists
     */
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return retailFileRepository.existsById(id);
    }

    /**
     * Generate SHA-256 checksum from URL (simplified for metadata-based checksum)
     */
    private String generateChecksumFromUrl(String url) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(url.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
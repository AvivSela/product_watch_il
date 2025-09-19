package com.avivse.retailfileservice.service;

import com.avivse.retailfileservice.dto.CreateRetailFileRequest;
import com.avivse.retailfileservice.dto.UpdateRetailFileRequest;
import com.avivse.retailfileservice.entity.RetailFile;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class RetailFileService {

    private final RetailFileRepository retailFileRepository;
    private final Counter filesCreatedCounter;

    @Autowired
    public RetailFileService(RetailFileRepository retailFileRepository, MeterRegistry meterRegistry) {
        this.retailFileRepository = retailFileRepository;

        this.filesCreatedCounter = Counter.builder("retail_files_created_total")
                .description("Total number of retail files created")
                .register(meterRegistry);
    }



    /**
     * Create retail file from CreateRetailFileRequest DTO
     */
    public RetailFile createRetailFile(CreateRetailFileRequest request) {
        RetailFile retailFile = new RetailFile();
        retailFile.setChainId(request.getChainId());
        retailFile.setStoreId(request.getStoreId());
        retailFile.setFileName(request.getFileName());
        retailFile.setFileUrl(request.getFileUrl());
        retailFile.setFileSize(request.getFileSize());

        // Set upload date to now if not provided
        if (request.getUploadDate() != null) {
            retailFile.setUploadDate(request.getUploadDate());
        } else {
            retailFile.setUploadDate(LocalDateTime.now());
        }

        // Set processing status
        if (request.getIsProcessed() != null) {
            retailFile.setIsProcessed(request.getIsProcessed());
        } else {
            retailFile.setIsProcessed(false);
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

        if (existingFile.isPresent()) {
            RetailFile fileToUpdate = existingFile.get();

            // Update only the fields that are provided (not null)
            if (request.getChainId() != null) {
                fileToUpdate.setChainId(request.getChainId());
            }
            if (request.getStoreId() != null) {
                fileToUpdate.setStoreId(request.getStoreId());
            }
            if (request.getFileName() != null) {
                fileToUpdate.setFileName(request.getFileName());
            }
            if (request.getFileUrl() != null) {
                fileToUpdate.setFileUrl(request.getFileUrl());
            }
            if (request.getFileSize() != null) {
                fileToUpdate.setFileSize(request.getFileSize());
            }
            if (request.getUploadDate() != null) {
                fileToUpdate.setUploadDate(request.getUploadDate());
            }
            if (request.getIsProcessed() != null) {
                fileToUpdate.setIsProcessed(request.getIsProcessed());
            }

            return retailFileRepository.save(fileToUpdate);
        }

        return null;
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
    public Page<RetailFile> findAllWithFilters(String chainId, Integer storeId, Boolean isProcessed,
                                               int page, int limit) {
        // Create pageable with sorting by upload date (newest first)
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("uploadDate").descending());

        return retailFileRepository.findWithFilters(chainId, storeId, isProcessed, pageable);
    }

    /**
     * Find files by chain ID
     */
    @Transactional(readOnly = true)
    public List<RetailFile> findByChainId(String chainId) {
        return retailFileRepository.findByChainId(chainId);
    }

    /**
     * Find files by processing status
     */
    @Transactional(readOnly = true)
    public List<RetailFile> findByProcessingStatus(Boolean isProcessed) {
        return retailFileRepository.findByIsProcessed(isProcessed);
    }

    /**
     * Mark a file as processed
     */
    public RetailFile markAsProcessed(UUID id) {
        Optional<RetailFile> existingFile = retailFileRepository.findById(id);

        if (existingFile.isPresent()) {
            RetailFile file = existingFile.get();
            file.setIsProcessed(true);
            return retailFileRepository.save(file);
        }

        return null; // Will handle this better with exceptions later
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
}
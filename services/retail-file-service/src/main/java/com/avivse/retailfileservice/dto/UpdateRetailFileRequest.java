package com.avivse.retailfileservice.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class UpdateRetailFileRequest {

    @Size(max = 100, message = "Chain ID cannot exceed 100 characters")
    private String chainId;

    private Integer storeId;

    @Size(max = 255, message = "File name cannot exceed 255 characters")
    private String fileName;

    @Size(max = 500, message = "File URL cannot exceed 500 characters")
    private String fileUrl;

    private Long fileSize;

    private LocalDateTime uploadDate;

    private Boolean isProcessed;

    // Default constructor
    public UpdateRetailFileRequest() {
    }

    // Getters and Setters
    public String getChainId() {
        return chainId;
    }

    public void setChainId(String chainId) {
        this.chainId = chainId;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public void setStoreId(Integer storeId) {
        this.storeId = storeId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public Boolean getIsProcessed() {
        return isProcessed;
    }

    public void setIsProcessed(Boolean isProcessed) {
        this.isProcessed = isProcessed;
    }
}
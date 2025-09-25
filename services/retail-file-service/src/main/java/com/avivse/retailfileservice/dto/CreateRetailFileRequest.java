package com.avivse.retailfileservice.dto;

import com.avivse.retailfileservice.enums.FileProcessingStatus;
import com.avivse.retailfileservice.validation.ValidFileType;
import com.avivse.retailfileservice.validation.ValidUrl;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class CreateRetailFileRequest {


    @NotBlank(message = "File name is required")
    @Size(max = 255, message = "File name cannot exceed 255 characters")
    @ValidFileType
    @JsonProperty("file_name")
    private String fileName;

    @NotBlank(message = "File URL is required")
    @Size(max = 500, message = "File URL cannot exceed 500 characters")
    @ValidUrl
    @JsonProperty("file_url")
    private String fileUrl;

    @JsonProperty("file_size")
    private Long fileSize;

    @JsonProperty("upload_date")
    private LocalDateTime uploadDate; // Optional - will default to now if not provided

    private FileProcessingStatus status = FileProcessingStatus.PENDING;

    private String checksum;

    @NotNull(message = "Store number is required")
    @JsonProperty("store_number")
    private Integer storeNumber;

    @NotNull(message = "Chain ID is required")
    @Size(max = 20, message = "Chain ID cannot exceed 20 characters")
    @JsonProperty("chain_id")
    private String chainId;

    // Default constructor
    public CreateRetailFileRequest() {
    }

    // Getters and Setters

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

    public FileProcessingStatus getStatus() {
        return status;
    }

    public void setStatus(FileProcessingStatus status) {
        this.status = status;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public Integer getStoreNumber() {
        return storeNumber;
    }

    public void setStoreNumber(Integer storeNumber) {
        this.storeNumber = storeNumber;
    }

    public String getChainId() {
        return chainId;
    }

    public void setChainId(String chainId) {
        this.chainId = chainId;
    }
}
package com.avivse.retailfileservice.dto;

import com.avivse.retailfileservice.enums.FileProcessingStatus;
import com.avivse.retailfileservice.validation.ValidFileType;
import com.avivse.retailfileservice.validation.ValidObjectKey;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class UpdateRetailFileRequest {


    @Size(max = 255, message = "File name cannot exceed 255 characters")
    @ValidFileType
    private String fileName;

    @Size(max = 500, message = "Object key cannot exceed 500 characters")
    @ValidObjectKey
    private String fileUrl;

    private Long fileSize;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[XXX]")
    private LocalDateTime uploadDate;

    private FileProcessingStatus status;

    private String checksum;

    // Default constructor
    public UpdateRetailFileRequest() {
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
}
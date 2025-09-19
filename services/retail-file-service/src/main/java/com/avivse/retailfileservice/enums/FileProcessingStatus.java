package com.avivse.retailfileservice.enums;

public enum FileProcessingStatus {
    PENDING("File upload pending processing"),
    PROCESSING("File is currently being processed"),
    COMPLETED("File processing completed successfully"),
    FAILED("File processing failed"),
    ARCHIVED("File has been archived");

    private final String description;

    FileProcessingStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
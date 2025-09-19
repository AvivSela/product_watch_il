package com.avivse.retailfileservice.enums;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum SupportedFileType {
    PDF("pdf", "application/pdf"),
    CSV("csv", "text/csv"),
    XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    XLS("xls", "application/vnd.ms-excel"),
    JSON("json", "application/json"),
    XML("xml", "application/xml"),
    TXT("txt", "text/plain");

    private final String extension;
    private final String mimeType;

    SupportedFileType(String extension, String mimeType) {
        this.extension = extension;
        this.mimeType = mimeType;
    }

    public String getExtension() {
        return extension;
    }

    public String getMimeType() {
        return mimeType;
    }

    public static boolean isSupported(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return false;
        }

        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        return Arrays.stream(values())
                .anyMatch(type -> type.extension.equals(extension));
    }

    public static Set<String> getAllExtensions() {
        return Arrays.stream(values())
                .map(SupportedFileType::getExtension)
                .collect(Collectors.toSet());
    }
}
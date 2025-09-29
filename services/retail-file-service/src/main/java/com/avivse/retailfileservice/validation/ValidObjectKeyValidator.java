package com.avivse.retailfileservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class ValidObjectKeyValidator implements ConstraintValidator<ValidObjectKey, String> {

    private static final int MAX_OBJECT_KEY_LENGTH = 1024;

    // Pattern for valid S3 object key characters (excluding some problematic characters)
    private static final Pattern VALID_OBJECT_KEY_PATTERN = Pattern.compile("^[a-zA-Z0-9._/\\-]+$");

    @Override
    public boolean isValid(String objectKey, ConstraintValidatorContext context) {
        if (objectKey == null || objectKey.trim().isEmpty()) {
            return true; // Let @NotBlank handle null/empty validation
        }

        String trimmedKey = objectKey.trim();

        // Check length
        if (trimmedKey.length() > MAX_OBJECT_KEY_LENGTH) {
            addViolation(context, "Object key cannot exceed " + MAX_OBJECT_KEY_LENGTH + " characters");
            return false;
        }

        // Check for valid characters
        if (!VALID_OBJECT_KEY_PATTERN.matcher(trimmedKey).matches()) {
            addViolation(context, "Object key contains invalid characters. Only letters, numbers, dots, slashes, underscores, and hyphens are allowed");
            return false;
        }

        // Check that it doesn't start or end with slash
        if (trimmedKey.startsWith("/") || trimmedKey.endsWith("/")) {
            addViolation(context, "Object key cannot start or end with a slash");
            return false;
        }

        // Check for consecutive slashes
        if (trimmedKey.contains("//")) {
            addViolation(context, "Object key cannot contain consecutive slashes");
            return false;
        }

        return true;
    }

    private void addViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}
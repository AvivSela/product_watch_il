package com.avivse.retailfileservice.validation;

import com.avivse.retailfileservice.enums.SupportedFileType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidFileTypeValidator implements ConstraintValidator<ValidFileType, String> {

    @Override
    public boolean isValid(String fileName, ConstraintValidatorContext context) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return true; // Let @NotBlank handle null/empty validation
        }

        return SupportedFileType.isSupported(fileName);
    }
}
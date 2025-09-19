package com.avivse.retailfileservice.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidFileTypeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFileType {
    String message() default "Unsupported file type. Supported types: pdf, csv, xlsx, xls, json, xml, txt";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
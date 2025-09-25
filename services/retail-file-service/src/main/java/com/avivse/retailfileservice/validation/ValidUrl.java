package com.avivse.retailfileservice.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidUrlValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUrl {
    String message() default "Invalid URL format or insecure protocol";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
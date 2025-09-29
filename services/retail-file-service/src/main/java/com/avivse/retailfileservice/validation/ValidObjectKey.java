package com.avivse.retailfileservice.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidObjectKeyValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidObjectKey {
    String message() default "Invalid S3 object key format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
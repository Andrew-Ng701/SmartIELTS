package com.andrew.smartielts.common.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DeletedRecordPageQueryValidator.class)
public @interface ValidDeletedRecordPageQuery {

    String message() default "Invalid deleted record page query";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
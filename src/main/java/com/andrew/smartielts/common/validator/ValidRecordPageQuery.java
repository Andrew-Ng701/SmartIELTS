package com.andrew.smartielts.common.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RecordPageQueryValidator.class)
public @interface ValidRecordPageQuery {

    String message() default "Invalid record page query";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
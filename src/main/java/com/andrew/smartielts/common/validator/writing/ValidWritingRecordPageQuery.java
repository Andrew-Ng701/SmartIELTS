package com.andrew.smartielts.common.validator.writing;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = WritingRecordPageQueryValidator.class)
public @interface ValidWritingRecordPageQuery {

    String message() default "Invalid writing record page query";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
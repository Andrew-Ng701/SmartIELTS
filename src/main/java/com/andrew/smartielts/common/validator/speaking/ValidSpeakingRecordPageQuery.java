package com.andrew.smartielts.common.validator.speaking;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SpeakingRecordPageQueryValidator.class)
public @interface ValidSpeakingRecordPageQuery {

    String message() default "Invalid speaking record page query";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
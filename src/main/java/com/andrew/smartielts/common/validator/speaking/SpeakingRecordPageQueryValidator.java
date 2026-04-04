package com.andrew.smartielts.common.validator.speaking;

import com.andrew.smartielts.speaking.domain.query.admin.AdminSpeakingRecordPageQuery;
import com.andrew.smartielts.speaking.domain.query.user.UserSpeakingRecordPageQuery;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;

public class SpeakingRecordPageQueryValidator implements ConstraintValidator<ValidSpeakingRecordPageQuery, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        if (value instanceof AdminSpeakingRecordPageQuery query) {
            return validate(
                    query.getPageNum(),
                    query.getPageSize(),
                    query.getUserId(),
                    query.getMinOverallScore(),
                    query.getMaxOverallScore(),
                    query.getStartTime(),
                    query.getEndTime(),
                    context
            );
        }

        if (value instanceof UserSpeakingRecordPageQuery query) {
            return validate(
                    query.getPageNum(),
                    query.getPageSize(),
                    null,
                    query.getMinOverallScore(),
                    query.getMaxOverallScore(),
                    query.getStartTime(),
                    query.getEndTime(),
                    context
            );
        }

        return true;
    }

    private boolean validate(Integer pageNum,
                             Integer pageSize,
                             Long userId,
                             Integer minOverallScore,
                             Integer maxOverallScore,
                             LocalDateTime startTime,
                             LocalDateTime endTime,
                             ConstraintValidatorContext context) {

        context.disableDefaultConstraintViolation();

        if (pageNum == null || pageNum < 1) {
            context.buildConstraintViolationWithTemplate("pageNum must be greater than or equal to 1")
                    .addConstraintViolation();
            return false;
        }

        if (pageSize == null || pageSize < 1) {
            context.buildConstraintViolationWithTemplate("pageSize must be greater than or equal to 1")
                    .addConstraintViolation();
            return false;
        }

        if (userId != null && userId < 1) {
            context.buildConstraintViolationWithTemplate("userId must be greater than or equal to 1")
                    .addConstraintViolation();
            return false;
        }

        if (minOverallScore != null && minOverallScore < 0) {
            context.buildConstraintViolationWithTemplate("minOverallScore must be greater than or equal to 0")
                    .addConstraintViolation();
            return false;
        }

        if (maxOverallScore != null && maxOverallScore < 0) {
            context.buildConstraintViolationWithTemplate("maxOverallScore must be greater than or equal to 0")
                    .addConstraintViolation();
            return false;
        }

        if (minOverallScore != null && maxOverallScore != null && minOverallScore > maxOverallScore) {
            context.buildConstraintViolationWithTemplate("minOverallScore cannot be greater than maxOverallScore")
                    .addConstraintViolation();
            return false;
        }

        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            context.buildConstraintViolationWithTemplate("startTime cannot be later than endTime")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
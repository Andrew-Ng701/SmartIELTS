package com.andrew.smartielts.common.validator.writing;

import com.andrew.smartielts.writing.domain.query.admin.AdminWritingRecordPageQuery;
import com.andrew.smartielts.writing.domain.query.user.UserWritingRecordPageQuery;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WritingRecordPageQueryValidator implements ConstraintValidator<ValidWritingRecordPageQuery, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        if (value instanceof AdminWritingRecordPageQuery query) {
            return validate(
                    query.getPageNum(),
                    query.getPageSize(),
                    query.getUserId(),
                    query.getQuestionId(),
                    query.getTargetScore(),
                    query.getStartTime(),
                    query.getEndTime(),
                    context
            );
        }

        if (value instanceof UserWritingRecordPageQuery query) {
            return validate(
                    query.getPageNum(),
                    query.getPageSize(),
                    null,
                    query.getQuestionId(),
                    query.getTargetScore(),
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
                             Long questionId,
                             BigDecimal targetScore,
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

        if (questionId != null && questionId < 1) {
            context.buildConstraintViolationWithTemplate("questionId must be greater than or equal to 1")
                    .addConstraintViolation();
            return false;
        }

        if (targetScore != null && targetScore.compareTo(BigDecimal.ZERO) < 0) {
            context.buildConstraintViolationWithTemplate("targetScore must be greater than or equal to 0")
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
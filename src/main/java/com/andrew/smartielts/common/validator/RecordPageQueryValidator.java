package com.andrew.smartielts.common.validator;

import com.andrew.smartielts.common.query.RecordPageQuery;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RecordPageQueryValidator implements ConstraintValidator<ValidRecordPageQuery, RecordPageQuery> {

    @Override
    public boolean isValid(RecordPageQuery query, ConstraintValidatorContext context) {
        if (query == null) {
            return true;
        }

        context.disableDefaultConstraintViolation();

        if (query.getPageNum() == null || query.getPageNum() < 1) {
            context.buildConstraintViolationWithTemplate("pageNum must be greater than or equal to 1")
                    .addConstraintViolation();
            return false;
        }

        if (query.getPageSize() == null || query.getPageSize() < 1) {
            context.buildConstraintViolationWithTemplate("pageSize must be greater than or equal to 1")
                    .addConstraintViolation();
            return false;
        }

        if (query.getUserId() != null && query.getUserId() < 1) {
            context.buildConstraintViolationWithTemplate("userId must be greater than or equal to 1")
                    .addConstraintViolation();
            return false;
        }

        if (query.getTestId() != null && query.getTestId() < 1) {
            context.buildConstraintViolationWithTemplate("testId must be greater than or equal to 1")
                    .addConstraintViolation();
            return false;
        }

        if (query.getMinScore() != null && query.getMinScore() < 0) {
            context.buildConstraintViolationWithTemplate("minScore must be greater than or equal to 0")
                    .addConstraintViolation();
            return false;
        }

        if (query.getMaxScore() != null && query.getMaxScore() < 0) {
            context.buildConstraintViolationWithTemplate("maxScore must be greater than or equal to 0")
                    .addConstraintViolation();
            return false;
        }

        if (query.getMinScore() != null && query.getMaxScore() != null
                && query.getMinScore() > query.getMaxScore()) {
            context.buildConstraintViolationWithTemplate("minScore cannot be greater than maxScore")
                    .addConstraintViolation();
            return false;
        }

        if (query.getStartTime() != null && query.getEndTime() != null
                && query.getStartTime().isAfter(query.getEndTime())) {
            context.buildConstraintViolationWithTemplate("startTime cannot be later than endTime")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
package com.andrew.smartielts.common.validator;

import com.andrew.smartielts.common.query.DeletedRecordPageQuery;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DeletedRecordPageQueryValidator implements ConstraintValidator<ValidDeletedRecordPageQuery, DeletedRecordPageQuery> {

    @Override
    public boolean isValid(DeletedRecordPageQuery query, ConstraintValidatorContext context) {
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

        return true;
    }
}
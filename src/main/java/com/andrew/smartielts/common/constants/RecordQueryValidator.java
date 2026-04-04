package com.andrew.smartielts.common.constants;

import com.andrew.smartielts.common.constants.ApiMessageConstants;

import java.time.LocalDateTime;

public final class RecordQueryValidator {

    private RecordQueryValidator() {}

    public static void validate(
            Integer pageNum,
            Integer pageSize,
            Long userId,
            Long testId,
            Integer minScore,
            Integer maxScore,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        if (pageNum != null && pageNum < 1) {
            throw new RuntimeException(ApiMessageConstants.PAGE_NUM_INVALID);
        }
        if (pageSize != null && pageSize < 1) {
            throw new RuntimeException(ApiMessageConstants.PAGE_SIZE_INVALID);
        }
        if (userId != null && userId < 1) {
            throw new RuntimeException(ApiMessageConstants.USER_ID_INVALID);
        }
        if (testId != null && testId < 1) {
            throw new RuntimeException(ApiMessageConstants.TEST_ID_INVALID);
        }
        if (minScore != null && maxScore != null && minScore > maxScore) {
            throw new RuntimeException(ApiMessageConstants.MIN_SCORE_GT_MAX_SCORE);
        }
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new RuntimeException(ApiMessageConstants.START_TIME_GT_END_TIME);
        }
    }
}
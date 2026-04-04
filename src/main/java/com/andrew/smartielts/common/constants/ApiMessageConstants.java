package com.andrew.smartielts.common.constants;

public final class ApiMessageConstants {

    private ApiMessageConstants() {}

    public static final String INVALID_REQUEST_BODY_FORMAT = "Invalid request body format";

    public static final String PAGE_NUM_INVALID = "pageNum must be greater than or equal to 1";
    public static final String PAGE_SIZE_INVALID = "pageSize must be greater than or equal to 1";
    public static final String USER_ID_INVALID = "userId must be greater than 0";
    public static final String TEST_ID_INVALID = "testId must be greater than 0";
    public static final String QUESTION_ID_INVALID = "questionId must be greater than 0";
    public static final String SESSION_ID_INVALID = "sessionId must not be blank";

    public static final String MIN_SCORE_GT_MAX_SCORE = "minScore cannot be greater than maxScore";
    public static final String START_TIME_GT_END_TIME = "startTime cannot be later than endTime";
}
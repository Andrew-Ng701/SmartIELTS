package com.andrew.smartielts.common.constants;

public final class StorageBizConstants {

    private StorageBizConstants() {
    }

    public static final String TARGET_TYPE_LISTENING_PART_GROUP = "LISTENING_PART_GROUP";
    public static final String TARGET_TYPE_READING_PART_GROUP = "READING_PART_GROUP";
    public static final String TARGET_TYPE_WRITING_QUESTION = "WRITING_QUESTION";

    /**
     * 必須與 application.yml 的 aliyun.oss.buckets.* key 完全一致
     */
    public static final String BUCKET_KEY_WRITING_QUESTION = "writing-question";
    public static final String BUCKET_KEY_WRITING_RECORD = "writing-record";
    public static final String BUCKET_KEY_LISTENING_RECORDING = "listening-recording";
    public static final String BUCKET_KEY_SPEAKING_AUDIO = "speaking-audio";
    public static final String BUCKET_KEY_QUESTION_GROUP_IMAGE = "question-group-image";

    public static final String BIZ_PATH_LISTENING_AUDIO = "listening-audio";
    public static final String BIZ_PATH_QUESTION_GROUP_IMAGE = "question-group-image";
    public static final String BIZ_PATH_WRITING_QUESTION_IMAGE = "writing-question-image";
    public static final String BIZ_PATH_WRITING_RECORD = "writing-record";
}
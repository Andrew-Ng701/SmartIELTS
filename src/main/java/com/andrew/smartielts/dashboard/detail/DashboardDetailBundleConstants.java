package com.andrew.smartielts.dashboard.detail;

import java.util.List;

public final class DashboardDetailBundleConstants {

    private DashboardDetailBundleConstants() {
    }

    public static final String MODULE_READING = "reading";
    public static final String MODULE_LISTENING = "listening";
    public static final String MODULE_WRITING = "writing";
    public static final String MODULE_SPEAKING = "speaking";

    public static final String OBJECT_TYPE_TEST = "test";
    public static final String OBJECT_TYPE_PASSAGE = "passage";
    public static final String OBJECT_TYPE_QUESTION = "question";
    public static final String OBJECT_TYPE_RECORD = "record";
    public static final String OBJECT_TYPE_SESSION = "session";

    public static final String PARAM_TARGET_USER_ID = "target_user_id";
    public static final String PARAM_MODULE = "module";
    public static final String PARAM_TEST_ID = "test_id";
    public static final String PARAM_PASSAGE_ID = "passage_id";
    public static final String PARAM_QUESTION_ID = "question_id";
    public static final String PARAM_RECORD_ID = "record_id";
    public static final String PARAM_QUESTION_NUMBER = "question_number";
    public static final String PARAM_SESSION_ID = "session_id";
    public static final String PARAM_LIMIT = "limit";

    public static final String KEY_MODULE = "module";
    public static final String KEY_ASK_SCENE = "ask_scene";
    public static final String KEY_OBJECT_REF = "object_ref";
    public static final String KEY_QUESTION_CONTEXT = "question_context";
    public static final String KEY_EXT = "ext";

    public static final String KEY_OBJECT_TYPE = "object_type";
    public static final String KEY_TEST_ID = "test_id";
    public static final String KEY_PASSAGE_ID = "passage_id";
    public static final String KEY_QUESTION_ID = "question_id";
    public static final String KEY_RECORD_ID = "record_id";
    public static final String KEY_QUESTION_NUMBER = "question_number";
    public static final String KEY_SESSION_ID = "session_id";

    public static final String KEY_TEST_TITLE = "test_title";
    public static final String KEY_PASSAGE_TITLE = "passage_title";
    public static final String KEY_ARTICLE_TITLE = "article_title";
    public static final String KEY_ARTICLE_CONTENT = "article_content";
    public static final String KEY_QUESTION_TEXT = "question_text";
    public static final String KEY_QUESTION_TYPE = "question_type";
    public static final String KEY_ANSWER_MODE = "answer_mode";
    public static final String KEY_OPTIONS_JSON = "options_json";
    public static final String KEY_ACCEPTED_ANSWERS_JSON = "accepted_answers_json";
    public static final String KEY_CORRECT_ANSWER = "correct_answer";
    public static final String KEY_EXPLANATION = "explanation";
    public static final String KEY_CUE_CARD = "cue_card";
    public static final String KEY_IMAGE_URL = "image_url";
    public static final String KEY_TASK_TYPE = "task_type";
    public static final String KEY_USER_ANSWER = "user_answer";
    public static final String KEY_USER_ESSAY = "user_essay";
    public static final String KEY_USER_TRANSCRIPT = "user_transcript";
    public static final String KEY_TRANSCRIPT_TEXT = "transcript_text";
    public static final String KEY_AUDIO_URL = "audio_url";
    public static final String KEY_AUDIO_OBJECT_KEY = "audio_object_key";
    public static final String KEY_USER_FEEDBACK = "user_feedback";
    public static final String KEY_AI_FEEDBACK = "ai_feedback";
    public static final String KEY_SCORE = "score";
    public static final String KEY_TOTAL_SCORE = "total_score";
    public static final String KEY_AI_SCORE = "ai_score";
    public static final String KEY_CORRECT = "correct";
    public static final String KEY_STATUS = "status";
    public static final String KEY_CREATED_TIME = "created_time";

    public static final List<String> DETAIL_BUNDLE_EXPECTED_COLUMNS = List.of(
            KEY_MODULE,
            KEY_RECORD_ID,
            KEY_TEST_ID,
            KEY_TEST_TITLE,
            KEY_PASSAGE_ID,
            KEY_PASSAGE_TITLE,
            KEY_ARTICLE_TITLE,
            KEY_ARTICLE_CONTENT,
            KEY_QUESTION_ID,
            KEY_QUESTION_NUMBER,
            KEY_QUESTION_TEXT,
            KEY_QUESTION_TYPE,
            KEY_ANSWER_MODE,
            KEY_OPTIONS_JSON,
            KEY_ACCEPTED_ANSWERS_JSON,
            KEY_CORRECT_ANSWER,
            KEY_EXPLANATION,
            KEY_CUE_CARD,
            KEY_IMAGE_URL,
            KEY_TASK_TYPE,
            KEY_USER_ANSWER,
            KEY_USER_ESSAY,
            KEY_USER_TRANSCRIPT,
            KEY_TRANSCRIPT_TEXT,
            KEY_AUDIO_URL,
            KEY_AUDIO_OBJECT_KEY,
            KEY_USER_FEEDBACK,
            KEY_AI_FEEDBACK,
            KEY_SCORE,
            KEY_TOTAL_SCORE,
            KEY_AI_SCORE,
            KEY_CORRECT,
            KEY_STATUS,
            KEY_CREATED_TIME,
            KEY_SESSION_ID
    );
}
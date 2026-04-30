package com.andrew.smartielts.dashboard.detail;

public final class DashboardDetailBundleSqlTemplates {

    private DashboardDetailBundleSqlTemplates() {
    }

    public static final String READING_DETAIL_BUNDLE_SQL = """
            SELECT 'reading' AS module,
                   rr.id AS record_id,
                   rt.id AS test_id,
                   rt.title AS test_title,
                   rp.id AS passage_id,
                   rp.title AS passage_title,
                   COALESCE(rp.title, rt.title) AS article_title,
                   rp.content AS article_content,
                   rq.id AS question_id,
                   rq.question_number AS question_number,
                   rq.question_text AS question_text,
                   rq.question_type AS question_type,
                   rq.answer_mode AS answer_mode,
                   rq.options_json AS options_json,
                   rq.accepted_answers_json AS accepted_answers_json,
                   rq.correct_answer AS correct_answer,
                   NULL AS explanation,
                   NULL AS cue_card,
                   NULL AS image_url,
                   NULL AS task_type,
                   rar.user_answer AS user_answer,
                   NULL AS user_essay,
                   NULL AS user_transcript,
                   NULL AS transcript_text,
                   NULL AS audio_url,
                   NULL AS audio_object_key,
                   NULL AS user_feedback,
                   NULL AS ai_feedback,
                   rar.score AS score,
                   rr.total_score AS total_score,
                   NULL AS ai_score,
                   rar.is_correct AS correct,
                   rr.record_status AS status,
                   rr.created_time AS created_time,
                   rr.session_id AS session_id
            FROM reading_record rr
            JOIN reading_test rt
              ON rt.id = rr.test_id
             AND rt.is_deleted = 0
            LEFT JOIN reading_answer_record rar
              ON rar.record_id = rr.id
            LEFT JOIN reading_question rq
              ON rq.id = rar.question_id
             AND rq.is_deleted = 0
            LEFT JOIN reading_passage rp
              ON rp.id = rq.passage_id
             AND rp.is_deleted = 0
            WHERE rr.user_id = :targetUserId
              AND rr.is_deleted = 0
              AND (:recordId IS NULL OR rr.id = :recordId)
              AND (:testId IS NULL OR rt.id = :testId)
              AND (:passageId IS NULL OR rp.id = :passageId)
              AND (:questionId IS NULL OR rq.id = :questionId)
              AND (:questionNumber IS NULL OR rq.question_number = :questionNumber)
            ORDER BY rr.created_time DESC, rq.question_number ASC
            LIMIT :limit
            """;

    public static final String LISTENING_DETAIL_BUNDLE_SQL = """
            SELECT 'listening' AS module,
                   lr.id AS record_id,
                   lt.id AS test_id,
                   lt.title AS test_title,
                   NULL AS passage_id,
                   NULL AS passage_title,
                   lt.title AS article_title,
                   COALESCE(lm.transcript_text, lt.transcript_text) AS article_content,
                   lq.id AS question_id,
                   lq.question_number AS question_number,
                   lq.question_text AS question_text,
                   lq.question_type AS question_type,
                   lq.answer_mode AS answer_mode,
                   lq.options_json AS options_json,
                   lq.accepted_answers_json AS accepted_answers_json,
                   lq.correct_answer AS correct_answer,
                   NULL AS explanation,
                   NULL AS cue_card,
                   NULL AS image_url,
                   NULL AS task_type,
                   lar.user_answer AS user_answer,
                   NULL AS user_essay,
                   NULL AS user_transcript,
                   COALESCE(lm.transcript_text, lt.transcript_text) AS transcript_text,
                   lm.audio_url AS audio_url,
                   lm.audio_object_key AS audio_object_key,
                   NULL AS user_feedback,
                   NULL AS ai_feedback,
                   lar.score AS score,
                   lr.total_score AS total_score,
                   NULL AS ai_score,
                   lar.is_correct AS correct,
                   lr.record_status AS status,
                   lr.created_time AS created_time,
                   lr.session_id AS session_id
            FROM listening_record lr
            JOIN listening_test lt
              ON lt.id = lr.test_id
             AND lt.is_deleted = 0
            LEFT JOIN listening_answer_record lar
              ON lar.record_id = lr.id
            LEFT JOIN listening_question lq
              ON lq.id = lar.question_id
             AND lq.is_deleted = 0
            LEFT JOIN listening_material lm
              ON lm.id = lq.material_id
             AND lm.is_deleted = 0
            WHERE lr.user_id = :targetUserId
              AND lr.is_deleted = 0
              AND (:recordId IS NULL OR lr.id = :recordId)
              AND (:testId IS NULL OR lt.id = :testId)
              AND (:questionId IS NULL OR lq.id = :questionId)
              AND (:questionNumber IS NULL OR lq.question_number = :questionNumber)
            ORDER BY lr.created_time DESC, lq.question_number ASC
            LIMIT :limit
            """;

    public static final String WRITING_DETAIL_BUNDLE_SQL = """
            SELECT 'writing' AS module,
                   wr.id AS record_id,
                   NULL AS test_id,
                   NULL AS test_title,
                   NULL AS passage_id,
                   NULL AS passage_title,
                   wq.title AS article_title,
                   wq.description AS article_content,
                   wq.id AS question_id,
                   NULL AS question_number,
                   wq.title AS question_text,
                   'writing_task' AS question_type,
                   'essay' AS answer_mode,
                   NULL AS options_json,
                   NULL AS accepted_answers_json,
                   NULL AS correct_answer,
                   NULL AS explanation,
                   NULL AS cue_card,
                   wq.image_url AS image_url,
                   wq.task_type AS task_type,
                   NULL AS user_answer,
                   COALESCE(wr.text_content, wr.extracted_text) AS user_essay,
                   NULL AS user_transcript,
                   NULL AS transcript_text,
                   NULL AS audio_url,
                   NULL AS audio_object_key,
                   NULL AS user_feedback,
                   wr.ai_feedback AS ai_feedback,
                   NULL AS score,
                   NULL AS total_score,
                   wr.ai_score AS ai_score,
                   NULL AS correct,
                   wr.ai_status AS status,
                   wr.created_time AS created_time,
                   NULL AS session_id
            FROM writing_record wr
            JOIN writing_question wq
              ON wq.id = wr.question_id
             AND wq.is_deleted = 0
            WHERE wr.user_id = :targetUserId
              AND wr.is_deleted = 0
              AND (:recordId IS NULL OR wr.id = :recordId)
              AND (:questionId IS NULL OR wq.id = :questionId)
            ORDER BY wr.created_time DESC
            LIMIT :limit
            """;

    public static final String SPEAKING_DETAIL_BUNDLE_SQL = """
            SELECT 'speaking' AS module,
                   sr.id AS record_id,
                   NULL AS test_id,
                   NULL AS test_title,
                   NULL AS passage_id,
                   NULL AS passage_title,
                   sq.topic_key AS article_title,
                   sq.cue_card AS article_content,
                   sq.id AS question_id,
                   NULL AS question_number,
                   sq.question_text AS question_text,
                   sq.sub_type AS question_type,
                   'speech' AS answer_mode,
                   NULL AS options_json,
                   NULL AS accepted_answers_json,
                   NULL AS correct_answer,
                   NULL AS explanation,
                   sq.cue_card AS cue_card,
                   NULL AS image_url,
                   sq.part AS task_type,
                   NULL AS user_answer,
                   NULL AS user_essay,
                   sr.transcript AS user_transcript,
                   sr.transcript AS transcript_text,
                   sr.audio_url AS audio_url,
                   NULL AS audio_object_key,
                   sr.feedback AS user_feedback,
                   NULL AS ai_feedback,
                   sr.overall_score AS score,
                   sr.overall_score AS total_score,
                   sr.overall_score AS ai_score,
                   NULL AS correct,
                   sr.answer_status AS status,
                   sr.created_time AS created_time,
                   sr.session_id AS session_id
            FROM speaking_record sr
            JOIN speaking_question sq
              ON sq.id = sr.question_id
             AND sq.is_deleted = 0
            WHERE sr.user_id = :targetUserId
              AND sr.is_deleted = 0
              AND (:recordId IS NULL OR sr.id = :recordId)
              AND (:questionId IS NULL OR sq.id = :questionId)
              AND (:sessionId IS NULL OR sr.session_id = :sessionId)
            ORDER BY sr.created_time DESC
            LIMIT :limit
            """;
}
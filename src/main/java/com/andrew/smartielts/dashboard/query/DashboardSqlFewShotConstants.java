package com.andrew.smartielts.dashboard.query;

public final class DashboardSqlFewShotConstants {

    private DashboardSqlFewShotConstants() {
    }

    public static final String FEW_SHOT_LISTENING_RECORD_DETAIL = """
            Example 1
            userQuery:
            show my listening record detail for record 1201

            goodJson:
            {
              "success": true,
              "sql": "SELECT 'listening' AS module, lr.id AS record_id, lr.test_id AS test_id, lt.title AS test_title, lar.question_id AS question_id, lq.question_number AS question_number, lq.question_text AS question_text, lq.question_type AS question_type, lq.answer_mode AS answer_mode, lq.options_json AS options_json, lq.accepted_answers_json AS accepted_answers_json, lq.correct_answer AS correct_answer, lar.user_answer AS user_answer, lar.is_correct AS is_correct, lar.score AS score, lr.total_score AS total_score, lr.record_status AS record_status, lr.created_time AS created_time, lr.submitted_time AS submitted_time, lr.session_id AS session_id FROM listening_record lr LEFT JOIN listening_test lt ON lt.id = lr.test_id LEFT JOIN listening_answer_record lar ON lar.record_id = lr.id LEFT JOIN listening_question lq ON lq.id = lar.question_id WHERE lr.id = :recordId AND lr.user_id = :targetUserId AND lr.is_deleted = 0 ORDER BY lq.question_number ASC, lar.id ASC",
              "params": {
                "recordId": 1201,
                "targetUserId": 2001
              },
              "expectedColumns": [
                "module",
                "record_id",
                "test_id",
                "test_title",
                "question_id",
                "question_number",
                "question_text",
                "question_type",
                "answer_mode",
                "options_json",
                "accepted_answers_json",
                "correct_answer",
                "user_answer",
                "is_correct",
                "score",
                "total_score",
                "record_status",
                "created_time",
                "submitted_time",
                "session_id"
              ],
              "queryPurpose": "listening_record_detail",
              "reasoningSummary": "Listening record detail should use listening_record as the driving table and return stable snake_case aliases.",
              "confidence": 0.98,
              "suggestions": [
                "You can also ask for the latest listening record.",
                "You can ask to filter one question within the record."
              ]
            }
            """;

    public static final String FEW_SHOT_READING_QUESTION_DETAIL = """
            Example 2
            userQuery:
            show reading question 15 with passage content

            goodJson:
            {
              "success": true,
              "sql": "SELECT 'reading' AS module, rq.id AS question_id, rq.passage_id AS passage_id, rp.title AS passage_title, rp.passage_no AS passage_no, rq.question_number AS question_number, rq.question_text AS question_text, rq.question_type AS question_type, rq.answer_mode AS answer_mode, rq.options_json AS options_json, rq.accepted_answers_json AS accepted_answers_json, rq.correct_answer AS correct_answer, rp.content AS article_content, rq.display_order AS display_order FROM reading_question rq LEFT JOIN reading_passage rp ON rp.id = rq.passage_id WHERE rq.id = :questionId AND rq.is_deleted = 0 AND rp.is_deleted = 0 LIMIT 1",
              "params": {
                "questionId": 15
              },
              "expectedColumns": [
                "module",
                "question_id",
                "passage_id",
                "passage_title",
                "passage_no",
                "question_number",
                "question_text",
                "question_type",
                "answer_mode",
                "options_json",
                "accepted_answers_json",
                "correct_answer",
                "article_content",
                "display_order"
              ],
              "queryPurpose": "reading_question_detail",
              "reasoningSummary": "Reading question detail should be driven by reading_question and join reading_passage only for needed passage content.",
              "confidence": 0.97,
              "suggestions": [
                "You can also ask for all questions in the same passage."
              ]
            }
            """;

    public static final String FEW_SHOT_WRITING_RECORD_DETAIL = """
            Example 3
            userQuery:
            show my writing record 880 and attachments

            goodJson:
            {
              "success": true,
              "sql": "SELECT 'writing' AS module, wr.id AS record_id, wr.question_id AS question_id, wq.task_type AS task_type, wq.title AS test_title, wq.description AS question_text, wq.image_url AS image_url, wq.image_object_key AS image_object_key, wr.input_type AS input_type, wr.text_content AS user_essay, wr.extracted_text AS extracted_text, wr.target_score AS target_score, wr.ai_score AS ai_score, wr.ai_feedback AS ai_feedback, wr.ai_status AS ai_status, wr.ai_provider AS ai_provider, wr.ai_model AS ai_model, wr.created_time AS created_time, wra.id AS attachment_id, wra.file_type AS file_type, wra.file_url AS file_url, wra.file_key AS file_key, wra.sort_order AS sort_order, wra.ocr_text AS ocr_text FROM writing_record wr LEFT JOIN writing_question wq ON wq.id = wr.question_id LEFT JOIN writing_record_attachment wra ON wra.record_id = wr.id WHERE wr.id = :recordId AND wr.user_id = :targetUserId AND wr.is_deleted = 0 ORDER BY wra.sort_order ASC, wra.id ASC",
              "params": {
                "recordId": 880,
                "targetUserId": 2001
              },
              "expectedColumns": [
                "module",
                "record_id",
                "question_id",
                "task_type",
                "test_title",
                "question_text",
                "image_url",
                "image_object_key",
                "input_type",
                "user_essay",
                "extracted_text",
                "target_score",
                "ai_score",
                "ai_feedback",
                "ai_status",
                "ai_provider",
                "ai_model",
                "created_time",
                "attachment_id",
                "file_type",
                "file_url",
                "file_key",
                "sort_order",
                "ocr_text"
              ],
              "queryPurpose": "writing_record_detail",
              "reasoningSummary": "Writing record detail should include prompt fields from writing_question and attachment fields from writing_record_attachment.",
              "confidence": 0.98,
              "suggestions": [
                "You can also ask for only the latest writing score.",
                "You can ask to hide attachment rows if you only need essay content."
              ]
            }
            """;

    public static final String FEW_SHOTS_SPEAKING_RECORD_DETAIL = """
            Example 4
            userQuery:
            show my speaking record detail for session abc123

            goodJson:
            {
              "success": true,
              "sql": "SELECT 'speaking' AS module, sr.id AS record_id, sr.question_id AS question_id, sq.part AS part, sq.sub_type AS sub_type, sq.topic_key AS topic_key, sq.question_text AS question_text, sq.cue_card AS cue_card, sr.audio_url AS audio_url, sr.transcript AS user_transcript, sr.feedback AS user_feedback, sr.ai_status AS ai_status, sr.ai_provider AS ai_provider, sr.ai_model AS ai_model, sr.fluency_and_coherence AS fluency_and_coherence, sr.lexical_resource AS lexical_resource, sr.grammatical_range_and_accuracy AS grammatical_range_and_accuracy, sr.pronunciation AS pronunciation, sr.overall_score AS total_score, sr.created_time AS created_time, sr.session_id AS session_id FROM speaking_record sr LEFT JOIN speaking_question sq ON sq.id = sr.question_id WHERE sr.session_id = :sessionId AND sr.user_id = :targetUserId AND sr.is_deleted = 0 ORDER BY sr.created_time ASC, sr.id ASC",
              "params": {
                "sessionId": "abc123",
                "targetUserId": 2001
              },
              "expectedColumns": [
                "module",
                "record_id",
                "question_id",
                "part",
                "sub_type",
                "topic_key",
                "question_text",
                "cue_card",
                "audio_url",
                "user_transcript",
                "user_feedback",
                "ai_status",
                "ai_provider",
                "ai_model",
                "fluency_and_coherence",
                "lexical_resource",
                "grammatical_range_and_accuracy",
                "pronunciation",
                "total_score",
                "created_time",
                "session_id"
              ],
              "queryPurpose": "speaking_record_detail",
              "reasoningSummary": "Speaking record detail should expose speaking prompt fields and user transcript with snake_case aliases only.",
              "confidence": 0.97,
              "suggestions": [
                "You can ask for one specific speaking question inside the same session."
              ]
            }
            """;

    public static final String FEW_SHOT_UNSUPPORTED = """
            Example 5
            userQuery:
            delete my last speaking record

            goodJson:
            {
              "success": false,
              "sql": "",
              "params": {},
              "expectedColumns": [],
              "queryPurpose": "unsupported_structured_query",
              "reasoningSummary": "The request is a write operation and structured query only allows read-only select statements.",
              "confidence": 0.0,
              "suggestions": [
                "Ask for a read-only summary instead.",
                "Ask to view the latest speaking record details."
              ]
            }
            """;

    public static final String DASH_SCOPE_SQL_GENERATION_FEW_SHOT = String.join("\n\n",
            FEW_SHOT_LISTENING_RECORD_DETAIL,
            FEW_SHOT_READING_QUESTION_DETAIL,
            FEW_SHOT_WRITING_RECORD_DETAIL,
            FEW_SHOTS_SPEAKING_RECORD_DETAIL,
            FEW_SHOT_UNSUPPORTED
    );
}
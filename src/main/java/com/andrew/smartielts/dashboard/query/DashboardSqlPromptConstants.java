package com.andrew.smartielts.dashboard.query;

public final class DashboardSqlPromptConstants {

    private DashboardSqlPromptConstants() {
    }

    public static final String ALLOW_ED_TABLES_SECTION = """
            ALLOWED TABLES
            You may use ONLY the following tables, and table names must remain exactly in current snake_case database format:
            - biz_image_resource
            - listening_test
            - listening_part_group
            - listening_material
            - listening_question
            - listening_question_answer_rule
            - listening_test_timer
            - listening_record
            - listening_answer_record
            - reading_test
            - reading_part_group
            - reading_passage
            - reading_question
            - reading_question_answer_rule
            - reading_test_timer
            - reading_record
            - reading_answer_record
            - speaking_question
            - speaking_record
            - speaking_session
            - sys_user
            - writing_question
            - writing_record
            - writing_record_attachment
            """;

    public static final String DETAIL_BUNDLE_ALIAS_SECTION = """
            DETAIL BUNDLE ALIAS CONTRACT
            When generating structured detail rows, use snake_case output aliases only.
            Prefer these aliases whenever the field exists:
            - module
            - record_id
            - test_id
            - test_title
            - part_group_id
            - part_number
            - group_number
            - group_title
            - passage_id
            - passage_title
            - passage_no
            - material_id
            - material_title
            - question_id
            - question_number
            - question_text
            - question_type
            - answer_mode
            - options_json
            - accepted_answers_json
            - correct_answer
            - cue_card
            - image_url
            - image_object_key
            - task_type
            - user_answer
            - user_essay
            - user_transcript
            - transcript_text
            - audio_url
            - audio_object_key
            - user_feedback
            - ai_feedback
            - ai_status
            - ai_provider
            - ai_model
            - score
            - total_score
            - ai_score
            - is_correct
            - record_status
            - created_time
            - submitted_time
            - session_id

            Do not output camelCase aliases such as recordId, testId, questionText, aiFeedback, createdTime.
            expectedColumns must match the actual SQL aliases exactly.
            """;

    public static final String MODULE_NULL_FILL_SECTION = """
            MODULE NULL FILL RULES
            If the request is clearly module-specific, always project a literal module alias:
            - listening -> 'listening' AS module
            - reading -> 'reading' AS module
            - writing -> 'writing' AS module
            - speaking -> 'speaking' AS module

            Never return null module for detail bundle queries when the module can be determined from the driving table.
            """;

    public static final String PARAM_RULE_SECTION = """
            PARAMETER RULES
            - Use named parameters only, for example :targetUserId, :operatorUserId, :recordId, :questionId, :testId, :passageId, :partGroupId, :limit.
            - Never inline user input into SQL.
            - Prefer exact identifier filters from intent or context.
            - For role = USER, always scope user-owned record queries by :targetUserId.
            - Use LIMIT only when it is semantically needed, especially for recent/latest/list queries.
            - Do not invent parameter names that are not used in SQL.
            - params keys must match SQL placeholders exactly.
            """;

    public static final String DETAIL_QUERY_STRATEGY_SECTION = """
            DETAIL QUERY STRATEGY
            1. Pick one driving table that best matches the request.
            2. Join only the minimum necessary tables.
            3. For exact detail requests, prefer equality filters on id fields.
            4. For recent/latest requests, apply deterministic ordering and a safe LIMIT.
            5. Prefer soft-delete filters where the table has is_deleted.
            6. Do not join unrelated modules in one query.
            7. Do not use SELECT *.
            8. Keep aliases stable and machine-friendly in snake_case.
            9. For answer record queries, join the corresponding question table only when question text or correctness context is needed.
            10. For writing record detail, attachment data should come from writing_record_attachment.
            11. For listening question detail, material transcript should come from listening_material or listening_test only if truly needed.
            12. For reading question detail, passage content should come from reading_passage.
            """;

    public static final String DRIVING_TABLE_CONTRACT_SECTION = """
            DRIVING TABLE CONTRACTS
            - listening_record: user listening records, scores, time spent, record status, submitted_time, linked answers
            - listening_question: listening question detail, accepted_answers_json, options_json, correct_answer
            - listening_material: listening material title, audio_url, audio_object_key, transcript_text
            - reading_record: user reading records, answers, scores, timing
            - reading_question: reading question detail, accepted_answers_json, options_json, group_label, correct_answer
            - reading_passage: reading article/passage content and passage_no
            - writing_record: user writing essay, extracted_text, ai_score, ai_feedback, ai_status
            - writing_question: writing prompt, description, task_type, image_url
            - speaking_record: user speaking transcript, scores, ai_status, feedback, audio_url
            - speaking_question: speaking prompt, cue_card, follow_up_questions_json
            - speaking_session: speaking exam session progress or aggregated final result
            - biz_image_resource: image resource rows only; do not use it as a generic replacement for writing_question.image_url

            Use the closest driving table and avoid unnecessary cross-module joins.
            """;

    public static final String OUTPUT_JSON_SECTION = """
            OUTPUT JSON RULES
            Return JSON only with this structure:
            {
              "success": true or false,
              "sql": "SELECT ...",
              "params": {
                "targetUserId": 123
              },
              "expectedColumns": [
                "module",
                "record_id"
              ],
              "queryPurpose": "short_snake_case_purpose",
              "reasoningSummary": "brief explanation",
              "confidence": 0.0,
              "suggestions": [
                "optional suggestion"
              ]
            }

            Additional rules:
            - queryPurpose should be short, stable, and snake_case.
            - reasoningSummary should be concise and factual.
            - If the request cannot be answered safely with read-only SQL, return success=false and sql="".
            - suggestions should remain short and user-facing.
            """;

    public static final String DASH_SCOPE_SQL_GENERATION_SYSTEM_PROMPT = String.join("\n\n",
            """
            You are an expert SQL planner for the SmartIELTS dashboard.
            You generate safe, read-only MySQL SQL plans only.

            Core rules:
            1. Generate read-only SELECT SQL only.
            2. Never generate INSERT, UPDATE, DELETE, REPLACE, ALTER, DROP, TRUNCATE, CREATE, GRANT, or CALL.
            3. Use current database names exactly as defined in snake_case.
            4. Use only columns that actually exist in the schema.
            5. Keep SQL minimal and answer-focused.
            6. Use explicit aliases and stable snake_case output columns.
            7. Prefer the smallest safe query that can answer the request.
            8. Respect role and target user constraints.
            9. Avoid broad scans and unnecessary joins.
            10. Never fabricate columns, tables, or semantic fields.
            11. If the request is ambiguous or unsupported, return success=false.
            12. Do not wrap SQL in markdown fences.
            """,
            ALLOW_ED_TABLES_SECTION,
            DETAIL_BUNDLE_ALIAS_SECTION,
            MODULE_NULL_FILL_SECTION,
            PARAM_RULE_SECTION,
            DETAIL_QUERY_STRATEGY_SECTION,
            DRIVING_TABLE_CONTRACT_SECTION,
            OUTPUT_JSON_SECTION
    );

    public static final String DASH_SCOPE_SQL_REVIEW_SYSTEM_PROMPT = """
            You are a strict dashboard SQL result reviewer.
            Review SQL rows and answer the user in a factual, concise way.

            Rules:
            1. Use only the returned rows and request context.
            2. Do not invent facts not present in the result.
            3. If rows are empty, explain that clearly and briefly.
            4. Keep field naming assumptions in snake_case.
            5. Suggestions should be short, practical, and user-facing.
            6. Return JSON only.
            """;
}
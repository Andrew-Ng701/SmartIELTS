package com.andrew.smartielts.dashboard.query;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DashboardTableSchemaRegistry {

    public static final String TABLE_BIZ_IMAGE_RESOURCE = "biz_image_resource";
    public static final String TABLE_LISTENING_TEST = "listening_test";
    public static final String TABLE_LISTENING_PART_GROUP = "listening_part_group";
    public static final String TABLE_LISTENING_MATERIAL = "listening_material";
    public static final String TABLE_LISTENING_QUESTION = "listening_question";
    public static final String TABLE_LISTENING_QUESTION_ANSWER_RULE = "listening_question_answer_rule";
    public static final String TABLE_LISTENING_TEST_TIMER = "listening_test_timer";
    public static final String TABLE_LISTENING_RECORD = "listening_record";
    public static final String TABLE_LISTENING_ANSWER_RECORD = "listening_answer_record";

    public static final String TABLE_READING_TEST = "reading_test";
    public static final String TABLE_READING_PART_GROUP = "reading_part_group";
    public static final String TABLE_READING_PASSAGE = "reading_passage";
    public static final String TABLE_READING_QUESTION = "reading_question";
    public static final String TABLE_READING_QUESTION_ANSWER_RULE = "reading_question_answer_rule";
    public static final String TABLE_READING_TEST_TIMER = "reading_test_timer";
    public static final String TABLE_READING_RECORD = "reading_record";
    public static final String TABLE_READING_ANSWER_RECORD = "reading_answer_record";

    public static final String TABLE_SPEAKING_QUESTION = "speaking_question";
    public static final String TABLE_SPEAKING_RECORD = "speaking_record";
    public static final String TABLE_SPEAKING_SESSION = "speaking_session";

    public static final String TABLE_SYS_USER = "sys_user";
    public static final String TABLE_WRITING_QUESTION = "writing_question";
    public static final String TABLE_WRITING_RECORD = "writing_record";
    public static final String TABLE_WRITING_RECORD_ATTACHMENT = "writing_record_attachment";

    private final Map<String, DashboardTableSchemaContract> contracts;

    public DashboardTableSchemaRegistry() {
        Map<String, DashboardTableSchemaContract> map = new LinkedHashMap<>();
        register(map, bizImageResourceContract());
        register(map, listeningMaterialContract());
        register(map, listeningQuestionContract());
        register(map, listeningRecordContract());
        register(map, readingPassageContract());
        register(map, readingQuestionContract());
        register(map, readingRecordContract());
        register(map, writingQuestionContract());
        register(map, writingRecordContract());
        register(map, speakingQuestionContract());
        register(map, speakingRecordContract());
        register(map, speakingSessionContract());
        this.contracts = Collections.unmodifiableMap(map);
    }

    public Optional<DashboardTableSchemaContract> find(String primaryTable) {
        return Optional.ofNullable(contracts.get(normalize(primaryTable)));
    }

    public DashboardTableSchemaContract getRequired(String primaryTable) {
        DashboardTableSchemaContract contract = contracts.get(normalize(primaryTable));
        if (contract == null) {
            throw new IllegalArgumentException("Unsupported primary_table contract: " + primaryTable);
        }
        return contract;
    }

    public boolean supports(String primaryTable) {
        return contracts.containsKey(normalize(primaryTable));
    }

    public Set<String> getRegisteredPrimaryTables() {
        return contracts.keySet();
    }

    public String buildPromptSection(String primaryTable) {
        return getRequired(primaryTable).toPromptBlock();
    }

    private void register(Map<String, DashboardTableSchemaContract> map,
                          DashboardTableSchemaContract contract) {
        map.put(normalize(contract.primaryTable()), contract);
    }

    private DashboardTableSchemaContract bizImageResourceContract() {
        return new DashboardTableSchemaContract(
                TABLE_BIZ_IMAGE_RESOURCE,
                "Image resource binding queries. Use only when the request is explicitly about stored image resources, object keys, file URLs, or target binding.",
                columns(
                        "id", "target_type", "target_id", "bucket_type", "biz_path", "file_url", "object_key",
                        "original_name", "content_type", "file_size", "width", "height",
                        "sort_order", "created_time", "is_deleted"
                ),
                joinMap(),
                aliases(
                        "image_id", "target_type", "target_id", "bucket_type", "biz_path", "file_url", "object_key",
                        "original_name", "content_type", "file_size", "width", "height",
                        "sort_order", "created_time", "is_deleted"
                ),
                params("target_type", "target_id", "limit"),
                List.of(
                        "Prefer exact filters on target_type and target_id.",
                        "Do not infer polymorphic business joins unless the system explicitly changes the primary_table.",
                        "Prefer ORDER BY sort_order ASC, id ASC when returning multiple images."
                )
        );
    }

    private DashboardTableSchemaContract listeningMaterialContract() {
        return new DashboardTableSchemaContract(
                TABLE_LISTENING_MATERIAL,
                "Listening material queries. Use for audio material, material transcript, or material-level content under a listening part group.",
                columns(
                        "id", "test_id", "part_group_id", "title", "audio_url", "audio_object_key",
                        "transcript_text", "display_order", "is_deleted"
                ),
                joinMap(
                        join(TABLE_LISTENING_TEST, columns(
                                "id", "title", "audio_url", "total_score", "created_time", "audio_object_key", "is_deleted", "transcript_text"
                        )),
                        join(TABLE_LISTENING_PART_GROUP, columns(
                                "id", "test_id", "part_number", "group_number", "title", "instruction_text",
                                "display_order", "time_limit_seconds", "is_deleted", "group_guide_text",
                                "group_requirement_text", "question_no_start", "question_no_end"
                        ))
                ),
                aliases(
                        "module", "material_id", "test_id", "test_title", "part_group_id", "part_number", "group_number",
                        "group_title", "instruction_text", "group_guide_text", "group_requirement_text",
                        "question_no_start", "question_no_end",
                        "title", "audio_url", "audio_object_key", "transcript_text",
                        "display_order", "is_deleted"
                ),
                params("test_id", "part_group_id", "material_id", "limit"),
                List.of(
                        "Use listening_material as the true driving table.",
                        "Do not invent question-level fields unless joining listening_question is explicitly allowed by another contract.",
                        "For one material, prefer lm.id = :material_id."
                )
        );
    }

    private DashboardTableSchemaContract listeningQuestionContract() {
        return new DashboardTableSchemaContract(
                TABLE_LISTENING_QUESTION,
                "Listening question queries. Use for question detail, accepted answers, answer mode, section, and question metadata.",
                columns(
                        "id", "test_id", "part_group_id", "material_id", "section_number", "question_number",
                        "question_type", "answer_mode", "question_text", "correct_answer", "options_json",
                        "accepted_answers_json", "case_insensitive", "ignore_whitespace",
                        "ignore_punctuation", "display_order", "score", "is_deleted"
                ),
                joinMap(
                        join(TABLE_LISTENING_TEST, columns(
                                "id", "title", "audio_url", "total_score", "created_time", "audio_object_key", "is_deleted", "transcript_text"
                        )),
                        join(TABLE_LISTENING_PART_GROUP, columns(
                                "id", "test_id", "part_number", "group_number", "title", "instruction_text",
                                "display_order", "time_limit_seconds", "is_deleted", "group_guide_text",
                                "group_requirement_text", "question_no_start", "question_no_end"
                        )),
                        join(TABLE_LISTENING_MATERIAL, columns(
                                "id", "test_id", "part_group_id", "title", "audio_url", "audio_object_key",
                                "transcript_text", "display_order", "is_deleted"
                        )),
                        join(TABLE_LISTENING_QUESTION_ANSWER_RULE, columns(
                                "id", "question_id", "blank_no", "answer_group_no", "answer_text",
                                "normalized_answer", "is_primary", "display_order"
                        ))
                ),
                aliases(
                        "module", "question_id", "test_id", "test_title", "part_group_id", "material_id",
                        "section_number", "question_number", "question_type", "answer_mode",
                        "question_text", "correct_answer", "options_json", "accepted_answers_json",
                        "case_insensitive", "ignore_whitespace", "ignore_punctuation",
                        "display_order", "score", "is_deleted",
                        "part_number", "group_number", "group_title", "instruction_text",
                        "group_guide_text", "group_requirement_text", "question_no_start", "question_no_end",
                        "material_title", "audio_url", "audio_object_key", "transcript_text",
                        "answer_rule_id", "blank_no", "answer_group_no", "answer_text",
                        "normalized_answer", "is_primary"
                ),
                params("test_id", "part_group_id", "material_id", "question_id", "question_number", "limit"),
                List.of(
                        "For exact question requests, prefer lq.id = :question_id.",
                        "When the user refers to question number, use lq.question_number = :question_number with test or part-group scope.",
                        "Do not add record-level user_answer fields unless the primary_table is switched to listening_record."
                )
        );
    }

    private DashboardTableSchemaContract listeningRecordContract() {
        return new DashboardTableSchemaContract(
                TABLE_LISTENING_RECORD,
                "Listening record queries. Use for one user record, answer results, listening score, timing, and record-level detail.",
                columns(
                        "id", "user_id", "test_id", "session_id", "started_time", "submitted_time",
                        "time_limit_seconds", "time_spent_seconds", "record_status", "total_score",
                        "created_time", "is_deleted"
                ),
                joinMap(
                        join(TABLE_LISTENING_TEST, columns(
                                "id", "title", "audio_url", "total_score", "created_time", "audio_object_key", "is_deleted", "transcript_text"
                        )),
                        join(TABLE_LISTENING_ANSWER_RECORD, columns(
                                "id", "record_id", "part_group_id", "question_id", "user_answer",
                                "normalized_answer", "raw_answers_json", "is_correct", "score"
                        )),
                        join(TABLE_LISTENING_QUESTION, columns(
                                "id", "test_id", "part_group_id", "material_id", "section_number", "question_number",
                                "question_type", "answer_mode", "question_text", "correct_answer", "options_json",
                                "accepted_answers_json", "case_insensitive", "ignore_whitespace",
                                "ignore_punctuation", "display_order", "score", "is_deleted"
                        )),
                        join(TABLE_LISTENING_PART_GROUP, columns(
                                "id", "test_id", "part_number", "group_number", "title", "instruction_text",
                                "display_order", "time_limit_seconds", "is_deleted", "group_guide_text",
                                "group_requirement_text", "question_no_start", "question_no_end"
                        )),
                        join(TABLE_LISTENING_MATERIAL, columns(
                                "id", "test_id", "part_group_id", "title", "audio_url", "audio_object_key",
                                "transcript_text", "display_order", "is_deleted"
                        ))
                ),
                aliases(
                        "module", "record_id", "user_id", "test_id", "test_title",
                        "session_id", "started_time", "submitted_time", "time_limit_seconds",
                        "time_spent_seconds", "record_status", "total_score", "created_time", "is_deleted",
                        "answer_id", "part_group_id", "question_id", "user_answer", "normalized_answer",
                        "raw_answers_json", "is_correct", "score",
                        "material_id", "material_title", "audio_url", "audio_object_key", "transcript_text",
                        "section_number", "question_number", "question_type", "answer_mode",
                        "question_text", "correct_answer", "options_json", "accepted_answers_json",
                        "part_number", "group_number", "group_title", "instruction_text",
                        "group_guide_text", "group_requirement_text", "question_no_start", "question_no_end"
                ),
                params("target_user_id", "record_id", "test_id", "session_id", "limit"),
                List.of(
                        "For user-scoped record queries, bind lr.user_id = :target_user_id.",
                        "For exact record detail, prefer lr.id = :record_id.",
                        "Do not invent writing or speaking fields such as ai_feedback, transcript, cue_card, or text_content."
                )
        );
    }

    private DashboardTableSchemaContract readingPassageContract() {
        return new DashboardTableSchemaContract(
                TABLE_READING_PASSAGE,
                "Reading passage queries. Use for passage content, passage title, passage number, and passage-level material detail.",
                columns(
                        "id", "test_id", "part_group_id", "title", "material_type", "content",
                        "display_order", "is_deleted", "passage_no"
                ),
                joinMap(
                        join(TABLE_READING_TEST, columns(
                                "id", "title", "total_score", "created_time", "is_deleted"
                        )),
                        join(TABLE_READING_PART_GROUP, columns(
                                "id", "test_id", "part_number", "group_number", "title", "instruction_text",
                                "display_order", "time_limit_seconds", "is_deleted", "group_guide_text",
                                "group_requirement_text", "question_no_start", "question_no_end"
                        ))
                ),
                aliases(
                        "module", "passage_id", "test_id", "test_title", "part_group_id",
                        "part_number", "group_number", "group_title", "instruction_text",
                        "group_guide_text", "group_requirement_text", "question_no_start", "question_no_end",
                        "title", "material_type", "content", "display_order", "is_deleted", "passage_no"
                ),
                params("test_id", "part_group_id", "passage_id", "passage_no", "limit"),
                List.of(
                        "For exact passage lookup, prefer rp.id = :passage_id.",
                        "If the request is about a numbered passage within one test, use rp.test_id and rp.passage_no.",
                        "Do not add user record fields unless the primary_table is switched to reading_record."
                )
        );
    }

    private DashboardTableSchemaContract readingQuestionContract() {
        return new DashboardTableSchemaContract(
                TABLE_READING_QUESTION,
                "Reading question queries. Use for question text, correct answer, options, group label, and linked passage detail.",
                columns(
                        "id", "passage_id", "part_group_id", "question_number", "question_text", "correct_answer",
                        "score", "question_type", "answer_mode", "options_json", "accepted_answers_json",
                        "case_insensitive", "ignore_whitespace", "ignore_punctuation",
                        "group_label", "display_order", "is_deleted"
                ),
                joinMap(
                        join(TABLE_READING_PASSAGE, columns(
                                "id", "test_id", "part_group_id", "title", "material_type", "content",
                                "display_order", "is_deleted", "passage_no"
                        )),
                        join(TABLE_READING_TEST, columns(
                                "id", "title", "total_score", "created_time", "is_deleted"
                        )),
                        join(TABLE_READING_PART_GROUP, columns(
                                "id", "test_id", "part_number", "group_number", "title", "instruction_text",
                                "display_order", "time_limit_seconds", "is_deleted", "group_guide_text",
                                "group_requirement_text", "question_no_start", "question_no_end"
                        )),
                        join(TABLE_READING_QUESTION_ANSWER_RULE, columns(
                                "id", "question_id", "blank_no", "answer_group_no", "answer_text",
                                "normalized_answer", "is_primary", "display_order"
                        ))
                ),
                aliases(
                        "module", "question_id", "passage_id", "test_id", "test_title",
                        "part_group_id", "question_number", "question_text", "correct_answer",
                        "score", "question_type", "answer_mode", "options_json", "accepted_answers_json",
                        "case_insensitive", "ignore_whitespace", "ignore_punctuation",
                        "group_label", "display_order", "is_deleted",
                        "passage_title", "material_type", "content", "passage_no",
                        "part_number", "group_number", "group_title", "instruction_text",
                        "group_guide_text", "group_requirement_text", "question_no_start", "question_no_end",
                        "answer_rule_id", "blank_no", "answer_group_no", "answer_text",
                        "normalized_answer", "is_primary"
                ),
                params("test_id", "passage_id", "part_group_id", "question_id", "question_number", "limit"),
                List.of(
                        "For exact question requests, prefer rq.id = :question_id.",
                        "If the request is passage-specific, constrain rq.passage_id = :passage_id.",
                        "Do not add user_answer or record_status unless the primary_table is switched to reading_record."
                )
        );
    }

    private DashboardTableSchemaContract readingRecordContract() {
        return new DashboardTableSchemaContract(
                TABLE_READING_RECORD,
                "Reading record queries. Use for one user reading record, reading answers, scores, and record timing.",
                columns(
                        "id", "user_id", "test_id", "session_id", "started_time", "submitted_time",
                        "time_limit_seconds", "time_spent_seconds", "record_status", "total_score",
                        "created_time", "is_deleted"
                ),
                joinMap(
                        join(TABLE_READING_TEST, columns(
                                "id", "title", "total_score", "created_time", "is_deleted"
                        )),
                        join(TABLE_READING_ANSWER_RECORD, columns(
                                "id", "record_id", "part_group_id", "question_id", "user_answer",
                                "normalized_answer", "raw_answers_json", "is_correct", "score"
                        )),
                        join(TABLE_READING_QUESTION, columns(
                                "id", "passage_id", "part_group_id", "question_number", "question_text", "correct_answer",
                                "score", "question_type", "answer_mode", "options_json", "accepted_answers_json",
                                "case_insensitive", "ignore_whitespace", "ignore_punctuation",
                                "group_label", "display_order", "is_deleted"
                        )),
                        join(TABLE_READING_PASSAGE, columns(
                                "id", "test_id", "part_group_id", "title", "material_type", "content",
                                "display_order", "is_deleted", "passage_no"
                        )),
                        join(TABLE_READING_PART_GROUP, columns(
                                "id", "test_id", "part_number", "group_number", "title", "instruction_text",
                                "display_order", "time_limit_seconds", "is_deleted", "group_guide_text",
                                "group_requirement_text", "question_no_start", "question_no_end"
                        ))
                ),
                aliases(
                        "module", "record_id", "user_id", "test_id", "test_title",
                        "session_id", "started_time", "submitted_time", "time_limit_seconds",
                        "time_spent_seconds", "record_status", "total_score", "created_time", "is_deleted",
                        "answer_id", "part_group_id", "question_id", "user_answer", "normalized_answer",
                        "raw_answers_json", "is_correct", "score",
                        "passage_id", "passage_title", "material_type", "content", "passage_no",
                        "question_number", "question_text", "correct_answer", "question_type", "answer_mode",
                        "options_json", "accepted_answers_json", "group_label",
                        "part_number", "group_number", "group_title", "instruction_text",
                        "group_guide_text", "group_requirement_text", "question_no_start", "question_no_end"
                ),
                params("target_user_id", "record_id", "test_id", "session_id", "limit"),
                List.of(
                        "For user-scoped record queries, bind rr.user_id = :target_user_id.",
                        "For exact record detail, prefer rr.id = :record_id.",
                        "Do not invent transcript, cue_card, ai_feedback, or image_url in reading_record queries."
                )
        );
    }

    private DashboardTableSchemaContract writingQuestionContract() {
        return new DashboardTableSchemaContract(
                TABLE_WRITING_QUESTION,
                "Writing question queries. Use for writing prompt, description, task type, and image metadata.",
                columns(
                        "id", "task_type", "title", "description", "created_time",
                        "image_url", "image_object_key", "is_deleted", "deleted_time", "image_target_migrated"
                ),
                joinMap(),
                aliases(
                        "module", "question_id", "task_type", "title", "description",
                        "created_time", "image_url", "image_object_key", "is_deleted",
                        "deleted_time", "image_target_migrated"
                ),
                params("question_id", "task_type", "limit"),
                List.of(
                        "Use writing_question for prompt-only or image-only requests.",
                        "Do not invent record-level fields such as text_content, ai_score, or ai_feedback unless the primary_table is switched to writing_record."
                )
        );
    }

    private DashboardTableSchemaContract writingRecordContract() {
        return new DashboardTableSchemaContract(
                TABLE_WRITING_RECORD,
                "Writing record queries. Use for essay text, extracted text, target score, AI score, AI feedback, and attachment data.",
                columns(
                        "id", "user_id", "question_id", "input_type", "text_content", "extracted_text",
                        "target_score", "ai_score", "ai_feedback", "ai_raw_response",
                        "ai_status", "ai_provider", "ai_model", "created_time",
                        "is_deleted", "deleted_time"
                ),
                joinMap(
                        join(TABLE_WRITING_QUESTION, columns(
                                "id", "task_type", "title", "description", "created_time",
                                "image_url", "image_object_key", "is_deleted", "deleted_time", "image_target_migrated"
                        )),
                        join(TABLE_WRITING_RECORD_ATTACHMENT, columns(
                                "id", "record_id", "file_type", "file_url", "file_key",
                                "sort_order", "created_time", "ocr_text"
                        ))
                ),
                aliases(
                        "module", "record_id", "user_id", "question_id",
                        "input_type", "text_content", "extracted_text",
                        "target_score", "ai_score", "ai_feedback", "ai_raw_response",
                        "ai_status", "ai_provider", "ai_model", "created_time",
                        "is_deleted", "deleted_time",
                        "task_type", "title", "description", "image_url", "image_object_key", "image_target_migrated",
                        "attachment_id", "file_type", "file_url", "file_key",
                        "sort_order", "attachment_created_time", "ocr_text"
                ),
                params("target_user_id", "record_id", "question_id", "limit"),
                List.of(
                        "For user-scoped record queries, bind wr.user_id = :target_user_id.",
                        "For exact record detail, prefer wr.id = :record_id.",
                        "Do not invent session_id, overall_score, transcript, or cue_card in writing_record queries."
                )
        );
    }

    private DashboardTableSchemaContract speakingQuestionContract() {
        return new DashboardTableSchemaContract(
                TABLE_SPEAKING_QUESTION,
                "Speaking question queries. Use for question text, cue card, follow-up questions, timing, and speaking prompt metadata.",
                columns(
                        "id", "part", "sub_type", "topic_key", "question_text", "cue_card",
                        "follow_up_questions_json", "prep_seconds", "answer_seconds",
                        "display_order", "active", "created_time", "is_deleted", "deleted_time"
                ),
                joinMap(),
                aliases(
                        "module", "question_id", "part", "sub_type", "topic_key",
                        "question_text", "cue_card", "follow_up_questions_json",
                        "prep_seconds", "answer_seconds", "display_order",
                        "active", "created_time", "is_deleted", "deleted_time"
                ),
                params("question_id", "part", "sub_type", "topic_key", "limit"),
                List.of(
                        "Use speaking_question for prompt-only or cue-card-only requests.",
                        "Do not invent record-level fields such as transcript, audio_url, feedback, or overall_score unless the primary_table is switched to speaking_record."
                )
        );
    }

    private DashboardTableSchemaContract speakingRecordContract() {
        return new DashboardTableSchemaContract(
                TABLE_SPEAKING_RECORD,
                "Speaking record queries. Use for user transcript, audio, band criteria scores, AI status, and linked speaking prompt/session data.",
                columns(
                        "id", "user_id", "session_id", "question_id", "audio_url", "transcript",
                        "fluency_and_coherence", "lexical_resource", "grammatical_range_and_accuracy",
                        "pronunciation", "overall_score", "feedback", "answer_status",
                        "is_deleted", "deleted_time", "ai_status", "ai_provider", "ai_model",
                        "ai_error_message", "created_time", "updated_time",
                        "relevance_comment", "quality_comment"
                ),
                joinMap(
                        join(TABLE_SPEAKING_QUESTION, columns(
                                "id", "part", "sub_type", "topic_key", "question_text", "cue_card",
                                "follow_up_questions_json", "prep_seconds", "answer_seconds",
                                "display_order", "active", "created_time", "is_deleted", "deleted_time"
                        )),
                        join(TABLE_SPEAKING_SESSION, columns(
                                "id", "session_id", "user_id", "exam_type", "total_questions", "current_index",
                                "exam_status", "exam_plan_json", "fluency_and_coherence", "lexical_resource",
                                "grammatical_range_and_accuracy", "pronunciation", "overall_score",
                                "final_feedback", "started_time", "completed_time", "created_time", "updated_time"
                        ))
                ),
                aliases(
                        "module", "record_id", "user_id", "session_id", "question_id",
                        "audio_url", "transcript",
                        "fluency_and_coherence", "lexical_resource", "grammatical_range_and_accuracy",
                        "pronunciation", "overall_score", "feedback", "answer_status",
                        "is_deleted", "deleted_time", "ai_status", "ai_provider", "ai_model",
                        "ai_error_message", "created_time", "updated_time",
                        "relevance_comment", "quality_comment",
                        "part", "sub_type", "topic_key", "question_text", "cue_card",
                        "follow_up_questions_json", "prep_seconds", "answer_seconds",
                        "exam_type", "total_questions", "current_index", "exam_status",
                        "exam_plan_json", "final_feedback", "started_time", "completed_time"
                ),
                params("target_user_id", "record_id", "session_id", "question_id", "limit"),
                List.of(
                        "For user-scoped record queries, bind sr.user_id = :target_user_id.",
                        "For exact record detail, prefer sr.id = :record_id.",
                        "Use speaking_session only through the shared session_id when session context is needed."
                )
        );
    }

    private DashboardTableSchemaContract speakingSessionContract() {
        return new DashboardTableSchemaContract(
                TABLE_SPEAKING_SESSION,
                "Speaking session queries. Use for session progress, exam status, total questions, final feedback, and session-level scores.",
                columns(
                        "id", "session_id", "user_id", "exam_type", "total_questions", "current_index",
                        "exam_status", "exam_plan_json", "fluency_and_coherence", "lexical_resource",
                        "grammatical_range_and_accuracy", "pronunciation", "overall_score",
                        "final_feedback", "started_time", "completed_time", "created_time", "updated_time"
                ),
                joinMap(
                        join(TABLE_SPEAKING_RECORD, columns(
                                "id", "user_id", "session_id", "question_id", "audio_url", "transcript",
                                "fluency_and_coherence", "lexical_resource", "grammatical_range_and_accuracy",
                                "pronunciation", "overall_score", "feedback", "answer_status",
                                "is_deleted", "deleted_time", "ai_status", "ai_provider", "ai_model",
                                "ai_error_message", "created_time", "updated_time",
                                "relevance_comment", "quality_comment"
                        )),
                        join(TABLE_SPEAKING_QUESTION, columns(
                                "id", "part", "sub_type", "topic_key", "question_text", "cue_card",
                                "follow_up_questions_json", "prep_seconds", "answer_seconds",
                                "display_order", "active", "created_time", "is_deleted", "deleted_time"
                        ))
                ),
                aliases(
                        "module", "session_pk_id", "session_id", "user_id",
                        "exam_type", "total_questions", "current_index", "exam_status",
                        "exam_plan_json",
                        "fluency_and_coherence", "lexical_resource", "grammatical_range_and_accuracy",
                        "pronunciation", "overall_score", "final_feedback",
                        "started_time", "completed_time", "created_time", "updated_time",
                        "record_id", "question_id", "audio_url", "transcript", "feedback",
                        "answer_status", "ai_status", "ai_provider", "ai_model", "ai_error_message",
                        "relevance_comment", "quality_comment",
                        "part", "sub_type", "topic_key", "question_text", "cue_card",
                        "follow_up_questions_json", "prep_seconds", "answer_seconds"
                ),
                params("target_user_id", "session_id", "limit"),
                List.of(
                        "For user-scoped session queries, bind ss.user_id = :target_user_id.",
                        "For one session, prefer ss.session_id = :session_id.",
                        "Do not invent writing or listening fields in speaking_session queries."
                )
        );
    }

    private static Set<String> columns(String... values) {
        return normalizeSet(values);
    }

    private static Set<String> aliases(String... values) {
        return normalizeSet(values);
    }

    private static Set<String> params(String... values) {
        return normalizeSet(values);
    }

    private static JoinTable join(String tableName, Set<String> columns) {
        return new JoinTable(tableName, columns);
    }

    private static Map<String, Set<String>> joinMap(JoinTable... joins) {
        if (joins == null || joins.length == 0) {
            return Map.of();
        }
        Map<String, Set<String>> result = new LinkedHashMap<>();
        for (JoinTable join : joins) {
            result.put(normalize(join.tableName()), normalizeSet(join.columns()));
        }
        return Collections.unmodifiableMap(result);
    }

    private static Set<String> normalizeSet(String... values) {
        if (values == null || values.length == 0) {
            return Set.of();
        }
        return normalizeSet(Arrays.stream(values).collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    private static Set<String> normalizeSet(Set<String> values) {
        if (values == null || values.isEmpty()) {
            return Set.of();
        }
        Set<String> normalized = values.stream()
                .filter(Objects::nonNull)
                .map(DashboardTableSchemaRegistry::normalize)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return Collections.unmodifiableSet(normalized);
    }

    public static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private record JoinTable(String tableName, Set<String> columns) {
    }

    public record DashboardTableSchemaContract(
            String primaryTable,
            String description,
            Set<String> primaryColumns,
            Map<String, Set<String>> allowedJoinColumns,
            Set<String> allowedOutputAliases,
            Set<String> suggestedParams,
            List<String> queryRules
    ) {
        public DashboardTableSchemaContract {
            primaryTable = normalize(primaryTable);
            description = description == null ? "" : description.trim();
            primaryColumns = primaryColumns == null ? Set.of() : primaryColumns;
            allowedJoinColumns = allowedJoinColumns == null ? Map.of() : allowedJoinColumns;
            allowedOutputAliases = allowedOutputAliases == null ? Set.of() : allowedOutputAliases;
            suggestedParams = suggestedParams == null ? Set.of() : suggestedParams;
            queryRules = queryRules == null ? List.of() : List.copyOf(queryRules);
        }

        public boolean allowsTable(String tableName) {
            String normalized = normalize(tableName);
            return primaryTable.equals(normalized) || allowedJoinColumns.containsKey(normalized);
        }

        public boolean allowsColumn(String tableName, String columnName) {
            String normalizedTable = normalize(tableName);
            String normalizedColumn = normalize(columnName);
            if (primaryTable.equals(normalizedTable)) {
                return primaryColumns.contains(normalizedColumn);
            }
            Set<String> joinColumns = allowedJoinColumns.get(normalizedTable);
            return joinColumns != null && joinColumns.contains(normalizedColumn);
        }

        public boolean allowsOutputAlias(String alias) {
            return allowedOutputAliases.contains(normalize(alias));
        }

        public Set<String> allTables() {
            LinkedHashSet<String> result = new LinkedHashSet<>();
            result.add(primaryTable);
            result.addAll(allowedJoinColumns.keySet());
            return Collections.unmodifiableSet(result);
        }

        public String toPromptBlock() {
            StringBuilder sb = new StringBuilder();
            sb.append("PRIMARY_TABLE_CONTRACT\n");
            sb.append("primary_table: ").append(primaryTable).append('\n');
            sb.append("description: ").append(description).append('\n');
            sb.append("allowed_tables: ").append(String.join(", ", allTables())).append('\n');
            sb.append("primary_table_columns: ").append(String.join(", ", primaryColumns)).append('\n');

            if (!allowedJoinColumns.isEmpty()) {
                sb.append("allowed_join_columns:\n");
                for (Map.Entry<String, Set<String>> entry : allowedJoinColumns.entrySet()) {
                    sb.append("- ")
                            .append(entry.getKey())
                            .append(": ")
                            .append(String.join(", ", entry.getValue()))
                            .append('\n');
                }
            }

            sb.append("allowed_output_aliases: ").append(String.join(", ", allowedOutputAliases)).append('\n');

            if (!suggestedParams.isEmpty()) {
                sb.append("suggested_params: ").append(String.join(", ", suggestedParams)).append('\n');
            }

            if (!queryRules.isEmpty()) {
                sb.append("query_rules:\n");
                for (String rule : queryRules) {
                    sb.append("- ").append(rule).append('\n');
                }
            }

            sb.append("strict_rules:\n");
            sb.append("- After choosing this primary_table, remove every field, join, filter, and alias outside this contract.\n");
            sb.append("- Do not keep a cross-module unified shape.\n");
            sb.append("- Do not use NULL AS alias placeholders.\n");
            sb.append("- expected_columns must contain only actually selected aliases, in the same order.\n");
            sb.append("- Table names, column names, and aliases must stay in snake_case.\n");
            return sb.toString();
        }
    }
}
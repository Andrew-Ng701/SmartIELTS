package com.andrew.smartielts.listening.constants;

import java.util.Set;

public final class ListeningQuestionConstants {

    private ListeningQuestionConstants() {
    }

    public static final String ANSWER_MODE_TEXT = "TEXT";
    public static final String ANSWER_MODE_SINGLE = "SINGLE";
    public static final String ANSWER_MODE_MULTI = "MULTI";

    public static final String QUESTION_TYPE_MULTIPLE_CHOICE_SINGLE = "MULTIPLE_CHOICE_SINGLE";
    public static final String QUESTION_TYPE_MULTIPLE_CHOICE_MULTI = "MULTIPLE_CHOICE_MULTI";
    public static final String QUESTION_TYPE_TRUE_FALSE_NOT_GIVEN = "TRUE_FALSE_NOT_GIVEN";
    public static final String QUESTION_TYPE_YES_NO_NOT_GIVEN = "YES_NO_NOT_GIVEN";
    public static final String QUESTION_TYPE_MATCHING = "MATCHING";
    public static final String QUESTION_TYPE_HEADING_MATCHING = "HEADING_MATCHING";
    public static final String QUESTION_TYPE_SUMMARY_COMPLETION = "SUMMARY_COMPLETION";
    public static final String QUESTION_TYPE_SENTENCE_COMPLETION = "SENTENCE_COMPLETION";
    public static final String QUESTION_TYPE_SHORT_ANSWER = "SHORT_ANSWER";
    public static final String QUESTION_TYPE_TABLE_COMPLETION = "TABLE_COMPLETION";
    public static final String QUESTION_TYPE_FLOW_CHART_COMPLETION = "FLOW_CHART_COMPLETION";
    public static final String QUESTION_TYPE_DIAGRAM_LABEL_COMPLETION = "DIAGRAM_LABEL_COMPLETION";
    public static final String QUESTION_TYPE_FORM_COMPLETION = "FORM_COMPLETION";
    public static final String QUESTION_TYPE_NOTE_COMPLETION = "NOTE_COMPLETION";

    private static final Set<String> SUPPORTED_QUESTION_TYPES = Set.of(
            QUESTION_TYPE_MULTIPLE_CHOICE_SINGLE,
            QUESTION_TYPE_MULTIPLE_CHOICE_MULTI,
            QUESTION_TYPE_TRUE_FALSE_NOT_GIVEN,
            QUESTION_TYPE_YES_NO_NOT_GIVEN,
            QUESTION_TYPE_MATCHING,
            QUESTION_TYPE_HEADING_MATCHING,
            QUESTION_TYPE_SUMMARY_COMPLETION,
            QUESTION_TYPE_SENTENCE_COMPLETION,
            QUESTION_TYPE_SHORT_ANSWER,
            QUESTION_TYPE_TABLE_COMPLETION,
            QUESTION_TYPE_FLOW_CHART_COMPLETION,
            QUESTION_TYPE_DIAGRAM_LABEL_COMPLETION,
            QUESTION_TYPE_FORM_COMPLETION,
            QUESTION_TYPE_NOTE_COMPLETION
    );

    private static final Set<String> SUPPORTED_ANSWER_MODES = Set.of(
            ANSWER_MODE_TEXT,
            ANSWER_MODE_SINGLE,
            ANSWER_MODE_MULTI
    );

    public static boolean supports_question_type(String question_type) {
        String normalized = normalize_question_type(question_type);
        return normalized != null && SUPPORTED_QUESTION_TYPES.contains(normalized);
    }

    public static boolean supports_answer_mode(String answer_mode) {
        String normalized = normalize_answer_mode(answer_mode);
        return normalized != null && SUPPORTED_ANSWER_MODES.contains(normalized);
    }

    public static boolean is_multi_answer_mode(String answer_mode) {
        return ANSWER_MODE_MULTI.equals(normalize_answer_mode(answer_mode));
    }

    public static boolean is_single_answer_mode(String answer_mode) {
        String normalized = normalize_answer_mode(answer_mode);
        return ANSWER_MODE_SINGLE.equals(normalized) || ANSWER_MODE_TEXT.equals(normalized);
    }

    public static String normalize_answer_mode(String answer_mode) {
        String normalized = normalize_token(answer_mode);
        if (normalized == null) {
            return ANSWER_MODE_TEXT;
        }

        return switch (normalized) {
            case "TEXT", "INPUT", "FILL", "FILL_IN", "FILL_IN_BLANK", "BLANK" -> ANSWER_MODE_TEXT;
            case "SINGLE", "RADIO", "ONE", "SINGLE_CHOICE" -> ANSWER_MODE_SINGLE;
            case "MULTI", "MULTIPLE", "CHECKBOX", "MANY", "MULTI_CHOICE" -> ANSWER_MODE_MULTI;
            default -> ANSWER_MODE_TEXT;
        };
    }

    public static String normalize_question_type(String question_type) {
        String normalized = normalize_token(question_type);
        if (normalized == null) {
            return null;
        }

        return switch (normalized) {
            case "MULTIPLE_CHOICE_SINGLE", "MULTIPLECHOICESINGLE", "MCQ_SINGLE", "MCQSINGLE", "SINGLE_CHOICE"
                    -> QUESTION_TYPE_MULTIPLE_CHOICE_SINGLE;
            case "MULTIPLE_CHOICE_MULTI", "MULTIPLECHOICEMULTI", "MCQ_MULTI", "MCQMULTI", "MULTI_CHOICE"
                    -> QUESTION_TYPE_MULTIPLE_CHOICE_MULTI;
            case "TRUE_FALSE_NOT_GIVEN", "TRUEFALSENOTGIVEN"
                    -> QUESTION_TYPE_TRUE_FALSE_NOT_GIVEN;
            case "YES_NO_NOT_GIVEN", "YESNONOTGIVEN"
                    -> QUESTION_TYPE_YES_NO_NOT_GIVEN;
            case "MATCHING", "MATCH"
                    -> QUESTION_TYPE_MATCHING;
            case "HEADING_MATCHING", "HEADINGMATCHING"
                    -> QUESTION_TYPE_HEADING_MATCHING;
            case "SUMMARY_COMPLETION", "SUMMARYCOMPLETION", "SUMMARY"
                    -> QUESTION_TYPE_SUMMARY_COMPLETION;
            case "SENTENCE_COMPLETION", "SENTENCECOMPLETION", "SENTENCE"
                    -> QUESTION_TYPE_SENTENCE_COMPLETION;
            case "SHORT_ANSWER", "SHORTANSWER"
                    -> QUESTION_TYPE_SHORT_ANSWER;
            case "TABLE_COMPLETION", "TABLECOMPLETION", "TABLE"
                    -> QUESTION_TYPE_TABLE_COMPLETION;
            case "FLOW_CHART_COMPLETION", "FLOWCHARTCOMPLETION", "FLOWCHART"
                    -> QUESTION_TYPE_FLOW_CHART_COMPLETION;
            case "DIAGRAM_LABEL_COMPLETION", "DIAGRAMLABELCOMPLETION", "DIAGRAM_LABEL", "DIAGRAMLABEL", "DIAGRAM"
                    -> QUESTION_TYPE_DIAGRAM_LABEL_COMPLETION;
            case "FORM_COMPLETION", "FORMCOMPLETION", "FORM"
                    -> QUESTION_TYPE_FORM_COMPLETION;
            case "NOTE_COMPLETION", "NOTECOMPLETION", "NOTE"
                    -> QUESTION_TYPE_NOTE_COMPLETION;
            default -> normalized;
        };
    }

    public static String infer_answer_mode(String question_type, String answer_mode) {
        String normalized_question_type = normalize_question_type(question_type);
        String normalized_answer_mode = normalize_answer_mode(answer_mode);

        if (QUESTION_TYPE_MULTIPLE_CHOICE_MULTI.equals(normalized_question_type)) {
            return ANSWER_MODE_MULTI;
        }
        if (QUESTION_TYPE_MULTIPLE_CHOICE_SINGLE.equals(normalized_question_type)) {
            return ANSWER_MODE_SINGLE;
        }
        return normalized_answer_mode;
    }

    private static String normalize_token(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed
                .replace('-', '_')
                .replace(' ', '_')
                .toUpperCase();
    }
}
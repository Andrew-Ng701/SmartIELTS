package com.andrew.smartielts.reading.constant;

public final class ReadingQuestionConstants {

    private ReadingQuestionConstants() {
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

    public static boolean is_multi_answer_mode(String answer_mode) {
        return ANSWER_MODE_MULTI.equalsIgnoreCase(trim_to_null(answer_mode));
    }

    public static boolean is_single_answer_mode(String answer_mode) {
        String normalized_answer_mode = trim_to_null(answer_mode);
        return ANSWER_MODE_SINGLE.equalsIgnoreCase(normalized_answer_mode)
                || ANSWER_MODE_TEXT.equalsIgnoreCase(normalized_answer_mode);
    }

    public static String normalize_answer_mode(String answer_mode) {
        String normalized_answer_mode = trim_to_null(answer_mode);
        if (normalized_answer_mode == null) {
            return ANSWER_MODE_TEXT;
        }

        normalized_answer_mode = normalized_answer_mode
                .trim()
                .replace('-', '_')
                .replace(' ', '_')
                .toUpperCase();

        if ("INPUT".equals(normalized_answer_mode)
                || "FILL".equals(normalized_answer_mode)
                || "FILL_IN".equals(normalized_answer_mode)
                || "FILL_IN_BLANK".equals(normalized_answer_mode)) {
            return ANSWER_MODE_TEXT;
        }
        if ("RADIO".equals(normalized_answer_mode)
                || "ONE".equals(normalized_answer_mode)) {
            return ANSWER_MODE_SINGLE;
        }
        if ("MULTIPLE".equals(normalized_answer_mode)
                || "CHECKBOX".equals(normalized_answer_mode)
                || "MANY".equals(normalized_answer_mode)) {
            return ANSWER_MODE_MULTI;
        }
        if (ANSWER_MODE_MULTI.equals(normalized_answer_mode)) {
            return ANSWER_MODE_MULTI;
        }
        if (ANSWER_MODE_SINGLE.equals(normalized_answer_mode)) {
            return ANSWER_MODE_SINGLE;
        }
        return ANSWER_MODE_TEXT;
    }

    public static String normalize_question_type(String question_type) {
        String normalized_question_type = trim_to_null(question_type);
        if (normalized_question_type == null) {
            return null;
        }

        normalized_question_type = normalized_question_type
                .trim()
                .replace('-', '_')
                .replace(' ', '_')
                .toUpperCase();

        return switch (normalized_question_type) {
            case "MULTIPLECHOICESINGLE", "MULTIPLE_CHOICE_SINGLE", "MCQ_SINGLE", "SINGLE_CHOICE" ->
                    QUESTION_TYPE_MULTIPLE_CHOICE_SINGLE;
            case "MULTIPLECHOICEMULTI", "MULTIPLE_CHOICE_MULTI", "MCQ_MULTI", "MULTI_CHOICE" ->
                    QUESTION_TYPE_MULTIPLE_CHOICE_MULTI;
            case "TRUEFALSENOTGIVEN", "TRUE_FALSE_NOT_GIVEN" ->
                    QUESTION_TYPE_TRUE_FALSE_NOT_GIVEN;
            case "YESNONOTGIVEN", "YES_NO_NOT_GIVEN" ->
                    QUESTION_TYPE_YES_NO_NOT_GIVEN;
            case "MATCHING", "MATCH" ->
                    QUESTION_TYPE_MATCHING;
            case "HEADINGMATCHING", "HEADING_MATCHING" ->
                    QUESTION_TYPE_HEADING_MATCHING;
            case "SUMMARYCOMPLETION", "SUMMARY_COMPLETION", "SUMMARY" ->
                    QUESTION_TYPE_SUMMARY_COMPLETION;
            case "SENTENCECOMPLETION", "SENTENCE_COMPLETION", "SENTENCE" ->
                    QUESTION_TYPE_SENTENCE_COMPLETION;
            case "SHORTANSWER", "SHORT_ANSWER" ->
                    QUESTION_TYPE_SHORT_ANSWER;
            case "TABLECOMPLETION", "TABLE_COMPLETION", "TABLE" ->
                    QUESTION_TYPE_TABLE_COMPLETION;
            case "FLOWCHARTCOMPLETION", "FLOW_CHART_COMPLETION", "FLOW_CHART" ->
                    QUESTION_TYPE_FLOW_CHART_COMPLETION;
            case "DIAGRAMLABELCOMPLETION", "DIAGRAM_LABEL_COMPLETION", "DIAGRAM_LABEL", "DIAGRAM" ->
                    QUESTION_TYPE_DIAGRAM_LABEL_COMPLETION;
            default -> normalized_question_type;
        };
    }

    public static String resolve_answer_mode_by_question_type(String question_type, String answer_mode) {
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

    private static String trim_to_null(String value) {
        if (value == null) {
            return null;
        }
        String trimmed_value = value.trim();
        return trimmed_value.isEmpty() ? null : trimmed_value;
    }
}
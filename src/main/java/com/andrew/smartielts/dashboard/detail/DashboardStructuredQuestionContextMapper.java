package com.andrew.smartielts.dashboard.detail;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DashboardStructuredQuestionContextMapper {

    private final ObjectMapper objectMapper;

    public DashboardStructuredQuestionContextMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> map_rows_to_ai_context(
            String ask_scene,
            String object_type,
            List<Map<String, Object>> rows
    ) {
        Map<String, Object> question_context = map_rows_to_question_context(rows);

        Map<String, Object> object_ref = new LinkedHashMap<>();
        object_ref.put(DashboardDetailBundleConstants.KEY_MODULE, question_context.get(DashboardDetailBundleConstants.KEY_MODULE));
        object_ref.put(DashboardDetailBundleConstants.KEY_OBJECT_TYPE, object_type);
        object_ref.put(DashboardDetailBundleConstants.KEY_TEST_ID, question_context.get(DashboardDetailBundleConstants.KEY_TEST_ID));
        object_ref.put(DashboardDetailBundleConstants.KEY_PASSAGE_ID, question_context.get(DashboardDetailBundleConstants.KEY_PASSAGE_ID));
        object_ref.put(DashboardDetailBundleConstants.KEY_QUESTION_ID, question_context.get(DashboardDetailBundleConstants.KEY_QUESTION_ID));
        object_ref.put(DashboardDetailBundleConstants.KEY_RECORD_ID, question_context.get(DashboardDetailBundleConstants.KEY_RECORD_ID));
        object_ref.put(DashboardDetailBundleConstants.KEY_QUESTION_NUMBER, question_context.get(DashboardDetailBundleConstants.KEY_QUESTION_NUMBER));
        object_ref.put(DashboardDetailBundleConstants.KEY_SESSION_ID, question_context.get(DashboardDetailBundleConstants.KEY_SESSION_ID));

        Map<String, Object> ext = new LinkedHashMap<>();
        ext.put("bundle_row_count", rows == null ? 0 : rows.size());
        ext.put("mapped_from_structured_query", true);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put(DashboardDetailBundleConstants.KEY_MODULE, question_context.get(DashboardDetailBundleConstants.KEY_MODULE));
        result.put(DashboardDetailBundleConstants.KEY_ASK_SCENE, ask_scene);
        result.put(DashboardDetailBundleConstants.KEY_OBJECT_REF, object_ref);
        result.put(DashboardDetailBundleConstants.KEY_QUESTION_CONTEXT, question_context);
        result.put(DashboardDetailBundleConstants.KEY_EXT, ext);
        return result;
    }

    public Map<String, Object> map_rows_to_question_context(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return Map.of();
        }

        Map<String, Object> first_row = rows.get(0);
        Map<String, Object> context = new LinkedHashMap<>();

        context.put(DashboardDetailBundleConstants.KEY_MODULE, get_string(first_row, DashboardDetailBundleConstants.KEY_MODULE));
        context.put(DashboardDetailBundleConstants.KEY_RECORD_ID, get_long(first_row, DashboardDetailBundleConstants.KEY_RECORD_ID));
        context.put(DashboardDetailBundleConstants.KEY_TEST_ID, get_long(first_row, DashboardDetailBundleConstants.KEY_TEST_ID));
        context.put(DashboardDetailBundleConstants.KEY_TEST_TITLE, get_string(first_row, DashboardDetailBundleConstants.KEY_TEST_TITLE));
        context.put(DashboardDetailBundleConstants.KEY_PASSAGE_ID, get_long(first_row, DashboardDetailBundleConstants.KEY_PASSAGE_ID));
        context.put(DashboardDetailBundleConstants.KEY_PASSAGE_TITLE, get_string(first_row, DashboardDetailBundleConstants.KEY_PASSAGE_TITLE));
        context.put(DashboardDetailBundleConstants.KEY_ARTICLE_TITLE, first_non_blank(
                get_string(first_row, DashboardDetailBundleConstants.KEY_ARTICLE_TITLE),
                get_string(first_row, DashboardDetailBundleConstants.KEY_PASSAGE_TITLE),
                get_string(first_row, DashboardDetailBundleConstants.KEY_TEST_TITLE),
                get_string(first_row, DashboardDetailBundleConstants.KEY_QUESTION_TEXT)
        ));
        context.put(DashboardDetailBundleConstants.KEY_ARTICLE_CONTENT, get_string(first_row, DashboardDetailBundleConstants.KEY_ARTICLE_CONTENT));
        context.put(DashboardDetailBundleConstants.KEY_QUESTION_ID, get_long(first_row, DashboardDetailBundleConstants.KEY_QUESTION_ID));
        context.put(DashboardDetailBundleConstants.KEY_QUESTION_NUMBER, get_integer(first_row, DashboardDetailBundleConstants.KEY_QUESTION_NUMBER));
        context.put(DashboardDetailBundleConstants.KEY_QUESTION_TEXT, get_string(first_row, DashboardDetailBundleConstants.KEY_QUESTION_TEXT));
        context.put(DashboardDetailBundleConstants.KEY_QUESTION_TYPE, get_string(first_row, DashboardDetailBundleConstants.KEY_QUESTION_TYPE));
        context.put(DashboardDetailBundleConstants.KEY_ANSWER_MODE, get_string(first_row, DashboardDetailBundleConstants.KEY_ANSWER_MODE));

        context.put("options", parse_string_list(first_row.get(DashboardDetailBundleConstants.KEY_OPTIONS_JSON)));
        context.put("accepted_answers", parse_string_list(first_row.get(DashboardDetailBundleConstants.KEY_ACCEPTED_ANSWERS_JSON)));

        context.put(DashboardDetailBundleConstants.KEY_CORRECT_ANSWER, get_string(first_row, DashboardDetailBundleConstants.KEY_CORRECT_ANSWER));
        context.put(DashboardDetailBundleConstants.KEY_EXPLANATION, get_string(first_row, DashboardDetailBundleConstants.KEY_EXPLANATION));
        context.put(DashboardDetailBundleConstants.KEY_CUE_CARD, get_string(first_row, DashboardDetailBundleConstants.KEY_CUE_CARD));
        context.put(DashboardDetailBundleConstants.KEY_IMAGE_URL, get_string(first_row, DashboardDetailBundleConstants.KEY_IMAGE_URL));
        context.put(DashboardDetailBundleConstants.KEY_TASK_TYPE, get_string(first_row, DashboardDetailBundleConstants.KEY_TASK_TYPE));
        context.put(DashboardDetailBundleConstants.KEY_USER_ANSWER, get_string(first_row, DashboardDetailBundleConstants.KEY_USER_ANSWER));
        context.put(DashboardDetailBundleConstants.KEY_USER_ESSAY, get_string(first_row, DashboardDetailBundleConstants.KEY_USER_ESSAY));
        context.put(DashboardDetailBundleConstants.KEY_USER_TRANSCRIPT, get_string(first_row, DashboardDetailBundleConstants.KEY_USER_TRANSCRIPT));
        context.put(DashboardDetailBundleConstants.KEY_TRANSCRIPT_TEXT, get_string(first_row, DashboardDetailBundleConstants.KEY_TRANSCRIPT_TEXT));
        context.put(DashboardDetailBundleConstants.KEY_AUDIO_URL, get_string(first_row, DashboardDetailBundleConstants.KEY_AUDIO_URL));
        context.put(DashboardDetailBundleConstants.KEY_AUDIO_OBJECT_KEY, get_string(first_row, DashboardDetailBundleConstants.KEY_AUDIO_OBJECT_KEY));
        context.put(DashboardDetailBundleConstants.KEY_USER_FEEDBACK, get_string(first_row, DashboardDetailBundleConstants.KEY_USER_FEEDBACK));
        context.put(DashboardDetailBundleConstants.KEY_AI_FEEDBACK, get_string(first_row, DashboardDetailBundleConstants.KEY_AI_FEEDBACK));
        context.put(DashboardDetailBundleConstants.KEY_SCORE, get_decimal(first_row, DashboardDetailBundleConstants.KEY_SCORE));
        context.put(DashboardDetailBundleConstants.KEY_TOTAL_SCORE, get_decimal(first_row, DashboardDetailBundleConstants.KEY_TOTAL_SCORE));
        context.put(DashboardDetailBundleConstants.KEY_AI_SCORE, get_decimal(first_row, DashboardDetailBundleConstants.KEY_AI_SCORE));
        context.put(DashboardDetailBundleConstants.KEY_CORRECT, get_boolean(first_row, DashboardDetailBundleConstants.KEY_CORRECT));
        context.put(DashboardDetailBundleConstants.KEY_STATUS, get_string(first_row, DashboardDetailBundleConstants.KEY_STATUS));
        context.put(DashboardDetailBundleConstants.KEY_CREATED_TIME, first_row.get(DashboardDetailBundleConstants.KEY_CREATED_TIME));
        context.put(DashboardDetailBundleConstants.KEY_SESSION_ID, get_string(first_row, DashboardDetailBundleConstants.KEY_SESSION_ID));

        Map<String, Object> ext = new LinkedHashMap<>();
        ext.put("row_count", rows.size());
        ext.put("expected_columns", new ArrayList<>(DashboardDetailBundleConstants.DETAIL_BUNDLE_EXPECTED_COLUMNS));
        ext.put("source", "structured_query_result");
        context.put(DashboardDetailBundleConstants.KEY_EXT, ext);

        return remove_nulls(context);
    }

    private List<String> parse_string_list(Object raw_value) {
        if (raw_value == null) {
            return List.of();
        }
        if (raw_value instanceof List<?> raw_list) {
            List<String> result = new ArrayList<>();
            for (Object item : raw_list) {
                if (item != null && !String.valueOf(item).isBlank()) {
                    result.add(String.valueOf(item).trim());
                }
            }
            return result;
        }
        String text = String.valueOf(raw_value).trim();
        if (text.isEmpty()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(text, new TypeReference<List<String>>() {});
        } catch (Exception ignored) {
            return List.of(text);
        }
    }

    private String get_string(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private Long get_long(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private Integer get_integer(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private Object get_decimal(Map<String, Object> row, String key) {
        return row.get(key);
    }

    private Boolean get_boolean(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        String text = String.valueOf(value).trim().toLowerCase();
        if ("1".equals(text) || "true".equals(text)) {
            return Boolean.TRUE;
        }
        if ("0".equals(text) || "false".equals(text)) {
            return Boolean.FALSE;
        }
        return null;
    }

    private String first_non_blank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private Map<String, Object> remove_nulls(Map<String, Object> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            Object value = entry.getValue();
            if (value == null) {
                continue;
            }
            if (value instanceof String text && text.isBlank()) {
                continue;
            }
            result.put(entry.getKey(), value);
        }
        return result;
    }
}
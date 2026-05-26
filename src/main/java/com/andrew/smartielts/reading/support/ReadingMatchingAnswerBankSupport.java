package com.andrew.smartielts.reading.support;

import com.andrew.smartielts.reading.constant.ReadingQuestionConstants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ReadingMatchingAnswerBankSupport {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String LABEL_KEY = "label";
    private static final String TEXT_KEY = "text";

    private ReadingMatchingAnswerBankSupport() {
    }

    public static String normalizeOptionsJson(String questionType, String optionsJson) {
        String safeOptionsJson = trimToNull(optionsJson);
        if (safeOptionsJson == null) {
            return null;
        }
        if (!ReadingQuestionConstants.QUESTION_TYPE_MATCHING.equals(ReadingQuestionConstants.normalize_question_type(questionType))) {
            return safeOptionsJson;
        }

        try {
            JsonNode root = OBJECT_MAPPER.readTree(safeOptionsJson);
            if (!root.isArray()) {
                return safeOptionsJson;
            }

            List<Map<String, String>> options = new ArrayList<>();
            int labelIndex = 0;
            for (JsonNode item : root) {
                String text = extractOptionText(item);
                if (text == null) {
                    continue;
                }

                Map<String, String> option = new LinkedHashMap<>();
                option.put(LABEL_KEY, toLabel(labelIndex));
                option.put(TEXT_KEY, text);
                options.add(option);
                labelIndex++;
            }
            return OBJECT_MAPPER.writeValueAsString(options);
        } catch (Exception ignored) {
            return safeOptionsJson;
        }
    }

    public static String normalizeCorrectAnswer(String questionType, String correctAnswer) {
        String safeCorrectAnswer = trimToNull(correctAnswer);
        if (safeCorrectAnswer == null) {
            return null;
        }
        if (!ReadingQuestionConstants.QUESTION_TYPE_MATCHING.equals(ReadingQuestionConstants.normalize_question_type(questionType))) {
            return safeCorrectAnswer;
        }
        return safeCorrectAnswer.toUpperCase();
    }

    private static String extractOptionText(JsonNode item) {
        if (item == null || item.isNull()) {
            return null;
        }
        if (item.isTextual() || item.isNumber() || item.isBoolean()) {
            return trimToNull(item.asText());
        }
        if (!item.isObject()) {
            return null;
        }
        String text = firstNonBlank(
                textValue(item, TEXT_KEY),
                textValue(item, "optionText"),
                textValue(item, "option_text"),
                textValue(item, "value"),
                textValue(item, "name")
        );
        if (text != null) {
            return text;
        }
        return firstNonBlank(textValue(item, LABEL_KEY), textValue(item, "key"));
    }

    private static String textValue(JsonNode node, String fieldName) {
        JsonNode value = node == null ? null : node.get(fieldName);
        return value == null || value.isNull() ? null : trimToNull(value.asText());
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            String safeValue = trimToNull(value);
            if (safeValue != null) {
                return safeValue;
            }
        }
        return null;
    }

    private static String toLabel(int index) {
        int safeIndex = Math.max(index, 0);
        StringBuilder label = new StringBuilder();
        do {
            label.insert(0, (char) ('A' + safeIndex % 26));
            safeIndex = safeIndex / 26 - 1;
        } while (safeIndex >= 0);
        return label.toString();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

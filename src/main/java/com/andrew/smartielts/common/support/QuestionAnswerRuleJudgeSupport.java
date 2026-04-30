package com.andrew.smartielts.common.support;

import com.andrew.smartielts.common.domain.pojo.QuestionAnswerRule;
import com.andrew.smartielts.reading.constant.ReadingQuestionConstants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class QuestionAnswerRuleJudgeSupport {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public GradeResult grade(List<String> raw_answers,
                             String answer_mode,
                             String correct_answer,
                             String accepted_answers_json,
                             Integer case_insensitive,
                             Integer ignore_whitespace,
                             Integer ignore_punctuation,
                             List<QuestionAnswerRule> rules,
                             Integer question_score) {

        List<String> safe_raw_answers = normalize_raw_answers(raw_answers);
        JudgeOptions options = JudgeOptions.of(
                enabled(case_insensitive),
                enabled(ignore_whitespace),
                enabled(ignore_punctuation)
        );

        String normalized_answer_mode = ReadingQuestionConstants.normalize_answer_mode(answer_mode);

        boolean correct;
        if (rules != null && !rules.isEmpty()) {
            correct = judge_by_rules(safe_raw_answers, rules, options);
        } else if (ReadingQuestionConstants.is_multi_answer_mode(normalized_answer_mode)) {
            correct = match_multi(correct_answer, accepted_answers_json, safe_raw_answers, options);
        } else {
            correct = match_single_or_text(correct_answer, accepted_answers_json, first_answer(safe_raw_answers), options);
        }

        int earned_score = correct ? safe_score(question_score) : 0;
        String stored_user_answer = build_stored_user_answer(safe_raw_answers);
        String normalized_user_answer = build_stored_normalized_answer(safe_raw_answers, options, normalized_answer_mode);
        String raw_answers_json = to_json_array(safe_raw_answers);
        String display_correct_answer = build_display_correct_answer(correct_answer, accepted_answers_json, rules);

        return new GradeResult(
                correct,
                earned_score,
                stored_user_answer,
                normalized_user_answer,
                raw_answers_json,
                display_correct_answer
        );
    }

    private boolean judge_by_rules(List<String> raw_answers,
                                   List<QuestionAnswerRule> rules,
                                   JudgeOptions options) {
        if (rules == null || rules.isEmpty()) {
            return false;
        }

        Map<Integer, List<QuestionAnswerRule>> blank_map = rules.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        rule -> rule.getBlankNo() == null ? 1 : rule.getBlankNo(),
                        TreeMap::new,
                        Collectors.toList()
                ));

        if (blank_map.isEmpty()) {
            return false;
        }

        for (Map.Entry<Integer, List<QuestionAnswerRule>> entry : blank_map.entrySet()) {
            Integer blank_no = entry.getKey();
            String user_value = get_user_value_by_blank(blank_no == null ? 1 : blank_no, raw_answers);
            String normalized_user = normalize(user_value, options);

            if (normalized_user.isBlank()) {
                return false;
            }

            boolean matched = entry.getValue().stream()
                    .filter(Objects::nonNull)
                    .map(rule -> first_non_blank(
                            trim_to_null(rule.getNormalizedAnswer()),
                            trim_to_null(rule.getAnswerText())
                    ))
                    .filter(Objects::nonNull)
                    .map(candidate -> normalize(candidate, options))
                    .filter(candidate -> !candidate.isBlank())
                    .anyMatch(normalized_user::equals);

            if (!matched) {
                return false;
            }
        }

        return true;
    }

    private boolean match_single_or_text(String correct_answer,
                                         String accepted_answers_json,
                                         String user_answer,
                                         JudgeOptions options) {
        String normalized_user = normalize(user_answer, options);
        if (normalized_user.isBlank()) {
            return false;
        }

        List<String> candidates = new ArrayList<>();
        String correct_value = trim_to_null(correct_answer);
        if (correct_value != null) {
            candidates.add(correct_value);
        }
        candidates.addAll(parse_accepted_answer_list(accepted_answers_json));

        return candidates.stream()
                .map(candidate -> normalize(candidate, options))
                .filter(candidate -> !candidate.isBlank())
                .anyMatch(normalized_user::equals);
    }

    private boolean match_multi(String correct_answer,
                                String accepted_answers_json,
                                List<String> user_answers,
                                JudgeOptions options) {
        List<String> normalized_user = normalize_list(user_answers, options);
        if (normalized_user.isEmpty()) {
            return false;
        }

        List<List<String>> candidate_groups = parse_accepted_answer_groups(accepted_answers_json);
        if (candidate_groups.isEmpty()) {
            candidate_groups = new ArrayList<>();
            candidate_groups.add(parse_csv(correct_answer));
        }

        for (List<String> group : candidate_groups) {
            List<String> normalized_group = normalize_list(group, options);
            if (normalized_user.equals(normalized_group)) {
                return true;
            }
        }

        return false;
    }

    private String build_stored_user_answer(List<String> raw_answers) {
        if (raw_answers == null || raw_answers.isEmpty()) {
            return null;
        }
        return String.join(", ", raw_answers);
    }

    private String build_stored_normalized_answer(List<String> raw_answers,
                                                  JudgeOptions options,
                                                  String answer_mode) {
        if (raw_answers == null || raw_answers.isEmpty()) {
            return null;
        }

        if (ReadingQuestionConstants.is_multi_answer_mode(answer_mode)) {
            List<String> normalized_list = normalize_list(raw_answers, options);
            return normalized_list.isEmpty() ? null : String.join(",", normalized_list);
        }

        String first_answer = first_answer(raw_answers);
        String normalized = normalize(first_answer, options);
        return normalized.isBlank() ? null : normalized;
    }

    private String build_display_correct_answer(String correct_answer,
                                                String accepted_answers_json,
                                                List<QuestionAnswerRule> rules) {
        if (rules != null && !rules.isEmpty()) {
            Map<Integer, List<QuestionAnswerRule>> blank_map = rules.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.groupingBy(
                            rule -> rule.getBlankNo() == null ? 1 : rule.getBlankNo(),
                            TreeMap::new,
                            Collectors.toList()
                    ));

            List<String> parts = new ArrayList<>();
            for (List<QuestionAnswerRule> rule_list : blank_map.values()) {
                if (rule_list == null || rule_list.isEmpty()) {
                    continue;
                }

                QuestionAnswerRule primary = rule_list.stream()
                        .filter(Objects::nonNull)
                        .filter(rule -> rule.getIsPrimary() != null && rule.getIsPrimary() == 1)
                        .findFirst()
                        .orElse(rule_list.get(0));

                String text = first_non_blank(
                        trim_to_null(primary.getAnswerText()),
                        trim_to_null(primary.getNormalizedAnswer())
                );
                if (text != null) {
                    parts.add(text);
                }
            }

            if (!parts.isEmpty()) {
                return String.join(", ", parts);
            }
        }

        String correct_value = trim_to_null(correct_answer);
        if (correct_value != null) {
            return correct_value;
        }

        List<String> accepted_list = parse_accepted_answer_list(accepted_answers_json);
        return accepted_list.isEmpty() ? null : String.join(", ", accepted_list);
    }

    private List<String> normalize_raw_answers(List<String> raw_answers) {
        List<String> result = new ArrayList<>();
        if (raw_answers == null || raw_answers.isEmpty()) {
            return result;
        }

        for (String item : raw_answers) {
            String value = trim_to_null(item);
            if (value != null) {
                result.add(value);
            }
        }
        return result;
    }

    private List<String> normalize_list(List<String> values, JudgeOptions options) {
        List<String> result = new ArrayList<>();
        if (values == null || values.isEmpty()) {
            return result;
        }

        for (String value : values) {
            String normalized = normalize(value, options);
            if (!normalized.isBlank()) {
                result.add(normalized);
            }
        }
        return result;
    }

    private String normalize(String value, JudgeOptions options) {
        String result = trim_to_null(value);
        if (result == null) {
            return "";
        }

        if (options.ignoreWhitespace) {
            result = result.replaceAll("\\s+", "");
        } else {
            result = result.replaceAll("\\s+", " ").trim();
        }

        if (options.ignorePunctuation) {
            result = result.replaceAll("[\\p{Punct}]", "");
        }

        if (options.caseInsensitive) {
            result = result.toLowerCase();
        }

        return result.trim();
    }

    private List<String> parse_accepted_answer_list(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }

        try {
            JsonNode root = objectMapper.readTree(json);
            List<String> result = new ArrayList<>();

            if (root.isArray()) {
                for (JsonNode node : root) {
                    if (node == null || node.isNull()) {
                        continue;
                    }

                    if (node.isArray()) {
                        for (JsonNode child : node) {
                            if (child == null || child.isNull()) {
                                continue;
                            }
                            String value = trim_to_null(child.asText(null));
                            if (value != null) {
                                result.add(value);
                            }
                        }
                    } else {
                        String value = trim_to_null(node.asText(null));
                        if (value != null) {
                            result.add(value);
                        }
                    }
                }
            }

            return distinct_preserve_order(result);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private List<List<String>> parse_accepted_answer_groups(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }

        try {
            JsonNode root = objectMapper.readTree(json);
            List<List<String>> result = new ArrayList<>();

            if (!root.isArray()) {
                return result;
            }

            boolean nested_array_found = false;
            for (JsonNode node : root) {
                if (node != null && node.isArray()) {
                    nested_array_found = true;
                    break;
                }
            }

            if (!nested_array_found) {
                List<String> single_group = new ArrayList<>();
                for (JsonNode node : root) {
                    if (node == null || node.isNull()) {
                        continue;
                    }
                    String value = trim_to_null(node.asText(null));
                    if (value != null) {
                        single_group.add(value);
                    }
                }
                if (!single_group.isEmpty()) {
                    result.add(single_group);
                }
                return result;
            }

            for (JsonNode node : root) {
                if (node == null || node.isNull() || !node.isArray()) {
                    continue;
                }

                List<String> group = new ArrayList<>();
                for (JsonNode child : node) {
                    if (child == null || child.isNull()) {
                        continue;
                    }
                    String value = trim_to_null(child.asText(null));
                    if (value != null) {
                        group.add(value);
                    }
                }

                if (!group.isEmpty()) {
                    result.add(group);
                }
            }

            return result;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private List<String> parse_csv(String value) {
        String text = trim_to_null(value);
        if (text == null) {
            return new ArrayList<>();
        }

        List<String> result = new ArrayList<>();
        for (String item : text.split(",")) {
            String trimmed = trim_to_null(item);
            if (trimmed != null) {
                result.add(trimmed);
            }
        }
        return result;
    }

    private String get_user_value_by_blank(int blank_no, List<String> raw_answers) {
        if (raw_answers == null || raw_answers.isEmpty()) {
            return null;
        }
        if (blank_no < 1 || blank_no > raw_answers.size()) {
            return null;
        }
        return raw_answers.get(blank_no - 1);
    }

    private String first_answer(List<String> raw_answers) {
        if (raw_answers == null || raw_answers.isEmpty()) {
            return null;
        }
        return raw_answers.get(0);
    }

    private List<String> distinct_preserve_order(List<String> values) {
        if (values == null || values.isEmpty()) {
            return new ArrayList<>();
        }
        Set<String> seen = new LinkedHashSet<>();
        for (String value : values) {
            String item = trim_to_null(value);
            if (item != null) {
                seen.add(item);
            }
        }
        return new ArrayList<>(seen);
    }

    private String to_json_array(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values == null ? Collections.emptyList() : values);
        } catch (Exception e) {
            return "[]";
        }
    }

    private int safe_score(Integer score) {
        return score == null || score < 0 ? 0 : score;
    }

    private boolean enabled(Integer flag) {
        return flag == null || flag == 1;
    }

    private String first_non_blank(String first, String second) {
        String first_value = trim_to_null(first);
        return first_value != null ? first_value : trim_to_null(second);
    }

    private String trim_to_null(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static class JudgeOptions {
        private final boolean caseInsensitive;
        private final boolean ignoreWhitespace;
        private final boolean ignorePunctuation;

        private JudgeOptions(boolean caseInsensitive, boolean ignoreWhitespace, boolean ignorePunctuation) {
            this.caseInsensitive = caseInsensitive;
            this.ignoreWhitespace = ignoreWhitespace;
            this.ignorePunctuation = ignorePunctuation;
        }

        private static JudgeOptions of(boolean caseInsensitive, boolean ignoreWhitespace, boolean ignorePunctuation) {
            return new JudgeOptions(caseInsensitive, ignoreWhitespace, ignorePunctuation);
        }
    }

    public static class GradeResult {
        private final boolean correct;
        private final int earnedScore;
        private final String storedUserAnswer;
        private final String normalizedUserAnswer;
        private final String rawAnswersJson;
        private final String displayCorrectAnswer;

        public GradeResult(boolean correct,
                           int earnedScore,
                           String storedUserAnswer,
                           String normalizedUserAnswer,
                           String rawAnswersJson,
                           String displayCorrectAnswer) {
            this.correct = correct;
            this.earnedScore = earnedScore;
            this.storedUserAnswer = storedUserAnswer;
            this.normalizedUserAnswer = normalizedUserAnswer;
            this.rawAnswersJson = rawAnswersJson;
            this.displayCorrectAnswer = displayCorrectAnswer;
        }

        public boolean isCorrect() {
            return correct;
        }

        public int getEarnedScore() {
            return earnedScore;
        }

        public String getStoredUserAnswer() {
            return storedUserAnswer;
        }

        public String getNormalizedUserAnswer() {
            return normalizedUserAnswer;
        }

        public String getRawAnswersJson() {
            return rawAnswersJson;
        }

        public String getDisplayCorrectAnswer() {
            return displayCorrectAnswer;
        }
    }
}
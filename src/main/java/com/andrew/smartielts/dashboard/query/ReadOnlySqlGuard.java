package com.andrew.smartielts.dashboard.query;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ReadOnlySqlGuard {

    private static final Pattern FROM_OR_JOIN_PATTERN =
            Pattern.compile("\\b(?:from|join)\\s+([a-zA-Z_][a-zA-Z0-9_]*)", Pattern.CASE_INSENSITIVE);

    private static final Set<String> ALLOWED_TABLES = Set.of(
            "sys_user",
            "listening_test",
            "listening_question",
            "listening_record",
            "listening_answer_record",
            "reading_test",
            "reading_passage",
            "reading_question",
            "reading_record",
            "reading_answer_record",
            "writing_question",
            "writing_record",
            "writing_record_attachment",
            "speaking_question",
            "speaking_record",
            "speaking_session"
    );

    private static final Set<String> FORBIDDEN_KEYWORDS = Set.of(
            "insert", "update", "delete", "drop", "alter", "truncate", "create",
            "replace", "merge", "grant", "revoke", "commit", "rollback",
            "call", "execute", "exec"
    );

    public void validate(String sql) {
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("SQL must not be blank");
        }

        String normalized = normalize(sql);
        validateSingleStatement(normalized);
        validateSelectOnly(normalized);
        validateNoComments(normalized);
        validateNoSelectStar(normalized);
        validateForbiddenKeywords(normalized);
        validateForbiddenSchemas(normalized);
        validateTables(normalized);
    }

    private String normalize(String sql) {
        return sql.trim()
                .replace('\n', ' ')
                .replace('\r', ' ')
                .replace('\t', ' ')
                .replaceAll("\\s+", " ");
    }

    private void validateSingleStatement(String sql) {
        String trimmed = sql.trim();
        if (trimmed.endsWith(";")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1).trim();
        }
        if (trimmed.contains(";")) {
            throw new IllegalArgumentException("Multiple SQL statements are not allowed");
        }
    }

    private void validateSelectOnly(String sql) {
        String lower = sql.toLowerCase(Locale.ROOT);
        if (!lower.startsWith("select ")) {
            throw new IllegalArgumentException("Only SELECT statements are allowed");
        }
    }

    private void validateNoComments(String sql) {
        if (sql.contains("--") || sql.contains("/*") || sql.contains("*/") || sql.contains("#")) {
            throw new IllegalArgumentException("SQL comments are not allowed");
        }
    }

    private void validateNoSelectStar(String sql) {
        String lower = sql.toLowerCase(Locale.ROOT);
        if (lower.matches(".*select\\s+\\*.*")) {
            throw new IllegalArgumentException("SELECT * is not allowed");
        }
    }

    private void validateForbiddenKeywords(String sql) {
        String lower = sql.toLowerCase(Locale.ROOT);
        for (String keyword : FORBIDDEN_KEYWORDS) {
            if (lower.matches(".*\\b" + Pattern.quote(keyword) + "\\b.*")) {
                throw new IllegalArgumentException("Forbidden SQL keyword: " + keyword);
            }
        }
    }

    private void validateForbiddenSchemas(String sql) {
        String lower = sql.toLowerCase(Locale.ROOT);
        if (lower.contains("information_schema.")
                || lower.contains("mysql.")
                || lower.contains("performance_schema.")
                || lower.contains("sys.")) {
            throw new IllegalArgumentException("Forbidden schema access detected");
        }
    }

    private void validateTables(String sql) {
        Matcher matcher = FROM_OR_JOIN_PATTERN.matcher(sql);
        while (matcher.find()) {
            String tableName = matcher.group(1);
            if (!ALLOWED_TABLES.contains(tableName)) {
                throw new IllegalArgumentException("Table not allowed: " + tableName);
            }
        }
    }
}
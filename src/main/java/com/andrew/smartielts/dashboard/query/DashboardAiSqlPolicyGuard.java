package com.andrew.smartielts.dashboard.query;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

@Component
public class DashboardAiSqlPolicyGuard {

    private static final Set<String> ALLOWED_TABLE_TOKENS = Set.of(
            "sys_user",
            "listening_test", "listening_question", "listening_record", "listening_answer_record",
            "reading_test", "reading_passage", "reading_question", "reading_record", "reading_answer_record",
            "writing_question", "writing_record",
            "speaking_question", "speaking_record", "speaking_session"
    );

    public void validate(String sql, SecureDashboardQueryRequest request) {
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("SQL cannot be blank");
        }
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        String normalized = sql.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);

        validateSqlShape(normalized);

        if ("USER".equalsIgnoreCase(request.getRole())) {
            validateUserSql(normalized);
        }
        if (usesUserRecordTable(normalized) && request.getTargetUserId() == null) {
            throw new AccessDeniedException("Target user is required when querying user records");
        }
    }

    private void validateSqlShape(String sql) {
        if (!sql.startsWith("select") && !sql.startsWith("with")) {
            throw new IllegalArgumentException("AI dashboard SQL must start with SELECT or WITH");
        }
        if (!sql.startsWith("with") && !sql.contains(" from ")) {
            throw new IllegalArgumentException("AI dashboard SQL must contain FROM");
        }
        if (!containsAllowedTable(sql)) {
            throw new IllegalArgumentException("AI dashboard SQL must reference an allowed dashboard table");
        }
        if (sql.matches("(?is)^select\\s+['\"]?\\w+['\"]?\\s+as\\s+module\\s*,\\s*limit\\s+[:?\\w]+\\s*$")) {
            throw new IllegalArgumentException("AI dashboard SQL is incomplete");
        }
    }

    private boolean containsAllowedTable(String sql) {
        return ALLOWED_TABLE_TOKENS.stream().anyMatch(sql::contains);
    }

    private void validateUserSql(String sql) {
        if (usesUserRecordTable(sql) && !containsTargetUserBinding(sql)) {
            throw new AccessDeniedException("User-scoped SQL must bind targetUserId");
        }
    }

    private boolean containsTargetUserBinding(String sql) {
        return sql.contains(":target_userid")
                || sql.contains("target_userid");
    }

    private boolean usesUserRecordTable(String sql) {
        return sql.contains("listening_record")
                || sql.contains("reading_record")
                || sql.contains("writing_record")
                || sql.contains("speaking_record");
    }
}
package com.andrew.smartielts.dashboard.query;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class DashboardAiSqlPolicyGuard {

    public void validate(String sql, SecureDashboardQueryRequest request) {
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("SQL cannot be blank");
        }
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        String normalized = sql.trim().replaceAll("\\s+", " ").toLowerCase();

        if ("USER".equalsIgnoreCase(request.getRole())) {
            validateUserSql(normalized);
        }

        if (usesUserRecordTable(normalized) && request.getTargetUserId() == null) {
            throw new AccessDeniedException("Target user is required when querying user records");
        }
    }

    private void validateUserSql(String sql) {
        if (usesUserRecordTable(sql) && !sql.contains(":targetuserid")) {
            throw new AccessDeniedException("User-scoped SQL must bind :targetUserId");
        }
    }

    private boolean usesUserRecordTable(String sql) {
        return sql.contains("listening_record")
                || sql.contains("reading_record")
                || sql.contains("writing_record")
                || sql.contains("speaking_record");
    }
}
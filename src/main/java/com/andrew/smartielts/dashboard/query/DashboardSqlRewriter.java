package com.andrew.smartielts.dashboard.query;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class DashboardSqlRewriter {

    private static final Pattern LIMIT_PATTERN = Pattern.compile("(?i)\\blimit\\b");
    private static final Pattern SEMICOLON_END_PATTERN = Pattern.compile(";\\s*$");

    public String rewrite(String sql, SecureDashboardQueryRequest request) {
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("sql cannot be blank");
        }

        String normalized = SEMICOLON_END_PATTERN.matcher(sql.trim()).replaceFirst("");

        if (!LIMIT_PATTERN.matcher(normalized).find()) {
            normalized = normalized + " LIMIT :limit";
        }

        return normalized;
    }
}
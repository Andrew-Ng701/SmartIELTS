package com.andrew.smartielts.dashboard.query;

import org.springframework.stereotype.Component;

@Component
public class DashboardSqlRewriter {

    public String rewrite(String sql, SecureDashboardQueryRequest request) {
        String normalized = sql.trim();

        if (normalized.endsWith(";")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        if (!normalized.toLowerCase().contains(" limit ")) {
            normalized = normalized + " LIMIT :limit";
        }

        return normalized;
    }
}
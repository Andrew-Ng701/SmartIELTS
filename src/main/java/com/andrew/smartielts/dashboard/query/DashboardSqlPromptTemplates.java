package com.andrew.smartielts.dashboard.query;

public final class DashboardSqlPromptTemplates {

    private DashboardSqlPromptTemplates() {
    }

    public static final String DASH_SCOPE_SQL_GENERATION_USER_PROMPT_TEMPLATE = """
            Generate a SQL plan for the following dashboard request.

            role: %s
            operatorUserId: %s
            targetUserId: %s
            originalQuery: %s
            intentJson: %s
            contextJson: %s

            Return JSON only.
            """;

    public static final String DASH_SCOPE_SQL_REVIEW_USER_PROMPT_TEMPLATE = """
            Review the SQL result and answer the user.

            role: %s
            operatorUserId: %s
            targetUserId: %s
            originalQuery: %s
            intentJson: %s
            sqlPlanJson: %s
            rowsJson: %s

            Return JSON only.
            """;
}
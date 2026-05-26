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
            responseLanguage: %s
            originalQuery: %s
            intentJson: %s
            sqlPlanJson: %s
            rowsJson: %s
            userTargetScoresJson: %s

            Suggestion rules:
            - Suggestions are recommended follow-up questions/actions shown to the user after the answer.
            - Write suggestions from the user's point of view.
            - For English suggestions, use "my" or "I"; do not use "you" or "your".
            - Example: "View my 30-day trend to track my progress over time".
            - Follow responseLanguage exactly.
            - For USER answers, if userTargetScoresJson contains any non-null score, explicitly relate the answer to the user's IELTS targets.
            - If the answer focuses on one IELTS module, mention that module's target; otherwise mention the four-module target context briefly.
            - Do not invent missing target scores. If a target score is null or blank, do not state a numeric value for it.

            Return JSON only.
            """;
}

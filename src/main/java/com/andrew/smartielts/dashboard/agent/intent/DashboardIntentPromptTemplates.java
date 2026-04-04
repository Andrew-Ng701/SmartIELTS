package com.andrew.smartielts.dashboard.agent.intent;

public final class DashboardIntentPromptTemplates {

    private DashboardIntentPromptTemplates() {
    }

    public static final String DASHSCOPE_INTENT_USER_PROMPT_TEMPLATE = """
        Parse the following dashboard query into JSON.

        role: %s
        operatorUserId: %s
        contextTargetUserId: %s
        query: %s
        context: %s
        """;
}
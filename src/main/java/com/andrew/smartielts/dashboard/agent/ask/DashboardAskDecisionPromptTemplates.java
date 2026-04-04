package com.andrew.smartielts.dashboard.agent.ask;

public final class DashboardAskDecisionPromptTemplates {

    private DashboardAskDecisionPromptTemplates() {
    }

    public static final String USER_PROMPT_TEMPLATE = """
Decide whether the current ask request can be answered directly with currently available data.

role: %s
operatorUserId: %s
targetUserId: %s
responseLanguage: %s
askScene: %s
responseMode: %s
query: %s
objectRefJson: %s
preloadedPayloadJson: %s
clientContextJson: %s
contextJson: %s
learningContextJson: %s

Return JSON only.
""";
}
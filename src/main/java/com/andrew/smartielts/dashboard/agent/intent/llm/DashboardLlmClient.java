package com.andrew.smartielts.dashboard.agent.intent.llm;

import com.andrew.smartielts.dashboard.agent.intent.dto.DashboardIntentParseRequest;
import com.andrew.smartielts.dashboard.agent.intent.dto.DashboardIntentParseResult;

public interface DashboardLlmClient {
    DashboardIntentParseResult parseIntent(DashboardIntentParseRequest request);
}
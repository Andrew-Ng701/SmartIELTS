package com.andrew.smartielts.dashboard.query;

import com.andrew.smartielts.dashboard.agent.intent.dto.DashboardIntentParseResult;
import com.andrew.smartielts.dashboard.controller.dto.DashboardAssistantResponse;

import java.util.Map;

public interface DashboardStructuredAiQueryService {

    DashboardAssistantResponse execute(String role,
                                       Long operatorUserId,
                                       Long targetUserId,
                                       String originalQuery,
                                       DashboardIntentParseResult intent,
                                       Map<String, Object> context);
}
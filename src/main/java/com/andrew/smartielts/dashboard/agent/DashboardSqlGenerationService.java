package com.andrew.smartielts.dashboard.agent;

import com.andrew.smartielts.dashboard.agent.intent.dto.DashboardIntentParseResult;
import com.andrew.smartielts.dashboard.query.dto.DashboardSqlGenerationResult;

import java.util.List;
import java.util.Map;

public interface DashboardSqlGenerationService {
    DashboardSqlGenerationResult generate(String role,
                                          Long operatorUserId,
                                          Long targetUserId,
                                          String originalQuery,
                                          DashboardIntentParseResult intent,
                                          Map<String, Object> context);

    Map<String, Object> reviewAndAnswer(String role,
                                        Long operatorUserId,
                                        Long targetUserId,
                                        String originalQuery,
                                        DashboardIntentParseResult intent,
                                        DashboardSqlGenerationResult sqlPlan,
                                        List<Map<String, Object>> rows);
}
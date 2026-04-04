package com.andrew.smartielts.dashboard.query.dto;

import com.andrew.smartielts.dashboard.agent.intent.dto.DashboardIntentParseResult;
import lombok.Data;

import java.util.Map;

@Data
public class DashboardSqlGenerationRequest {
    private String role;
    private Long operatorUserId;
    private Long targetUserId;
    private String originalQuery;
    private DashboardIntentParseResult intent;
    private Map<String, Object> context;
}
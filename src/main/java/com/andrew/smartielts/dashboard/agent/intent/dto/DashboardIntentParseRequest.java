package com.andrew.smartielts.dashboard.agent.intent.dto;

import lombok.Data;

import java.util.Map;

@Data
public class DashboardIntentParseRequest {
    private String query;
    private String role;
    private Long operatorUserId;
    private Long contextTargetUserId;
    private Map<String, Object> context;
    private String responseLanguage;
}
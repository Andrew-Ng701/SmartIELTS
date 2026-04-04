package com.andrew.smartielts.dashboard.agent.answer.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class DashboardAnswerComposeRequest {
    private String role;
    private Long operatorUserId;
    private Long targetUserId;
    private String originalQuery;
    private String capability;
    private Map<String, Object> filters;
    private Object data;
    private String responseLanguage;
}
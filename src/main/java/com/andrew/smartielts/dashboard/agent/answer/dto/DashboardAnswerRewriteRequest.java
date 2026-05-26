package com.andrew.smartielts.dashboard.agent.answer.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardAnswerRewriteRequest {
    private String role;
    private String originalQuery;
    private String capability;
    private Map<String, Object> filters;
    private Map<String, Object> userTargetScores;
    private Object data;
    private String factualSummary;
    private List<String> suggestions;
    private String responseLanguage;
}

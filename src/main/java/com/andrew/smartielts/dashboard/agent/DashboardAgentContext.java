package com.andrew.smartielts.dashboard.agent;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class DashboardAgentContext {
    private String role;
    private Long operatorUserId;
    private Long targetUserId;
    private String originalQuery;
    private Map<String, Object> filters;
}
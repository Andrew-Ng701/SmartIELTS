package com.andrew.smartielts.dashboard.controller.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DashboardAskPreloadedPayload {

    private String snapshotId;
    private String snapshotTime;

    private Object overview;
    private Object progressSummary;

    private List<?> recentRecords;
    private List<?> moduleStats;
    private List<Map<String, Object>> recentQuestions;
    private List<Map<String, Object>> recentPassages;

    private Map<String, Object> aggregates;

    private Map<String, Object> learningContext;
    private Map<String, Object> questionContext;
    private List<String> availableScopes;
    private String preloadSource;
}
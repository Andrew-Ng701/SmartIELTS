package com.andrew.smartielts.dashboard.query.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DashboardSqlGenerationResult {
    private Boolean success;
    private String sql;
    private Map<String, Object> params;
    private List<String> expectedColumns;
    private String queryPurpose;
    private String reasoningSummary;
    private Double confidence;
    private List<String> suggestions;
}
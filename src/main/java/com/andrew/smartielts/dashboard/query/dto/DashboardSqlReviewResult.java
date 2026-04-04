package com.andrew.smartielts.dashboard.query.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DashboardSqlReviewResult {
    private String answer;
    private Object data;
    private List<String> suggestions;
    private Map<String, Object> meta;
}
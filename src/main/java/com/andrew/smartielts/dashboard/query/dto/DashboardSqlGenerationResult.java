package com.andrew.smartielts.dashboard.query.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DashboardSqlGenerationResult {

    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("sql")
    private String sql;

    @JsonProperty("params")
    private Map<String, Object> params;

    @JsonProperty("expected_columns")
    private List<String> expectedColumns;

    @JsonProperty("query_purpose")
    private String queryPurpose;

    @JsonProperty("reasoning_summary")
    private String reasoningSummary;

    @JsonProperty("confidence")
    private Double confidence;

    @JsonProperty("suggestions")
    private List<String> suggestions;
}
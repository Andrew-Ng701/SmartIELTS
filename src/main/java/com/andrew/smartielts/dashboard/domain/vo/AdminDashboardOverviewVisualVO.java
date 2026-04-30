package com.andrew.smartielts.dashboard.domain.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AdminDashboardOverviewVisualVO {

    private String snapshotId;
    private String snapshotTime;

    private Object overview;
    private List<?> moduleStats;
    private List<?> recentRecords;
    private Map<String, Object> aggregates;

    private Map<String, Object> moduleBarChart;
    private Map<String, Object> moduleDonutChart;
}
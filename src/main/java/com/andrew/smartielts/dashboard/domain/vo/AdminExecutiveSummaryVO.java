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
public class AdminExecutiveSummaryVO {

    private String snapshotId;
    private String snapshotTime;

    private String summaryType;
    private String summaryText;
    private List<String> summarySentences;
    private String queryUsed;

    private Map<String, Object> meta;
}
package com.andrew.smartielts.dashboard.domain.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserExecutiveSummaryVO {

    private String snapshotId;
    private String snapshotTime;

    private String summaryType;
    private String summaryText;
    private List<String> summarySentences;
    private String queryUsed;

    private Map<String, Object> meta;
}

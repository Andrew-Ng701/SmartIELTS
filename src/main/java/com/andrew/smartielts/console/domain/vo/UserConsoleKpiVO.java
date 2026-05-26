package com.andrew.smartielts.console.domain.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserConsoleKpiVO {

    private long totalActiveRecords;
    private long totalDeletedRecords;
    private long totalRecords;
    private BigDecimal overallAverageScore;
    private BigDecimal overallAverage;
    private long recentActivityCount;
    private BigDecimal targetAverageScore;
    private BigDecimal overallTargetGap;
    private long aiPendingCount;
    private long aiFailedCount;
}

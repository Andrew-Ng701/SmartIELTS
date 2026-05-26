package com.andrew.smartielts.console.domain.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AdminConsoleKpiVO {

    private long totalUsers;
    private long activeUsers;
    private long deletedUsers;
    private long totalActiveRecords;
    private long totalDeletedRecords;
    private long totalRecords;
    private long aiFailureCount;
}

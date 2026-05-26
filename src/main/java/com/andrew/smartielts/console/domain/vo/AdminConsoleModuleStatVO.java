package com.andrew.smartielts.console.domain.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AdminConsoleModuleStatVO {

    private String module;
    private long activeCount;
    private long deletedCount;
    private long totalCount;
    private long aiFailureCount;
}

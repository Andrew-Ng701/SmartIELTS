package com.andrew.smartielts.console.domain.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserConsoleModuleStatVO {

    private String module;
    private long activeCount;
    private long deletedCount;
    private long totalCount;
    private BigDecimal averageScore;
    private BigDecimal targetScore;
    private BigDecimal targetGap;
}

package com.andrew.smartielts.console.domain.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserConsoleProfileVO {

    private Long userId;
    private String email;
    private String username;
    private LocalDateTime lastLoginTime;
    private Integer consecutiveLoginDays;
    private BigDecimal listeningTargetScore;
    private BigDecimal readingTargetScore;
    private BigDecimal writingTargetScore;
    private BigDecimal speakingTargetScore;
}

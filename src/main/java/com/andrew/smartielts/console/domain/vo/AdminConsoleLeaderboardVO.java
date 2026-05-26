package com.andrew.smartielts.console.domain.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AdminConsoleLeaderboardVO {

    private Long userId;
    private String email;
    private String username;
    private long activeRecordCount;
    private long deletedRecordCount;
    private long totalRecordCount;
    private LocalDateTime lastActivityTime;
}

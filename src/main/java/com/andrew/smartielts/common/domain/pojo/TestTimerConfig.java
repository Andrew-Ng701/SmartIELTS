package com.andrew.smartielts.common.domain.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TestTimerConfig {
    private Long id;
    private Long testId;
    private String timerMode;
    private Integer totalSeconds;
    private Integer autoSubmit;
    private Integer allowPause;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
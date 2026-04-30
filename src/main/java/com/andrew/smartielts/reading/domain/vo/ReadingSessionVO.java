package com.andrew.smartielts.reading.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReadingSessionVO {
    private Long recordId;
    private Long testId;
    private String sessionId;
    private String recordStatus;
    private LocalDateTime startedTime;
    private LocalDateTime submittedTime;
    private Integer timeLimitSeconds;
    private Integer timeSpentSeconds;
    private Integer remainingSeconds;
    private Integer allowPause;
    private Integer autoSubmit;
}
package com.andrew.smartielts.reading.domain.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReadingRecord {
    private Long id;
    private Long userId;
    private Long testId;

    private String sessionId;
    private Integer totalScore;
    private LocalDateTime startedTime;
    private LocalDateTime submittedTime;
    private Integer timeLimitSeconds;
    private Integer timeSpentSeconds;
    private String recordStatus;

    private LocalDateTime createdTime;
    private Integer isDeleted;
}
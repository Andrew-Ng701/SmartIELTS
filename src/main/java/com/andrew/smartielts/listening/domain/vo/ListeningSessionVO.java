package com.andrew.smartielts.listening.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ListeningSessionVO {
    private Long recordId;
    private Long testId;
    private String sessionId;
    private String recordStatus;
    private LocalDateTime startedTime;
    private LocalDateTime submittedTime;
    private Integer prepSeconds;
    private Integer timeLimitSeconds;
    private Integer prepMinutes;
    private Integer totalMinutes;
    private Integer timeSpentSeconds;
    private Integer remainingSeconds;
    private Integer allowPause;
    private Integer autoSubmit;
    private Integer allowAudioSeek;
}

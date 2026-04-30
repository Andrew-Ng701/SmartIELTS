package com.andrew.smartielts.reading.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReadingSubmitDTO {
    private String sessionId;
    private LocalDateTime startedTime;
    private Integer timeSpentSeconds;
    private Integer autoSubmitted;
    private List<ReadingAnswerDTO> answers;
}
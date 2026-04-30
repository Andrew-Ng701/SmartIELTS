package com.andrew.smartielts.listening.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ListeningSubmitDTO {
    private String sessionId;
    private LocalDateTime startedTime;
    private Integer timeSpentSeconds;
    private Integer autoSubmitted;
    private List<ListeningAnswerDTO> answers;
}
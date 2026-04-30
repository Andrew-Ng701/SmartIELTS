package com.andrew.smartielts.listening.domain.pojo;

import lombok.Data;

@Data
public class ListeningAnswerRecord {
    private Long id;
    private Long recordId;
    private Long questionId;

    private Long partGroupId;
    private String userAnswer;
    private String normalizedAnswer;
    private String rawAnswersJson;

    private Integer isCorrect;
    private Integer score;
}
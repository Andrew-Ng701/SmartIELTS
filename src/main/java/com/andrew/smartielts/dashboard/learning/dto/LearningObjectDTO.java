package com.andrew.smartielts.dashboard.learning.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class LearningObjectDTO {

    private String module;
    private String objectType;

    private Long id;
    private Long testId;
    private Long passageId;
    private Long questionId;

    private String title;
    private String content;
    private String questionText;
    private String correctAnswer;
    private String explanation;

    private Integer questionNumber;
    private String questionType;
    private String answerMode;

    private List<String> options;
    private List<String> acceptedAnswers;

    private Map<String, Object> ext;
}
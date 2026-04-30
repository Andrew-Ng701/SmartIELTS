package com.andrew.smartielts.common.domain.pojo;

import lombok.Data;

@Data
public class QuestionAnswerRule {
    private Long id;
    private Long questionId;
    private Integer blankNo;
    private Integer answerGroupNo;
    private String answerText;
    private String normalizedAnswer;
    private Integer isPrimary;
    private Integer displayOrder;
}
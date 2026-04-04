package com.andrew.smartielts.reading.domain.vo;

import lombok.Data;

@Data
public class ReadingAnswerResultVO {
    private Long questionId;
    private String questionText;
    private String userAnswer;
    private String correctAnswer;
    private Integer isCorrect;
    private String questionType;
    private String answerMode;
    private String optionsJson;
    private Integer score;
}

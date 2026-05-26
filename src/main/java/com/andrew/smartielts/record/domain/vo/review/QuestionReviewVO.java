package com.andrew.smartielts.record.domain.vo.review;

import lombok.Data;

@Data
public class QuestionReviewVO {

    private Long questionId;

    private Integer questionNumber;

    private String questionType;

    private String answerMode;

    private String questionText;

    private String prompt;

    private String optionsJson;

    private String userAnswer;

    private String correctAnswer;

    private Integer isCorrect;

    private Integer score;
}

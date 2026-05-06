package com.andrew.smartielts.speaking.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SubmitAnswerVO {

    private Long recordId;

    private String sessionId;

    private Long questionId;

    private String audioUrl;

    private String answerStatus;

    private String status;

    private String aiStatus;

    private String aiProvider;

    private String aiModel;

    private String aiErrorMessage;

    private BigDecimal fluencyAndCoherence;

    private BigDecimal lexicalResource;

    private BigDecimal grammaticalRangeAndAccuracy;

    private BigDecimal pronunciation;

    private BigDecimal overallScore;

    private String relevanceComment;

    private String qualityComment;

    private String feedback;

    private String message;
}

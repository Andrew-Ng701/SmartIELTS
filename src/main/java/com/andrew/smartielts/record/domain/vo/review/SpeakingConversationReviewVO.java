package com.andrew.smartielts.record.domain.vo.review;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SpeakingConversationReviewVO {

    private Long recordId;

    private Long questionId;

    private String part;

    private String questionText;

    private String prompt;

    private String cueCard;

    private String audioUrl;

    private String transcript;

    private BigDecimal fluencyAndCoherence;

    private BigDecimal lexicalResource;

    private BigDecimal grammaticalRangeAndAccuracy;

    private BigDecimal pronunciation;

    private BigDecimal overallScore;

    private String feedback;

    private String relevanceComment;

    private String qualityComment;

    private String answerStatus;

    private String aiStatus;

    private String aiProvider;

    private String aiModel;

    private String aiErrorMessage;

    private Integer isDeleted;

    private LocalDateTime deletedTime;

    private LocalDateTime createdTime;
}

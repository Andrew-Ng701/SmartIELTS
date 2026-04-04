package com.andrew.smartielts.speaking.domain.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SpeakingRecord {

    private Long id;

    private Long userId;

    private String sessionId;

    private Long questionId;

    private String audioUrl;

    private String transcript;

    private BigDecimal fluencyAndCoherence;

    private BigDecimal lexicalResource;

    private BigDecimal grammaticalRangeAndAccuracy;

    private BigDecimal pronunciation;

    private BigDecimal overallScore;

    private String feedback;

    private String answerStatus;

    private Integer isDeleted;

    private LocalDateTime deletedTime;

    private String aiStatus;

    private String aiProvider;

    private String aiModel;

    private String aiErrorMessage;

    private String relevanceComment;

    private String qualityComment;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}

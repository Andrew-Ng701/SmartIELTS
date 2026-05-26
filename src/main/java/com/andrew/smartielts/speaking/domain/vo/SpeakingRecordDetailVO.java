package com.andrew.smartielts.speaking.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SpeakingRecordDetailVO {

    private Long recordId;

    private String sessionId;

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

    private Integer isDeleted;

    private LocalDateTime deletedTime;

    private String aiStatus;

    private String aiProvider;

    private String aiModel;

    private String aiErrorMessage;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;

    private List<SpeakingRecordVO> sessionRecords;
}

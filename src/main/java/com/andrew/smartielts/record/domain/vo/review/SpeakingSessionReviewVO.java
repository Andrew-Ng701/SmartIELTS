package com.andrew.smartielts.record.domain.vo.review;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SpeakingSessionReviewVO {

    private String sessionId;

    private String examStatus;

    private Integer totalQuestions;

    private Integer answeredCount;

    private Integer processingCount;

    private Integer scoredCount;

    private Integer failedCount;

    private BigDecimal fluencyAndCoherence;

    private BigDecimal lexicalResource;

    private BigDecimal grammaticalRangeAndAccuracy;

    private BigDecimal pronunciation;

    private BigDecimal overallScore;

    private String feedback;

    private List<SpeakingConversationReviewVO> conversations;
}

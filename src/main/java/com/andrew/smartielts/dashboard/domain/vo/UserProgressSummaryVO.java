package com.andrew.smartielts.dashboard.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserProgressSummaryVO {

    private BigDecimal listeningAverageScore;

    private BigDecimal readingAverageScore;

    private BigDecimal writingAverageScore;

    private BigDecimal speakingAverageScore;

    private BigDecimal overallAverageScore;

    private LocalDateTime generatedAt;
}
package com.andrew.smartielts.dashboard.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminUserRecordSummaryVO {

    private Long userId;

    private long listeningActiveRecords;
    private long listeningDeletedRecords;

    private long readingActiveRecords;
    private long readingDeletedRecords;

    private long writingActiveRecords;
    private long writingDeletedRecords;

    private long speakingActiveRecords;
    private long speakingDeletedRecords;

    private long totalActiveRecords;
    private long totalDeletedRecords;

    private BigDecimal listeningAverageScore;
    private BigDecimal readingAverageScore;
    private BigDecimal writingAverageScore;
    private BigDecimal speakingAverageScore;

    private BigDecimal averageScore;

    private LocalDateTime generatedAt;
}
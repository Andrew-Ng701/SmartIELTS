package com.andrew.smartielts.dashboard.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserOverviewVO {

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

    private LocalDateTime generatedAt;
}
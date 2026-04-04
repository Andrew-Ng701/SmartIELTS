package com.andrew.smartielts.admin.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserConsoleSummaryVO {

    private Long userId;
    private String email;
    private String role;
    private Boolean userDeleted;

    private Long listeningActiveRecords;
    private Long readingActiveRecords;
    private Long writingActiveRecords;
    private Long speakingActiveRecords;

    private Long listeningDeletedRecords;
    private Long readingDeletedRecords;
    private Long writingDeletedRecords;
    private Long speakingDeletedRecords;

    private Long totalActiveRecords;
    private Long totalDeletedRecords;
    private LocalDateTime generatedAt;
}
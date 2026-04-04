package com.andrew.smartielts.user.domain.vo;

import lombok.Data;

@Data
public class UserStatsVO {

    private Long userId;

    private Long listeningActiveRecordCount;
    private Long listeningDeletedRecordCount;

    private Long readingActiveRecordCount;
    private Long readingDeletedRecordCount;

    private Long writingActiveRecordCount;
    private Long writingDeletedRecordCount;

    private Long speakingActiveRecordCount;
    private Long speakingDeletedRecordCount;

    private Long totalActiveRecordCount;
    private Long totalDeletedRecordCount;
}

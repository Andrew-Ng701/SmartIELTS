package com.andrew.smartielts.dashboard.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserRecentRecordVO {

    private String module;
    private Long recordId;

    /**
     * 顯示標題，例如 Listening Test #12 / Writing Task 1
     */
    private String title;

    /**
     * 顯示摘要，例如 score / ai score / overall score
     */
    private String summary;

    private String status;

    private LocalDateTime createdTime;
}
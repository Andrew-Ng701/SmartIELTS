package com.andrew.smartielts.dashboard.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminRecentIssueVO {

    /**
     * 模組：writing / speaking / listening / reading / system
     */
    private String module;

    /**
     * 問題類型：AI_FAILURE_SUMMARY / DATA_ANOMALY / QUERY_ALERT ...
     */
    private String issueType;

    /**
     * 問題數量
     */
    private long issueCount;

    /**
     * 問題說明，可選
     */
    private String message;

    /**
     * 生成時間
     */
    private LocalDateTime generatedAt;
}
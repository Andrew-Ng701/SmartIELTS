package com.andrew.smartielts.dashboard.domain.vo;

import lombok.Data;

@Data
public class AdminAiFailureVO {

    /**
     * 模組：writing / speaking
     */
    private String module;

    /**
     * AI 失敗筆數
     */
    private long failureCount;
}
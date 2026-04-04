package com.andrew.smartielts.dashboard.controller.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DashboardAskRequest {

    private String query;
    private Long targetUserId;
    private Map<String, Object> context;

    /**
     * CHAT / QUESTION_EXPLAIN / QUESTION_RESULT_EXPLAIN / ARTICLE_TITLE / ARTICLE_EXPLAIN / RECORD_REVIEW
     */
    private String askScene;

    /**
     * CHAT / TEXT / CARD / JSON
     */
    private String responseMode;

    /**
     * 前端直接帶入當前頁面正在查看的目標物件
     */
    private DashboardAskObjectRef objectRef;

    /**
     * 前端頁面預先載入的使用者摘要資料
     */
    private DashboardAskPreloadedPayload preloadedPayload;

    /**
     * 前端畫面附帶資訊，例如 pageName、route、tab、clientTime
     */
    private DashboardAskClientContext clientContext;
}
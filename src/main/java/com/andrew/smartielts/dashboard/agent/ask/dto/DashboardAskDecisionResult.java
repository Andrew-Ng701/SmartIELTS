package com.andrew.smartielts.dashboard.agent.ask.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardAskDecisionResult {

    /**
     * DIRECT_ANSWER / GENERATE_SQL / NEED_CLARIFICATION / EXIT
     */
    private String action;

    /**
     * 資料是否足夠直接回答
     */
    private Boolean sufficient;

    /**
     * 如果足夠，第一輪 AI 直接給答案
     */
    private String answer;

    /**
     * 若不足，讓後端直接轉成 fallback intent / sql generation hint
     */
    private String capability;

    private Map<String, Object> filters;

    /**
     * AI 對當前資料判斷的簡短說明
     */
    private String reviewSummary;

    /**
     * 若需要更多資料，標記缺什麼
     */
    private List<String> requiredDataScopes;

    private List<String> suggestions;

    private Map<String, Object> meta;
}
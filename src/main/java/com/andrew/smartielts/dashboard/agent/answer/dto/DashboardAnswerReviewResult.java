package com.andrew.smartielts.dashboard.agent.answer.dto;

import com.andrew.smartielts.dashboard.agent.answer.DashboardAnswerReviewAction;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardAnswerReviewResult {
    /**
     * PROCEED / RETRY_QUERY / EXIT
     */
    private DashboardAnswerReviewAction action;

    /**
     * AI 對當前資料的審查摘要
     */
    private String reviewSummary;

    /**
     * 若為 RETRY_QUERY，允許返回新的補救 filters
     */
    private Map<String, Object> retryFilters;

    /**
     * 若為 EXIT，可直接提供退出時的自然語言
     */
    private String exitMessage;

    /**
     * 補充建議
     */
    private List<String> suggestions;
}
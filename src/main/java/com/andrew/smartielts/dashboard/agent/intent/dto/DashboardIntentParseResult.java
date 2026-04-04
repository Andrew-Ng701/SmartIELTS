package com.andrew.smartielts.dashboard.agent.intent.dto;

import com.andrew.smartielts.dashboard.agent.intent.DashboardIntentCapability;
import com.andrew.smartielts.dashboard.agent.intent.DashboardIntentQueryMode;
import com.andrew.smartielts.dashboard.agent.intent.DashboardIntentTargetScope;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DashboardIntentParseResult {

    /**
     * AI 是否成功理解該 query
     */
    private Boolean success;

    /**
     * AI 解析後的能力
     */
    private DashboardIntentCapability capability;

    /**
     * SIMPLE_HANDLER / STRUCTURED_QUERY / CLARIFICATION / UNSUPPORTED
     */
    private DashboardIntentQueryMode queryMode;

    /**
     * SELF / SPECIFIC_USER / GLOBAL
     */
    private DashboardIntentTargetScope targetScope;

    /**
     * 解析出的 target user
     */
    private Long targetUserId;

    /**
     * AI 解析出的條件
     */
    private Map<String, Object> filters;

    /**
     * 如果需要補問，放這裡
     */
    private String clarificationQuestion;

    /**
     * AI 對此次理解的摘要
     */
    private String reasoningSummary;

    /**
     * 置信度
     */
    private Double confidence;

    /**
     * 不支援或需補問時，給前端的建議
     */
    private List<String> suggestions;
}
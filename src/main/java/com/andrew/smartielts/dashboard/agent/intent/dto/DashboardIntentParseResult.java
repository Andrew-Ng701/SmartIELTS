package com.andrew.smartielts.dashboard.agent.intent.dto;

import com.andrew.smartielts.dashboard.agent.intent.DashboardIntentCapability;
import com.andrew.smartielts.dashboard.agent.intent.DashboardIntentQueryMode;
import com.andrew.smartielts.dashboard.agent.intent.DashboardIntentTargetScope;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DashboardIntentParseResult {

    private Boolean success;
    private DashboardIntentCapability capability;
    private DashboardIntentQueryMode queryMode;
    private DashboardIntentTargetScope targetScope;
    private Long targetUserId;
    private Map<String, Object> filters;
    private String clarificationQuestion;
    private String reasoningSummary;
    private Double confidence;
    private List<String> suggestions;

    public Boolean getSufficient() {
        return success;
    }

    public void setSufficient(Boolean sufficient) {
        this.success = sufficient;
    }

    public String getReviewSummary() {
        return reasoningSummary;
    }

    public void setReviewSummary(String reviewSummary) {
        this.reasoningSummary = reviewSummary;
    }
}
package com.andrew.smartielts.dashboard.agent.answer.impl;

import com.andrew.smartielts.dashboard.agent.answer.DashboardAnswerReviewAction;
import com.andrew.smartielts.dashboard.agent.answer.DashboardAnswerReviewService;
import com.andrew.smartielts.dashboard.agent.answer.dto.DashboardAnswerReviewRequest;
import com.andrew.smartielts.dashboard.agent.answer.dto.DashboardAnswerReviewResult;
import com.andrew.smartielts.dashboard.domain.vo.UserProgressSummaryVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class DashboardFallbackAnswerReviewService implements DashboardAnswerReviewService {

    @Override
    public DashboardAnswerReviewResult review(DashboardAnswerReviewRequest request) {
        Object data = request.getData();
        String query = request.getOriginalQuery() == null ? "" : request.getOriginalQuery().toLowerCase(Locale.ROOT);

        if (data == null) {
            return DashboardAnswerReviewResult.builder()
                    .action(DashboardAnswerReviewAction.EXIT)
                    .reviewSummary("No data returned from backend.")
                    .exitMessage("目前沒有查到可用資料，因此暫時無法回答這個問題。")
                    .suggestions(List.of("查看總覽", "換個方式再問一次"))
                    .build();
        }

        if (data instanceof UserProgressSummaryVO
                && containsComparisonIntent(query)) {
            return DashboardAnswerReviewResult.builder()
                    .action(DashboardAnswerReviewAction.EXIT)
                    .reviewSummary("Current progress summary only contains overall averages, not week-over-week comparison data.")
                    .exitMessage("目前查到的資料只有整體平均分，未包含今週與上週的比較資料，因此暫時無法準確判斷是否進步。")
                    .suggestions(List.of("查看整體平均分", "查看最近紀錄", "查看各科統計"))
                    .build();
        }

        if (containsRecentIntent(query) && !hasRecentLimit(request.getFilters())) {
            return DashboardAnswerReviewResult.builder()
                    .action(DashboardAnswerReviewAction.RETRY_QUERY)
                    .reviewSummary("The query appears to ask for recent data but current filters do not constrain recency.")
                    .retryFilters(mergeFilters(request.getFilters(), Map.of("limit", 10, "sortBy", "createdTime", "sortDirection", "desc")))
                    .suggestions(List.of("查看最近紀錄"))
                    .build();
        }

        return DashboardAnswerReviewResult.builder()
                .action(DashboardAnswerReviewAction.PROCEED)
                .reviewSummary("Current data is acceptable for answer generation.")
                .retryFilters(Map.of())
                .suggestions(List.of())
                .build();
    }

    private boolean containsComparisonIntent(String query) {
        return query.contains("對比")
                || query.contains("比較")
                || query.contains("相比")
                || query.contains("進步")
                || query.contains("上週")
                || query.contains("上个星期")
                || query.contains("上個星期")
                || query.contains("this week")
                || query.contains("last week");
    }

    private boolean containsRecentIntent(String query) {
        return query.contains("最近")
                || query.contains("recent")
                || query.contains("latest");
    }

    private boolean hasRecentLimit(Map<String, Object> filters) {
        return filters != null && filters.containsKey("limit");
    }

    private Map<String, Object> mergeFilters(Map<String, Object> oldFilters, Map<String, Object> newFilters) {
        java.util.Map<String, Object> merged = new java.util.HashMap<>();
        if (oldFilters != null) {
            merged.putAll(oldFilters);
        }
        merged.putAll(newFilters);
        return merged;
    }
}
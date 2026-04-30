package com.andrew.smartielts.dashboard.agent.answer.impl;

import com.andrew.smartielts.dashboard.agent.answer.DashboardAnswerReviewAction;
import com.andrew.smartielts.dashboard.agent.answer.DashboardAnswerReviewService;
import com.andrew.smartielts.dashboard.agent.answer.dto.DashboardAnswerReviewRequest;
import com.andrew.smartielts.dashboard.agent.answer.dto.DashboardAnswerReviewResult;
import com.andrew.smartielts.dashboard.agent.intent.DashboardIntentFilterKeys;
import com.andrew.smartielts.dashboard.domain.vo.UserProgressSummaryVO;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class DashboardFallbackAnswerReviewService implements DashboardAnswerReviewService {

    @Override
    public DashboardAnswerReviewResult review(DashboardAnswerReviewRequest request) {
        Object data = request.getData();
        String query = request.getOriginalQuery() == null
                ? ""
                : request.getOriginalQuery().toLowerCase(Locale.ROOT);

        if (data == null) {
            return DashboardAnswerReviewResult.builder()
                    .action(DashboardAnswerReviewAction.EXIT)
                    .reviewSummary("No data returned from backend.")
                    .exitMessage("目前沒有足夠資料可回答這個問題。")
                    .suggestions(List.of())
                    .build();
        }

        if (data instanceof UserProgressSummaryVO && containsComparisonIntent(query)) {
            return DashboardAnswerReviewResult.builder()
                    .action(DashboardAnswerReviewAction.EXIT)
                    .reviewSummary("Current progress summary only contains overall averages, not comparison-ready data.")
                    .exitMessage("目前這組資料較適合總覽，還不足以支撐你要的比較分析。")
                    .suggestions(List.of())
                    .build();
        }

        if (containsRecentIntent(query) && !hasRecentLimit(request.getFilters())) {
            return DashboardAnswerReviewResult.builder()
                    .action(DashboardAnswerReviewAction.RETRY_QUERY)
                    .reviewSummary("The query appears to ask for recent data but current filters do not constrain recency.")
                    .retryFilters(mergeFilters(request.getFilters(), Map.of(
                            DashboardIntentFilterKeys.LIMIT, 10,
                            DashboardIntentFilterKeys.SORT_BY, "created_time",
                            DashboardIntentFilterKeys.SORT_DIRECTION, "desc"
                    )))
                    .suggestions(List.of())
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
        return query.contains("compare")
                || query.contains("comparison")
                || query.contains("vs")
                || query.contains("對比")
                || query.contains("比較")
                || query.contains("this week")
                || query.contains("last week");
    }

    private boolean containsRecentIntent(String query) {
        return query.contains("最近")
                || query.contains("recent")
                || query.contains("latest");
    }

    private boolean hasRecentLimit(Map<String, Object> filters) {
        return filters != null && filters.containsKey(DashboardIntentFilterKeys.LIMIT);
    }

    private Map<String, Object> mergeFilters(Map<String, Object> oldFilters, Map<String, Object> newFilters) {
        Map<String, Object> merged = new HashMap<>();
        if (oldFilters != null) {
            merged.putAll(oldFilters);
        }
        merged.putAll(newFilters);
        return merged;
    }
}
package com.andrew.smartielts.dashboard.agent.answer.impl;

import com.andrew.smartielts.dashboard.agent.answer.DashboardSuggestionService;
import com.andrew.smartielts.dashboard.agent.intent.DashboardIntentFilterKeys;
import com.andrew.smartielts.dashboard.agent.intent.dto.DashboardIntentParseResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class DashboardSuggestionServiceImpl implements DashboardSuggestionService {

    @Override
    public List<String> buildSuggestions(
            String role,
            String originalQuery,
            String answer,
            String capability,
            DashboardIntentParseResult intent,
            Map<String, Object> filters,
            Object data
    ) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        String query = lower(originalQuery);
        String cap = capability == null ? "" : capability;
        String module = extractModule(query, filters, data);
        String timeRange = extractTimeRange(filters, query);
        String userId = extractUserId(data, filters);

        if (userId != null) {
            result.add("查看 user " + userId + " 最近 10 筆作答");
        }
        if (module != null) {
            result.add("分析 " + toZhModule(module) + " 模組表現");
        }
        if (timeRange != null) {
            result.add("查看 " + toZhTimeRange(nextTimeRange(timeRange)) + " 的變化");
        } else {
            result.add("查看最近 30 天的趨勢");
        }
        if (cap.contains("RECENT") || query.contains("最近") || query.contains("latest")) {
            result.add("查看最近 10 筆明細");
        }
        if (cap.contains("PROGRESS") || query.contains("平均") || query.contains("進步")) {
            result.add("比較最近 30 天與前一期");
        }
        if ("ADMIN".equalsIgnoreCase(role)) {
            result.add("查看異常 AI 任務與失敗原因");
        }

        if (result.isEmpty()) {
            result.add("查看最近 10 筆資料");
            result.add("查看最近 30 天趨勢");
            result.add("切換模組重新分析");
        }

        return new ArrayList<>(result.stream()
                .filter(it -> it != null && !it.isBlank())
                .limit(3)
                .toList());
    }

    private String extractModule(String query, Map<String, Object> filters, Object data) {
        if (filters != null && filters.get(DashboardIntentFilterKeys.MODULE) != null) {
            return String.valueOf(filters.get(DashboardIntentFilterKeys.MODULE)).toLowerCase(Locale.ROOT);
        }
        if (query.contains("listening") || query.contains("聽力")) {
            return "listening";
        }
        if (query.contains("reading") || query.contains("閱讀")) {
            return "reading";
        }
        if (query.contains("writing") || query.contains("寫作")) {
            return "writing";
        }
        if (query.contains("speaking") || query.contains("口說")) {
            return "speaking";
        }
        return firstRowValue(data, "module");
    }

    private String extractTimeRange(Map<String, Object> filters, String query) {
        if (filters != null && filters.get(DashboardIntentFilterKeys.TIME_RANGE) != null) {
            return String.valueOf(filters.get(DashboardIntentFilterKeys.TIME_RANGE)).toLowerCase(Locale.ROOT);
        }
        if (query.contains("7")) {
            return "last7days";
        }
        if (query.contains("30")) {
            return "last30days";
        }
        if (query.contains("last 7")) {
            return "last7days";
        }
        if (query.contains("last 30")) {
            return "last30days";
        }
        return null;
    }

    private String extractUserId(Object data, Map<String, Object> filters) {
        if (filters != null && filters.get(DashboardIntentFilterKeys.TARGET_USER_ID) != null) {
            return String.valueOf(filters.get(DashboardIntentFilterKeys.TARGET_USER_ID));
        }
        return firstRowValue(data, "user_id");
    }

    @SuppressWarnings("unchecked")
    private String firstRowValue(Object data, String key) {
        if (data instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Map<?, ?> map) {
            Object value = ((Map<String, Object>) map).get(key);
            return value == null ? null : String.valueOf(value);
        }
        return null;
    }

    private String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }

    private String nextTimeRange(String current) {
        if ("last7days".equals(current)) {
            return "last30days";
        }
        if ("last30days".equals(current)) {
            return "last90days";
        }
        return "last30days";
    }

    private String toZhModule(String module) {
        return switch (module.toLowerCase(Locale.ROOT)) {
            case "listening" -> "聽力";
            case "reading" -> "閱讀";
            case "writing" -> "寫作";
            case "speaking" -> "口說";
            default -> module;
        };
    }

    private String toZhTimeRange(String timeRange) {
        return switch (timeRange.toLowerCase(Locale.ROOT)) {
            case "last7days" -> "最近 7 天";
            case "last30days" -> "最近 30 天";
            case "last90days" -> "最近 90 天";
            default -> timeRange;
        };
    }
}
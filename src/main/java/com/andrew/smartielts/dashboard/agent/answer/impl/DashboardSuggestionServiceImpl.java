package com.andrew.smartielts.dashboard.agent.answer.impl;

import com.andrew.smartielts.dashboard.agent.answer.DashboardSuggestionService;
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
    public List<String> buildSuggestions(String role,
                                         String originalQuery,
                                         String answer,
                                         String capability,
                                         DashboardIntentParseResult intent,
                                         Map<String, Object> filters,
                                         Object data) {
        LinkedHashSet<String> result = new LinkedHashSet<>();

        String query = lower(originalQuery);
        String cap = capability == null ? "" : capability;
        String module = extractModule(query, filters, data);
        String timeRange = extractTimeRange(filters, query);
        String userId = extractUserId(data, filters);

        if (userId != null) {
            result.add("查看 user " + userId + (module != null ? " 的" + toZhModule(module) : "") + "最近30天趨勢");
            result.add("比較 user " + userId + " 四科表現");
        }

        if (module != null) {
            result.add("查看" + toZhModule(module) + "最近10筆記錄");
            result.add("比較" + toZhModule(module) + "與其他模組表現");
        }

        if (timeRange != null) {
            result.add("改看" + toZhTimeRange(nextTimeRange(timeRange)) + "數據");
        } else {
            result.add("查看最近30天趨勢");
        }

        if (cap.contains("RECENT") || query.contains("最近") || query.contains("latest")) {
            result.add("只看最近10筆高分記錄");
        }

        if (cap.contains("PROGRESS") || query.contains("進步") || query.contains("average") || query.contains("平均")) {
            result.add("找出目前最弱的模組");
            result.add("比較最近30天與前30天平均分");
        }

        if ("ADMIN".equalsIgnoreCase(role)) {
            result.add("查看異常最多的模組");
            result.add("查看最近30天 AI 失敗趨勢");
        }

        if (result.isEmpty()) {
            result.add("查看最近10筆記錄");
            result.add("查看最近30天趨勢");
            result.add("比較各模組表現");
        }

        return new ArrayList<>(result).stream()
                .filter(it -> it != null && !it.isBlank())
                .limit(3)
                .toList();
    }

    private String extractModule(String query, Map<String, Object> filters, Object data) {
        if (filters != null && filters.get("module") != null) {
            return String.valueOf(filters.get("module")).toLowerCase(Locale.ROOT);
        }
        if (query.contains("listening")) return "listening";
        if (query.contains("reading")) return "reading";
        if (query.contains("writing")) return "writing";
        if (query.contains("speaking")) return "speaking";
        if (query.contains("聽")) return "listening";
        if (query.contains("讀")) return "reading";
        if (query.contains("寫")) return "writing";
        if (query.contains("說")) return "speaking";
        return firstRowValue(data, "module");
    }

    private String extractTimeRange(Map<String, Object> filters, String query) {
        if (filters != null && filters.get("timeRange") != null) {
            return String.valueOf(filters.get("timeRange")).toLowerCase(Locale.ROOT);
        }
        if (query.contains("最近7天")) return "last7days";
        if (query.contains("最近30天")) return "last30days";
        if (query.contains("last 7")) return "last7days";
        if (query.contains("last 30")) return "last30days";
        return null;
    }

    private String extractUserId(Object data, Map<String, Object> filters) {
        if (filters != null && filters.get("targetUserId") != null) {
            return String.valueOf(filters.get("targetUserId"));
        }
        return firstRowValue(data, "userId");
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
        if ("last7days".equals(current)) return "last30days";
        if ("last30days".equals(current)) return "last90days";
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
            case "last7days" -> "最近7天";
            case "last30days" -> "最近30天";
            case "last90days" -> "最近90天";
            default -> timeRange;
        };
    }
}
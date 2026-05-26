package com.andrew.smartielts.dashboard.agent.answer.impl;

import com.andrew.smartielts.dashboard.agent.answer.DashboardSuggestionService;
import com.andrew.smartielts.dashboard.agent.answer.DashboardSuggestionPerspectiveNormalizer;
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

    private static final String LANGUAGE_EN = "en";
    private static final String LANGUAGE_ZH_HANS = "zh-Hans";
    private static final String LANGUAGE_ZH_HANT = "zh-Hant";

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
        String language = detectLanguage(originalQuery, answer);

        if (userId != null && "ADMIN".equalsIgnoreCase(role)) {
            result.add(localize(language,
                    "View user " + userId + "'s recent 10 records",
                    "查看使用者 " + userId + " 的最近 10 筆紀錄",
                    "查看用户 " + userId + " 的最近 10 条记录"));
        }
        if (module != null) {
            result.add(localize(language,
                    "Review my " + toEnglishModule(module) + " performance",
                    "查看我的" + toZhModule(module, false) + "表現",
                    "查看我的" + toZhModule(module, true) + "表现"));
        }
        if (timeRange != null) {
            String nextRange = nextTimeRange(timeRange);
            result.add(localize(language,
                    "View my " + toEnglishTimeRange(nextRange) + " trend to track my progress over time",
                    "查看我的" + toZhTimeRange(nextRange, false) + "趨勢，追蹤我的進步變化",
                    "查看我的" + toZhTimeRange(nextRange, true) + "趋势，追踪我的进步变化"));
        } else {
            result.add(localize(language,
                    "View my 30-day trend to track my progress over time",
                    "查看我的 30 天趨勢，追蹤我的進步變化",
                    "查看我的 30 天趋势，追踪我的进步变化"));
        }
        if (cap.contains("RECENT") || query.contains("recent") || query.contains("latest")
                || query.contains("最近") || query.contains("最新")) {
            result.add(localize(language,
                    "Show my latest 10 practice records",
                    "查看我的最新 10 筆練習紀錄",
                    "查看我的最新 10 条练习记录"));
        }
        if (cap.contains("PROGRESS") || query.contains("progress") || query.contains("trend")
                || query.contains("進步") || query.contains("进步") || query.contains("趨勢") || query.contains("趋势")) {
            result.add(localize(language,
                    "Compare my recent progress across IELTS modules",
                    "比較我最近在各 IELTS 模組的進步",
                    "比较我最近在各 IELTS 模块的进步"));
        }
        if ("ADMIN".equalsIgnoreCase(role)) {
            result.add(localize(language,
                    "Review my recent AI scoring failure risks",
                    "查看我最近需要關注的 AI 評分失敗風險",
                    "查看我最近需要关注的 AI 评分失败风险"));
        }

        if (result.isEmpty()) {
            result.add(localize(language,
                    "Show my latest 10 practice records",
                    "查看我的最新 10 筆練習紀錄",
                    "查看我的最新 10 条练习记录"));
            result.add(localize(language,
                    "View my 30-day trend to track my progress over time",
                    "查看我的 30 天趨勢，追蹤我的進步變化",
                    "查看我的 30 天趋势，追踪我的进步变化"));
            result.add(localize(language,
                    "Find my weakest IELTS module",
                    "找出我目前最需要加強的 IELTS 模組",
                    "找出我目前最需要加强的 IELTS 模块"));
        }

        return new ArrayList<>(DashboardSuggestionPerspectiveNormalizer.normalize(result.stream()
                .filter(it -> it != null && !it.isBlank())
                .limit(3)
                .toList()));
    }

    private String extractModule(String query, Map<String, Object> filters, Object data) {
        if (filters != null && filters.get(DashboardIntentFilterKeys.MODULE) != null) {
            return String.valueOf(filters.get(DashboardIntentFilterKeys.MODULE)).toLowerCase(Locale.ROOT);
        }
        if (query.contains("listening") || query.contains("聽力") || query.contains("听力")) {
            return "listening";
        }
        if (query.contains("reading") || query.contains("閱讀") || query.contains("阅读")) {
            return "reading";
        }
        if (query.contains("writing") || query.contains("寫作") || query.contains("写作")) {
            return "writing";
        }
        if (query.contains("speaking") || query.contains("口說") || query.contains("口语")) {
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

    private String detectLanguage(String query, String answer) {
        String text = (query == null ? "" : query) + " " + (answer == null ? "" : answer);
        if (text.isBlank()) {
            return LANGUAGE_ZH_HANT;
        }
        for (char ch : text.toCharArray()) {
            if (Character.UnicodeScript.of(ch) == Character.UnicodeScript.HAN) {
                return containsSimplifiedChinese(text) ? LANGUAGE_ZH_HANS : LANGUAGE_ZH_HANT;
            }
        }
        return LANGUAGE_EN;
    }

    private boolean containsSimplifiedChinese(String text) {
        return text.contains("这") || text.contains("们") || text.contains("习") || text.contains("进")
                || text.contains("条") || text.contains("块") || text.contains("听") || text.contains("语");
    }

    private String localize(String language, String english, String traditional, String simplified) {
        return english;
    }

    private String toEnglishModule(String module) {
        return switch (module.toLowerCase(Locale.ROOT)) {
            case "listening" -> "listening";
            case "reading" -> "reading";
            case "writing" -> "writing";
            case "speaking" -> "speaking";
            default -> module;
        };
    }

    private String toZhModule(String module, boolean simplified) {
        return switch (module.toLowerCase(Locale.ROOT)) {
            case "listening" -> simplified ? "听力" : "聽力";
            case "reading" -> simplified ? "阅读" : "閱讀";
            case "writing" -> simplified ? "写作" : "寫作";
            case "speaking" -> simplified ? "口语" : "口說";
            default -> module;
        };
    }

    private String toEnglishTimeRange(String timeRange) {
        return switch (timeRange.toLowerCase(Locale.ROOT)) {
            case "last7days" -> "7-day";
            case "last30days" -> "30-day";
            case "last90days" -> "90-day";
            default -> timeRange;
        };
    }

    private String toZhTimeRange(String timeRange, boolean simplified) {
        return switch (timeRange.toLowerCase(Locale.ROOT)) {
            case "last7days" -> " 7 天";
            case "last30days" -> " 30 天";
            case "last90days" -> " 90 天";
            default -> timeRange;
        };
    }
}

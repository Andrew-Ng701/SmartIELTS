package com.andrew.smartielts.dashboard.service.impl;

import com.andrew.smartielts.dashboard.agent.answer.DashboardAnswerComposeService;
import com.andrew.smartielts.dashboard.agent.answer.dto.DashboardAnswerComposeRequest;
import com.andrew.smartielts.dashboard.agent.answer.dto.DashboardAnswerComposeResult;
import com.andrew.smartielts.dashboard.constants.DashboardExecutiveSummaryQueryConstants;
import com.andrew.smartielts.dashboard.constants.DashboardOverviewConstants;
import com.andrew.smartielts.dashboard.controller.dto.DashboardAskPreloadedPayload;
import com.andrew.smartielts.dashboard.domain.vo.AdminExecutiveSummaryVO;
import com.andrew.smartielts.dashboard.preload.DashboardPreloadService;
import com.andrew.smartielts.dashboard.service.DashboardExecutiveSummaryCacheService;
import com.andrew.smartielts.dashboard.service.AdminDashboardService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private static final long EXECUTIVE_SUMMARY_CACHE_TTL_MILLIS = 12 * 60 * 60 * 1000L;
    private static final String SUMMARY_SOURCE_CACHE = "executive_summary_cache";
    private static final String SUMMARY_SOURCE_COMPOSE = "preloaded_payload_compose";

    private final ObjectProvider<DashboardPreloadService> dashboardPreloadServiceProvider;
    private final ObjectMapper objectMapper;
    private final DashboardAnswerComposeService dashboardAnswerComposeService;
    private final DashboardExecutiveSummaryCacheService dashboardExecutiveSummaryCacheService;

    @Override
    public AdminExecutiveSummaryVO adminExecutiveSummary(Long operatorUserId, Long targetUserId, String timeRange) {
        return adminExecutiveSummary(operatorUserId, targetUserId, timeRange, null);
    }

    @Override
    public AdminExecutiveSummaryVO adminExecutiveSummary(Long operatorUserId,
                                                        Long targetUserId,
                                                        String timeRange,
                                                        String summaryCacheKey) {
        Long effectiveTargetUserId = null;
        String cacheKey = buildSummaryCacheKey(
                DashboardOverviewConstants.ROLE_ADMIN,
                operatorUserId,
                effectiveTargetUserId,
                timeRange,
                summaryCacheKey
        );
        AdminExecutiveSummaryVO cached = dashboardExecutiveSummaryCacheService.get(cacheKey, AdminExecutiveSummaryVO.class);
        if (cached != null) {
            markCacheHit(cached.getMeta(), true);
            return cached;
        }

        DashboardAskPreloadedPayload payload = loadAdminOverviewPayload(operatorUserId, effectiveTargetUserId, timeRange);
        String query = DashboardExecutiveSummaryQueryConstants.ADMIN_EXECUTIVE_SUMMARY_DEFAULT_QUERY;
        String summaryText = composeExecutiveSummary(
                DashboardOverviewConstants.ROLE_ADMIN,
                operatorUserId,
                effectiveTargetUserId,
                query,
                "admin_executive_summary",
                timeRange,
                payload
        );

        if (!hasText(summaryText)) {
            summaryText = buildFallbackSummary(payload, timeRange);
        }

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("summary_source", SUMMARY_SOURCE_COMPOSE);
        meta.put("summary_cache_enabled", true);
        meta.put("summary_cache_hit", false);
        meta.put("summary_cache_key", safeCacheKey(summaryCacheKey));
        meta.put("summary_cache_scope_key", cacheKey);
        meta.put("time_range", normalizeTimeRange(timeRange));
        meta.put("has_overview", payload.getOverview() != null);
        meta.put("has_progress_summary", payload.getProgressSummary() != null);
        meta.put("module_stat_count", payload.getModuleStats() == null ? 0 : payload.getModuleStats().size());
        meta.put("recent_record_count", payload.getRecentRecords() == null ? 0 : payload.getRecentRecords().size());

        AdminExecutiveSummaryVO result = AdminExecutiveSummaryVO.builder()
                .snapshotId(payload.getSnapshotId())
                .snapshotTime(payload.getSnapshotTime())
                .summaryType(DashboardOverviewConstants.SUMMARY_TYPE_AI)
                .summaryText(summaryText)
                .summarySentences(splitSummarySentences(summaryText))
                .queryUsed(query)
                .meta(meta)
                .build();
        dashboardExecutiveSummaryCacheService.put(cacheKey, result, EXECUTIVE_SUMMARY_CACHE_TTL_MILLIS);
        return result;
    }

    private DashboardAskPreloadedPayload loadAdminOverviewPayload(Long operatorUserId,
                                                                 Long targetUserId,
                                                                 String timeRange) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put(DashboardOverviewConstants.CONTEXT_KEY_TIME_RANGE, normalizeTimeRange(timeRange));
        return dashboardPreloadServiceProvider.getObject().preload(
                DashboardOverviewConstants.ROLE_ADMIN,
                operatorUserId,
                targetUserId,
                DashboardOverviewConstants.PAGE_NAME_ADMIN_OVERVIEW,
                null,
                context
        );
    }

    private String composeExecutiveSummary(String role,
                                           Long operatorUserId,
                                           Long targetUserId,
                                           String query,
                                           String pageName,
                                           String timeRange,
                                           DashboardAskPreloadedPayload payload) {
        Map<String, Object> data = new LinkedHashMap<>();
        putIfPresent(data, "query", query);
        putIfPresent(data, "askScene", DashboardOverviewConstants.ASK_SCENE_CHAT);
        putIfPresent(data, "responseMode", DashboardOverviewConstants.RESPONSE_MODE_DEFAULT);
        putIfPresent(data, "preloadedPayload", payload);

        if (payload != null) {
            putIfPresent(data, "overview", payload.getOverview());
            putIfPresent(data, "progressSummary", payload.getProgressSummary());
            putIfPresent(data, "recentRecords", payload.getRecentRecords());
            putIfPresent(data, "moduleStats", payload.getModuleStats());
            putIfPresent(data, "recentQuestions", payload.getRecentQuestions());
            putIfPresent(data, "recentPassages", payload.getRecentPassages());
            putIfPresent(data, "aggregates", payload.getAggregates());
        }

        Map<String, Object> filters = new LinkedHashMap<>();
        filters.put("pageName", pageName);
        filters.put("summaryType", "executive_summary");
        filters.put("tone", "concise_executive");
        filters.put("outputStyle", "2-3 short English sentences");
        filters.put("timeRange", normalizeTimeRange(timeRange));

        DashboardAnswerComposeResult result = dashboardAnswerComposeService.compose(
                DashboardAnswerComposeRequest.builder()
                        .role(role)
                        .operatorUserId(operatorUserId)
                        .targetUserId(targetUserId)
                        .originalQuery(query)
                        .capability("PRELOADED_DIRECT")
                        .filters(filters)
                        .data(data)
                        .responseLanguage(DashboardOverviewConstants.RESPONSE_LANGUAGE_EN)
                        .build()
        );
        return result == null || result.getAnswer() == null ? null : result.getAnswer().trim();
    }

    private String buildFallbackSummary(DashboardAskPreloadedPayload payload, String timeRange) {
        Map<String, Object> overview = toMap(payload.getOverview());
        String totalUsers = firstNonBlank(getString(overview, "totalUsers"), "0");
        String activeUsers = firstNonBlank(getString(overview, "activeUsers"), "0");
        String activeRecords = firstNonBlank(getString(overview, "totalActiveRecords"), "0");
        String aiFailures = firstNonBlank(getString(overview, "recentAiFailureCount"), "0");
        return "AI admin summary fallback for " + normalizeTimeRange(timeRange)
                + ": platform users " + totalUsers
                + ", active users " + activeUsers
                + ", active records " + activeRecords
                + ", recent AI failures " + aiFailures + ".";
    }

    private Map<String, Object> toMap(Object value) {
        if (value == null) {
            return Map.of();
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            map.forEach((k, v) -> result.put(String.valueOf(k), v));
            return result;
        }
        return objectMapper.convertValue(value, new TypeReference<Map<String, Object>>() {});
    }

    private void putIfPresent(Map<String, Object> target, String key, Object value) {
        if (target == null || key == null || key.isBlank() || value == null) {
            return;
        }
        if (value instanceof String text && text.isBlank()) {
            return;
        }
        if (value instanceof List<?> list && list.isEmpty()) {
            return;
        }
        if (value instanceof Map<?, ?> map && map.isEmpty()) {
            return;
        }
        target.put(key, value);
    }

    private String getString(Map<String, Object> map, String key) {
        if (map == null || map.isEmpty() || key == null) {
            return null;
        }
        Object value = map.get(key);
        return value == null ? null : String.valueOf(value).trim();
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalizeTimeRange(String timeRange) {
        return hasText(timeRange) ? timeRange.trim() : DashboardOverviewConstants.DEFAULT_TIME_RANGE;
    }

    private String buildSummaryCacheKey(String role,
                                        Long operatorUserId,
                                        Long targetUserId,
                                        String timeRange,
                                        String summaryCacheKey) {
        return String.join(":",
                safeKey(role),
                String.valueOf(operatorUserId),
                String.valueOf(targetUserId),
                safeKey(normalizeTimeRange(timeRange)),
                safeKey(summaryCacheKey)
        );
    }

    private String safeCacheKey(String summaryCacheKey) {
        return hasText(summaryCacheKey) ? summaryCacheKey.trim() : null;
    }

    private String safeKey(String value) {
        return hasText(value) ? value.trim().toLowerCase().replaceAll("[^a-z0-9._-]", "_") : "default";
    }

    private void markCacheHit(Map<String, Object> meta, boolean cacheHit) {
        if (meta == null) {
            return;
        }
        meta.put("summary_source", SUMMARY_SOURCE_CACHE);
        meta.put("summary_cache_hit", cacheHit);
    }

    private List<String> splitSummarySentences(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return Arrays.stream(text.split("[.\\n]+"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }
}

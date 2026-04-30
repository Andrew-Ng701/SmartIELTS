package com.andrew.smartielts.dashboard.agent.intent.impl;

import com.andrew.smartielts.dashboard.agent.intent.DashboardIntentCapability;
import com.andrew.smartielts.dashboard.agent.intent.DashboardIntentFilterKeys;
import com.andrew.smartielts.dashboard.agent.intent.DashboardIntentParseService;
import com.andrew.smartielts.dashboard.agent.intent.DashboardIntentQueryMode;
import com.andrew.smartielts.dashboard.agent.intent.DashboardIntentTargetScope;
import com.andrew.smartielts.dashboard.agent.intent.dto.DashboardIntentParseRequest;
import com.andrew.smartielts.dashboard.agent.intent.dto.DashboardIntentParseResponse;
import com.andrew.smartielts.dashboard.agent.intent.dto.DashboardIntentParseResult;
import com.andrew.smartielts.dashboard.agent.intent.llm.DashboardLlmClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardIntentParseServiceImpl implements DashboardIntentParseService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final DashboardLlmClient dashboardLlmClient;

    @Override
    public DashboardIntentParseResponse parse(DashboardIntentParseRequest request) {
        try {
            DashboardIntentParseResult result = dashboardLlmClient.parseIntent(request);
            normalize(result, request);
            return DashboardIntentParseResponse.builder()
                    .code(0)
                    .msg("ok")
                    .data(result)
                    .build();
        } catch (Exception e) {
            log.warn("Intent parse failed, fallback to STRUCTURED_QUERY: {}", e.getMessage());
            DashboardIntentParseResult fallback = fallbackStructuredQueryIntent(request);
            return DashboardIntentParseResponse.builder()
                    .code(0)
                    .msg("ok")
                    .data(fallback)
                    .build();
        }
    }

    private void normalize(DashboardIntentParseResult result, DashboardIntentParseRequest request) {
        if (result == null) {
            throw new IllegalArgumentException("Intent result cannot be null");
        }

        if (result.getSuccess() == null) {
            result.setSuccess(Boolean.TRUE);
        }
        if (result.getCapability() == null) {
            result.setCapability(DashboardIntentCapability.STRUCTURED_QUERY);
        }
        if (result.getQueryMode() == null) {
            result.setQueryMode(defaultQueryMode(result.getCapability()));
        }

        Long normalizedTargetUserId = normalizeTargetUserId(request, result);
        DashboardIntentTargetScope normalizedTargetScope = normalizeTargetScope(request, result, normalizedTargetUserId);
        Map<String, Object> normalizedFilters = normalizeFilters(request, result, normalizedTargetUserId);

        result.setTargetUserId(normalizedTargetUserId);
        result.setTargetScope(normalizedTargetScope);
        result.setFilters(normalizedFilters);

        if (result.getSuggestions() == null) {
            result.setSuggestions(List.of());
        }
        if (result.getConfidence() == null) {
            result.setConfidence(0.0D);
        }
    }

    private DashboardIntentQueryMode defaultQueryMode(DashboardIntentCapability capability) {
        if (capability == null) {
            return DashboardIntentQueryMode.STRUCTURED_QUERY;
        }
        return switch (capability) {
            case CLARIFICATION_REQUIRED -> DashboardIntentQueryMode.CLARIFICATION;
            case UNSUPPORTED -> DashboardIntentQueryMode.UNSUPPORTED;
            case STRUCTURED_QUERY -> DashboardIntentQueryMode.STRUCTURED_QUERY;
            default -> DashboardIntentQueryMode.SIMPLE_HANDLER;
        };
    }

    private DashboardIntentTargetScope normalizeTargetScope(
            DashboardIntentParseRequest request,
            DashboardIntentParseResult result,
            Long normalizedTargetUserId
    ) {
        if ("USER".equalsIgnoreCase(request.getRole())) {
            return DashboardIntentTargetScope.SELF;
        }
        if (result.getTargetScope() != null) {
            if (result.getTargetScope() == DashboardIntentTargetScope.SPECIFIC_USER && normalizedTargetUserId == null) {
                return DashboardIntentTargetScope.GLOBAL;
            }
            return result.getTargetScope();
        }
        return normalizedTargetUserId != null
                ? DashboardIntentTargetScope.SPECIFIC_USER
                : DashboardIntentTargetScope.GLOBAL;
    }

    private Long normalizeTargetUserId(DashboardIntentParseRequest request, DashboardIntentParseResult result) {
        if ("USER".equalsIgnoreCase(request.getRole())) {
            return request.getOperatorUserId();
        }
        if (result.getTargetUserId() != null) {
            return result.getTargetUserId();
        }
        if (request.getContextTargetUserId() != null) {
            return request.getContextTargetUserId();
        }
        Map<String, Object> filters = result.getFilters();
        if (filters != null) {
            Long filterTargetUserId = asLong(firstValue(filters,
                    DashboardIntentFilterKeys.TARGET_USER_ID,
                    "targetUserId"));
            if (filterTargetUserId != null) {
                return filterTargetUserId;
            }
        }
        return null;
    }

    private Map<String, Object> normalizeFilters(
            DashboardIntentParseRequest request,
            DashboardIntentParseResult result,
            Long normalizedTargetUserId
    ) {
        Map<String, Object> filters = new LinkedHashMap<>();

        if (result.getFilters() != null) {
            copyKnownFilter(result.getFilters(), filters, DashboardIntentFilterKeys.MODULE, "module");
            copyKnownFilter(result.getFilters(), filters, DashboardIntentFilterKeys.MODULES, "modules");
            copyKnownFilter(result.getFilters(), filters, DashboardIntentFilterKeys.TIME_RANGE, "timeRange");
            copyKnownFilter(result.getFilters(), filters, DashboardIntentFilterKeys.STATUS, "status");
            copyKnownFilter(result.getFilters(), filters, DashboardIntentFilterKeys.AGGREGATION, "aggregation");
            copyKnownFilter(result.getFilters(), filters, DashboardIntentFilterKeys.LIMIT, "limit");
            copyKnownFilter(result.getFilters(), filters, DashboardIntentFilterKeys.SORT_BY, "sortBy");
            copyKnownFilter(result.getFilters(), filters, DashboardIntentFilterKeys.SORT_DIRECTION, "sortDirection");
            copyKnownFilter(result.getFilters(), filters, DashboardIntentFilterKeys.METRIC_FOCUS, "metricFocus");
            copyKnownFilter(result.getFilters(), filters, DashboardIntentFilterKeys.METRICS, "metrics");
            copyKnownFilter(result.getFilters(), filters, DashboardIntentFilterKeys.FOCUS_METRIC, "focusMetric");
            copyKnownFilter(result.getFilters(), filters, DashboardIntentFilterKeys.TARGET_USER_ID, "targetUserId");
            copyKnownFilter(result.getFilters(), filters, DashboardIntentFilterKeys.RECORD_ID, "recordId");
            copyKnownFilter(result.getFilters(), filters, DashboardIntentFilterKeys.TEST_ID, "testId");
            copyKnownFilter(result.getFilters(), filters, DashboardIntentFilterKeys.PASSAGE_ID, "passageId");
            copyKnownFilter(result.getFilters(), filters, DashboardIntentFilterKeys.QUESTION_ID, "questionId");
            copyKnownFilter(result.getFilters(), filters, DashboardIntentFilterKeys.QUESTION_NUMBER, "questionNumber");
            copyKnownFilter(result.getFilters(), filters, DashboardIntentFilterKeys.SESSION_ID, "sessionId");
        }

        Map<String, Object> context = request.getContext();
        if (context != null && !context.isEmpty()) {
            copyKnownFilter(context, filters, DashboardIntentFilterKeys.MODULE, "module");
            copyKnownFilter(context, filters, DashboardIntentFilterKeys.TIME_RANGE, "timeRange");
            copyKnownFilter(context, filters, DashboardIntentFilterKeys.RECORD_ID, "recordId");
            copyKnownFilter(context, filters, DashboardIntentFilterKeys.TEST_ID, "testId");
            copyKnownFilter(context, filters, DashboardIntentFilterKeys.PASSAGE_ID, "passageId");
            copyKnownFilter(context, filters, DashboardIntentFilterKeys.QUESTION_ID, "questionId");
            copyKnownFilter(context, filters, DashboardIntentFilterKeys.QUESTION_NUMBER, "questionNumber");
            copyKnownFilter(context, filters, DashboardIntentFilterKeys.SESSION_ID, "sessionId");
            copyKnownFilter(context, filters, DashboardIntentFilterKeys.TARGET_USER_ID, "targetUserId");
        }

        String query = safeLower(request.getQuery());
        normalizeQueryDerivedFilters(filters, query);

        Integer limit = asInteger(filters.get(DashboardIntentFilterKeys.LIMIT));
        if (limit == null || limit <= 0) {
            filters.put(DashboardIntentFilterKeys.LIMIT, DEFAULT_LIMIT);
        } else {
            filters.put(DashboardIntentFilterKeys.LIMIT, Math.min(limit, MAX_LIMIT));
        }

        if (normalizedTargetUserId != null) {
            filters.put(DashboardIntentFilterKeys.TARGET_USER_ID, normalizedTargetUserId);
        }

        Object module = filters.get(DashboardIntentFilterKeys.MODULE);
        if (module instanceof String text && !text.isBlank()) {
            filters.put(DashboardIntentFilterKeys.MODULE, text.trim().toLowerCase(Locale.ROOT));
        }

        Object timeRange = filters.get(DashboardIntentFilterKeys.TIME_RANGE);
        if (timeRange instanceof String text && !text.isBlank()) {
            filters.put(DashboardIntentFilterKeys.TIME_RANGE, text.trim().toLowerCase(Locale.ROOT));
        }

        Object sortDirection = filters.get(DashboardIntentFilterKeys.SORT_DIRECTION);
        if (sortDirection instanceof String text && !text.isBlank()) {
            filters.put(DashboardIntentFilterKeys.SORT_DIRECTION, text.trim().toLowerCase(Locale.ROOT));
        }

        Object sortBy = filters.get(DashboardIntentFilterKeys.SORT_BY);
        if (sortBy instanceof String text && !text.isBlank()) {
            filters.put(DashboardIntentFilterKeys.SORT_BY, text.trim().toLowerCase(Locale.ROOT));
        }

        return filters;
    }

    private void normalizeQueryDerivedFilters(Map<String, Object> filters, String query) {
        if (!filters.containsKey(DashboardIntentFilterKeys.MODULE)) {
            if (query.contains("listening") || query.contains("聽力")) {
                filters.put(DashboardIntentFilterKeys.MODULE, "listening");
            } else if (query.contains("reading") || query.contains("閱讀")) {
                filters.put(DashboardIntentFilterKeys.MODULE, "reading");
            } else if (query.contains("writing") || query.contains("寫作")) {
                filters.put(DashboardIntentFilterKeys.MODULE, "writing");
            } else if (query.contains("speaking") || query.contains("口說")) {
                filters.put(DashboardIntentFilterKeys.MODULE, "speaking");
            }
        }

        if (!filters.containsKey(DashboardIntentFilterKeys.TIME_RANGE)) {
            if (query.contains("7") || query.contains("last 7")) {
                filters.put(DashboardIntentFilterKeys.TIME_RANGE, "last7days");
            } else if (query.contains("30") || query.contains("last 30")) {
                filters.put(DashboardIntentFilterKeys.TIME_RANGE, "last30days");
            } else if (query.contains("90") || query.contains("last 90")) {
                filters.put(DashboardIntentFilterKeys.TIME_RANGE, "last90days");
            }
        }

        if (!filters.containsKey(DashboardIntentFilterKeys.SORT_BY)
                && (query.contains("最近") || query.contains("recent") || query.contains("latest"))) {
            filters.put(DashboardIntentFilterKeys.SORT_BY, "created_time");
        }

        if (!filters.containsKey(DashboardIntentFilterKeys.SORT_DIRECTION)
                && (query.contains("最近") || query.contains("recent") || query.contains("latest"))) {
            filters.put(DashboardIntentFilterKeys.SORT_DIRECTION, "desc");
        }

        if (!filters.containsKey(DashboardIntentFilterKeys.LIMIT)) {
            if (query.contains("top 5") || query.contains("最近5") || query.contains("5筆")) {
                filters.put(DashboardIntentFilterKeys.LIMIT, 5);
            } else if (query.contains("top 10") || query.contains("最近10") || query.contains("10筆")) {
                filters.put(DashboardIntentFilterKeys.LIMIT, 10);
            }
        }
    }

    private DashboardIntentParseResult fallbackStructuredQueryIntent(DashboardIntentParseRequest request) {
        DashboardIntentParseResult result = new DashboardIntentParseResult();
        result.setSuccess(Boolean.TRUE);
        result.setCapability(DashboardIntentCapability.STRUCTURED_QUERY);
        result.setQueryMode(DashboardIntentQueryMode.STRUCTURED_QUERY);

        Long targetUserId = "USER".equalsIgnoreCase(request.getRole())
                ? request.getOperatorUserId()
                : request.getContextTargetUserId();

        result.setTargetScope("USER".equalsIgnoreCase(request.getRole())
                ? DashboardIntentTargetScope.SELF
                : targetUserId != null ? DashboardIntentTargetScope.SPECIFIC_USER : DashboardIntentTargetScope.GLOBAL);
        result.setTargetUserId(targetUserId);
        result.setFilters(normalizeFilters(request, result, targetUserId));
        result.setClarificationQuestion(null);
        result.setReasoningSummary("Fallback to structured query because intent parse failed.");
        result.setConfidence(0.2D);
        result.setSuggestions(List.of());
        return result;
    }

    private void copyKnownFilter(Map<String, Object> source, Map<String, Object> target, String canonicalKey, String legacyKey) {
        Object value = firstValue(source, canonicalKey, legacyKey);
        if (value != null) {
            target.putIfAbsent(canonicalKey, value);
        }
    }

    private Object firstValue(Map<String, Object> source, String canonicalKey, String legacyKey) {
        if (source.containsKey(canonicalKey)) {
            return source.get(canonicalKey);
        }
        return source.get(legacyKey);
    }

    private Integer asInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Long asLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (Exception e) {
            return null;
        }
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
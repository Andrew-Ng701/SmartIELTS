package com.andrew.smartielts.dashboard.agent.intent.impl;

import com.andrew.smartielts.dashboard.agent.intent.DashboardIntentCapability;
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
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardIntentParseServiceImpl implements DashboardIntentParseService {

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
        if (result.getCapability() == null) {
            result.setCapability(DashboardIntentCapability.STRUCTURED_QUERY);
        }
        if (result.getQueryMode() == null) {
            result.setQueryMode(DashboardIntentQueryMode.STRUCTURED_QUERY);
        }
        if (result.getTargetScope() == null) {
            result.setTargetScope("ADMIN".equalsIgnoreCase(request.getRole())
                    ? DashboardIntentTargetScope.GLOBAL
                    : DashboardIntentTargetScope.SELF);
        }
        if (result.getFilters() == null) {
            result.setFilters(new LinkedHashMap<>());
        }
        if (result.getSuggestions() == null) {
            result.setSuggestions(List.of());
        }
        if (result.getConfidence() == null) {
            result.setConfidence(0.0D);
        }
    }

    private DashboardIntentParseResult fallbackStructuredQueryIntent(DashboardIntentParseRequest request) {
        DashboardIntentParseResult result = new DashboardIntentParseResult();
        result.setSuccess(Boolean.TRUE);
        result.setCapability(DashboardIntentCapability.STRUCTURED_QUERY);
        result.setQueryMode(DashboardIntentQueryMode.STRUCTURED_QUERY);
        result.setTargetScope("ADMIN".equalsIgnoreCase(request.getRole())
                ? DashboardIntentTargetScope.GLOBAL
                : DashboardIntentTargetScope.SELF);
        result.setTargetUserId("USER".equalsIgnoreCase(request.getRole()) ? request.getOperatorUserId() : request.getContextTargetUserId());
        result.setFilters(defaultFilters(request.getContext()));
        result.setClarificationQuestion(null);
        result.setReasoningSummary("Fallback to structured query because intent parse failed.");
        result.setConfidence(0.2D);
        result.setSuggestions(List.of());
        return result;
    }

    private Map<String, Object> defaultFilters(Map<String, Object> context) {
        Map<String, Object> filters = new LinkedHashMap<>();
        filters.put("limit", 20);
        if (context != null && context.containsKey("module")) {
            filters.put("module", context.get("module"));
        }
        if (context != null && context.containsKey("timeRange")) {
            filters.put("timeRange", context.get("timeRange"));
        }
        return filters;
    }
}
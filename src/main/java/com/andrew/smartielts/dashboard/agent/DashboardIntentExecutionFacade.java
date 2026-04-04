package com.andrew.smartielts.dashboard.agent;

import com.andrew.smartielts.dashboard.agent.answer.DashboardAnswerComposeService;
import com.andrew.smartielts.dashboard.agent.ask.DashboardAskDecisionService;
import com.andrew.smartielts.dashboard.agent.ask.dto.DashboardAskDecisionRequest;
import com.andrew.smartielts.dashboard.agent.ask.dto.DashboardAskDecisionResult;
import com.andrew.smartielts.dashboard.agent.intent.DashboardIntentCapability;
import com.andrew.smartielts.dashboard.agent.intent.DashboardIntentParseService;
import com.andrew.smartielts.dashboard.agent.intent.DashboardIntentPermissionValidator;
import com.andrew.smartielts.dashboard.agent.intent.DashboardIntentQueryMode;
import com.andrew.smartielts.dashboard.agent.intent.DashboardIntentTargetScope;
import com.andrew.smartielts.dashboard.agent.intent.dto.DashboardIntentParseRequest;
import com.andrew.smartielts.dashboard.agent.intent.dto.DashboardIntentParseResponse;
import com.andrew.smartielts.dashboard.agent.intent.dto.DashboardIntentParseResult;
import com.andrew.smartielts.dashboard.controller.dto.DashboardAskRequest;
import com.andrew.smartielts.dashboard.controller.dto.DashboardAssistantResponse;
import com.andrew.smartielts.dashboard.learning.DashboardLearningContextService;
import com.andrew.smartielts.dashboard.query.DashboardStructuredAiQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardIntentExecutionFacade {

    private final DashboardIntentParseService dashboardIntentParseService;
    private final DashboardIntentPermissionValidator permissionValidator;
    private final DashboardCapabilityRouter capabilityRouter;
    private final DashboardAnswerComposeService dashboardAnswerComposeService;
    private final DashboardStructuredAiQueryService dashboardStructuredAiQueryService;

    private final DashboardAskDecisionService dashboardAskDecisionService;
    private final DashboardLearningContextService dashboardLearningContextService;

    public DashboardAssistantResponse ask(
            String role,
            Long operatorUserId,
            Long contextTargetUserId,
            DashboardAskRequest request) {

        long startedAt = System.currentTimeMillis();
        Long resolvedTargetUserId = request.getTargetUserId() != null
                ? request.getTargetUserId()
                : (contextTargetUserId != null ? contextTargetUserId : operatorUserId);

        Map<String, Object> learningContext = dashboardLearningContextService.buildLearningContext(
                role,
                operatorUserId,
                resolvedTargetUserId,
                request.getAskScene(),
                request.getObjectRef()
        );

        DashboardAskDecisionResult decision = dashboardAskDecisionService.decide(
                DashboardAskDecisionRequest.builder()
                        .role(role)
                        .operatorUserId(operatorUserId)
                        .targetUserId(resolvedTargetUserId)
                        .query(request.getQuery())
                        .responseLanguage(detectResponseLanguage(request.getQuery()))
                        .askScene(request.getAskScene())
                        .responseMode(request.getResponseMode())
                        .objectRef(request.getObjectRef())
                        .preloadedPayload(request.getPreloadedPayload())
                        .clientContext(request.getClientContext())
                        .context(request.getContext())
                        .learningContext(learningContext)
                        .build()
        );

        if ("DIRECT_ANSWER".equalsIgnoreCase(decision.getAction())
                && Boolean.TRUE.equals(decision.getSufficient())) {

            return DashboardAssistantResponse.builder()
                    .answer(decision.getAnswer())
                    .data(buildDirectAnswerData(request, learningContext))
                    .suggestions(safeList(decision.getSuggestions()))
                    .meta(mergeMeta(Map.of(
                            "answerMode", "AI_DIRECT",
                            "askScene", safeString(request.getAskScene()),
                            "sufficient", true,
                            "elapsedMs", System.currentTimeMillis() - startedAt,
                            "reviewSummary", safeString(decision.getReviewSummary())
                    ), decision.getMeta()))
                    .build();
        }

        if ("NEED_CLARIFICATION".equalsIgnoreCase(decision.getAction())) {
            return DashboardAssistantResponse.builder()
                    .answer(nonBlank(decision.getAnswer(), "請補充更具體的題目、文章或作答範圍。"))
                    .data(Map.of(
                            "askScene", safeString(request.getAskScene()),
                            "objectRef", request.getObjectRef()
                    ))
                    .suggestions(safeList(decision.getSuggestions()))
                    .meta(mergeMeta(Map.of(
                            "answerMode", "CLARIFICATION",
                            "sufficient", false,
                            "elapsedMs", System.currentTimeMillis() - startedAt
                    ), decision.getMeta()))
                    .build();
        }

        DashboardIntentParseResult fallbackIntent = toFallbackStructuredIntent(
                role, operatorUserId, resolvedTargetUserId, request, decision);

        permissionValidator.validate(role, operatorUserId, fallbackIntent);

        return dashboardStructuredAiQueryService.execute(
                role,
                operatorUserId,
                resolvedTargetUserId,
                request.getQuery(),
                fallbackIntent,
                mergeContext(request.getContext(), Map.of(
                        "askScene", request.getAskScene(),
                        "responseMode", request.getResponseMode(),
                        "objectRef", request.getObjectRef(),
                        "preloadedPayload", request.getPreloadedPayload(),
                        "clientContext", request.getClientContext(),
                        "learningContext", learningContext,
                        "decisionReviewSummary", decision.getReviewSummary(),
                        "requiredDataScopes", safeList(decision.getRequiredDataScopes())
                )));
    }

    private DashboardIntentParseResult toFallbackStructuredIntent(
            String role,
            Long operatorUserId,
            Long targetUserId,
            DashboardAskRequest request,
            DashboardAskDecisionResult decision) {

        DashboardIntentParseResult result = new DashboardIntentParseResult();
        result.setSuccess(Boolean.TRUE);
        result.setCapability(DashboardIntentCapability.STRUCTUREDQUERY);
        result.setQueryMode(DashboardIntentQueryMode.STRUCTUREDQUERY);
        result.setTargetScope("ADMIN".equalsIgnoreCase(role)
                ? DashboardIntentTargetScope.SPECIFICUSER
                : DashboardIntentTargetScope.SELF);
        result.setTargetUserId(targetUserId);
        result.setFilters(decision.getFilters() == null ? new LinkedHashMap<>() : new LinkedHashMap<>(decision.getFilters()));
        result.setClarificationQuestion(null);
        result.setReasoningSummary(nonBlank(decision.getReviewSummary(),
                "Round-1 AI found current context insufficient, fallback to structured query."));
        result.setConfidence(0.80D);
        result.setSuggestions(safeList(decision.getSuggestions()));
        return result;
    }

    private Map<String, Object> buildDirectAnswerData(DashboardAskRequest request, Map<String, Object> learningContext) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("objectRef", request.getObjectRef());
        data.put("learningContext", learningContext);
        data.put("preloadedPayload", request.getPreloadedPayload());
        return data;
    }

    private Map<String, Object> mergeContext(Map<String, Object> left, Map<String, Object> right) {
        Map<String, Object> merged = new LinkedHashMap<>();
        if (left != null) {
            merged.putAll(left);
        }
        if (right != null) {
            merged.putAll(right);
        }
        return merged;
    }

    private Map<String, Object> mergeMeta(Map<String, Object> base, Map<String, Object> ext) {
        Map<String, Object> merged = new LinkedHashMap<>();
        if (base != null) {
            merged.putAll(base);
        }
        if (ext != null) {
            merged.putAll(ext);
        }
        return merged;
    }

    private String detectResponseLanguage(String query) {
        if (query == null || query.isBlank()) {
            return "en";
        }
        for (char ch : query.toCharArray()) {
            if (Character.UnicodeScript.of(ch) == Character.UnicodeScript.HAN) {
                return "zh-Hant";
            }
        }
        return "en";
    }

    private List<String> safeList(List<String> list) {
        return list == null ? List.of() : list;
    }

    private String safeString(String value) {
        return value == null ? "" : value.trim();
    }

    private String nonBlank(String first, String fallback) {
        return first != null && !first.isBlank() ? first : fallback;
    }
}
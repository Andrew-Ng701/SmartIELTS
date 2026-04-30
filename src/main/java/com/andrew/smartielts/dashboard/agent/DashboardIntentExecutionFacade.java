package com.andrew.smartielts.dashboard.agent;

import com.andrew.smartielts.dashboard.agent.answer.DashboardAnswerComposeService;
import com.andrew.smartielts.dashboard.agent.answer.dto.DashboardAnswerComposeRequest;
import com.andrew.smartielts.dashboard.agent.answer.dto.DashboardAnswerComposeResult;
import com.andrew.smartielts.dashboard.agent.ask.DashboardAskContextResolver;
import com.andrew.smartielts.dashboard.agent.ask.DashboardAskDecisionService;
import com.andrew.smartielts.dashboard.agent.ask.dto.DashboardAskDecisionRequest;
import com.andrew.smartielts.dashboard.agent.ask.dto.DashboardAskDecisionResult;
import com.andrew.smartielts.dashboard.agent.intent.DashboardIntentCapability;
import com.andrew.smartielts.dashboard.agent.intent.DashboardIntentPermissionValidator;
import com.andrew.smartielts.dashboard.agent.intent.DashboardIntentQueryMode;
import com.andrew.smartielts.dashboard.agent.intent.DashboardIntentTargetScope;
import com.andrew.smartielts.dashboard.agent.intent.dto.DashboardIntentParseResult;
import com.andrew.smartielts.dashboard.controller.dto.DashboardAskClientContext;
import com.andrew.smartielts.dashboard.controller.dto.DashboardAskPreloadedPayload;
import com.andrew.smartielts.dashboard.controller.dto.DashboardAskRequest;
import com.andrew.smartielts.dashboard.controller.dto.DashboardAssistantResponse;
import com.andrew.smartielts.dashboard.learning.DashboardLearningContextService;
import com.andrew.smartielts.dashboard.preload.DashboardPreloadService;
import com.andrew.smartielts.dashboard.query.DashboardStructuredAiQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.andrew.smartielts.dashboard.agent.ask.DashboardAskConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardIntentExecutionFacade {

    private static final String ROLE_USER = "USER";
    private static final String RESPONSE_LANGUAGE_ZH_HANT = "zh-Hant";
    private static final String RESPONSE_LANGUAGE_EN = "en";
    private static final String CAPABILITY_PRELOADED_DIRECT = "PRELOADED_DIRECT";
    private static final String CAPABILITY_STRUCTURED_QUERY = "STRUCTURED_QUERY";

    private static final String META_KEY_ANSWER_MODE = "answerMode";
    private static final String META_KEY_ASK_SCENE = "askScene";
    private static final String META_KEY_ACTION = "action";
    private static final String META_KEY_SUFFICIENT = "sufficient";
    private static final String META_KEY_USED_PRELOAD = "usedPreload";
    private static final String META_KEY_PRELOAD_SOURCE = "preloadSource";
    private static final String META_KEY_ELAPSED_MS = "elapsedMs";
    private static final String META_KEY_REVIEW_SUMMARY = "reviewSummary";

    private final DashboardAskDecisionService dashboardAskDecisionService;
    private final DashboardAskContextResolver dashboardAskContextResolver;
    private final DashboardAnswerComposeService dashboardAnswerComposeService;
    private final DashboardStructuredAiQueryService dashboardStructuredAiQueryService;
    private final DashboardIntentPermissionValidator permissionValidator;
    private final DashboardLearningContextService dashboardLearningContextService;
    private final DashboardPreloadService dashboardPreloadService;

    public DashboardAssistantResponse ask(String role,
                                          Long operatorUserId,
                                          Long targetUserId,
                                          DashboardAskRequest request) {

        long startedAt = System.currentTimeMillis();
        DashboardAskRequest safeRequest = request == null ? new DashboardAskRequest() : request;
        Long resolvedTargetUserId = resolveTargetUserId(role, operatorUserId, targetUserId, safeRequest);

        DashboardAskPreloadedPayload mergedPayload = safeRequest.getPreloadedPayload();
        String preloadSource = mergedPayload != null ? "request" : "none";

        if (mergedPayload == null) {
            String pageName = nonBlank(extractPageName(safeRequest.getClientContext()),
                    ROLE_USER.equalsIgnoreCase(role) ? "useroverview" : "adminoverview");

            DashboardAskPreloadedPayload cachedPayload = dashboardPreloadService.getCached(
                    role, operatorUserId, resolvedTargetUserId, pageName, safeRequest.getObjectRef()
            );
            if (cachedPayload != null) {
                mergedPayload = cachedPayload;
                preloadSource = "redis";
            } else {
                mergedPayload = dashboardPreloadService.preload(
                        role,
                        operatorUserId,
                        resolvedTargetUserId,
                        pageName,
                        safeRequest.getObjectRef(),
                        safeMap(safeRequest.getContext())
                );
                preloadSource = mergedPayload != null ? "database" : "none";
            }
        }

        Map<String, Object> learningContext = resolveLearningContext(
                role, operatorUserId, resolvedTargetUserId, safeRequest, mergedPayload
        );
        Map<String, Object> mergedContext = dashboardAskContextResolver.resolve(
                safeRequest, mergedPayload, learningContext
        );

        DashboardAskDecisionResult decision = safeDecision(dashboardAskDecisionService.decide(
                DashboardAskDecisionRequest.builder()
                        .role(role)
                        .operatorUserId(operatorUserId)
                        .targetUserId(resolvedTargetUserId)
                        .query(safeString(safeRequest.getQuery()))
                        .responseLanguage(resolveResponseLanguage(safeRequest))
                        .askScene(safeString(safeRequest.getAskScene()))
                        .responseMode(safeString(safeRequest.getResponseMode()))
                        .objectRef(safeRequest.getObjectRef())
                        .preloadedPayload(mergedPayload)
                        .clientContext(safeRequest.getClientContext())
                        .context(mergeContext(safeMap(safeRequest.getContext()), mergedContext))
                        .learningContext(learningContext)
                        .questionContext(resolveQuestionContext(mergedPayload, mergedContext))
                        .build()
        ));

        if (ACTION_DIRECT_ANSWER.equalsIgnoreCase(decision.getAction()) && Boolean.TRUE.equals(decision.getSufficient())) {
            DashboardAnswerComposeResult composed = composeDirectAnswer(
                    role, operatorUserId, resolvedTargetUserId, safeRequest, mergedPayload, learningContext, mergedContext
            );

            String finalAnswer = nonBlank(composed == null ? null : composed.getAnswer(), decision.getAnswer());
            List<String> finalSuggestions = composed != null && composed.getSuggestions() != null && !composed.getSuggestions().isEmpty()
                    ? composed.getSuggestions()
                    : safeList(decision.getSuggestions());

            return DashboardAssistantResponse.builder()
                    .answer(finalAnswer)
                    .data(buildDirectAnswerData(safeRequest, mergedPayload, learningContext, mergedContext))
                    .suggestions(finalSuggestions)
                    .meta(mergeMeta(
                            mapOfNullable(
                                    META_KEY_ANSWER_MODE, notBlank(composed == null ? null : composed.getAnswer()) ? ANSWER_MODE_AI_DIRECT : ANSWER_MODE_TEMPLATE_DIRECT,
                                    META_KEY_ASK_SCENE, safeString(safeRequest.getAskScene()),
                                    META_KEY_ACTION, safeString(decision.getAction()),
                                    META_KEY_SUFFICIENT, true,
                                    META_KEY_USED_PRELOAD, mergedPayload != null,
                                    META_KEY_PRELOAD_SOURCE, preloadSource,
                                    META_KEY_ELAPSED_MS, System.currentTimeMillis() - startedAt,
                                    META_KEY_REVIEW_SUMMARY, safeString(decision.getReviewSummary())
                            ),
                            decision.getMeta()
                    ))
                    .build();
        }

        if (ACTION_NEED_CLARIFICATION.equalsIgnoreCase(decision.getAction())
                || ACTION_EXIT.equalsIgnoreCase(decision.getAction())) {
            return DashboardAssistantResponse.builder()
                    .answer(nonBlank(decision.getAnswer(),
                            ACTION_EXIT.equalsIgnoreCase(decision.getAction()) ? "目前此問題不在支援範圍。" : "我需要更多資訊，例如題目、紀錄或時間範圍。"))
                    .data(mapOfNullable(
                            META_KEY_ASK_SCENE, safeString(safeRequest.getAskScene()),
                            "objectRef", safeRequest.getObjectRef()
                    ))
                    .suggestions(safeList(decision.getSuggestions()))
                    .meta(mergeMeta(
                            mapOfNullable(
                                    META_KEY_ANSWER_MODE, ANSWER_MODE_CLARIFICATION,
                                    META_KEY_SUFFICIENT, false,
                                    META_KEY_USED_PRELOAD, mergedPayload != null,
                                    META_KEY_PRELOAD_SOURCE, preloadSource,
                                    META_KEY_ELAPSED_MS, System.currentTimeMillis() - startedAt,
                                    META_KEY_ACTION, safeString(decision.getAction()),
                                    META_KEY_REVIEW_SUMMARY, safeString(decision.getReviewSummary())
                            ),
                            decision.getMeta()
                    ))
                    .build();
        }

        if (!shouldQueryDatabase(decision, mergedPayload, safeRequest)) {
            DashboardAnswerComposeResult composed = composeDirectAnswer(
                    role, operatorUserId, resolvedTargetUserId, safeRequest, mergedPayload, learningContext, mergedContext
            );
            return DashboardAssistantResponse.builder()
                    .answer(nonBlank(composed == null ? null : composed.getAnswer(), "我先根據目前快照整理可用資訊。"))
                    .data(buildDirectAnswerData(safeRequest, mergedPayload, learningContext, mergedContext))
                    .suggestions(composed == null ? new ArrayList<>() : safeList(composed.getSuggestions()))
                    .meta(mapOfNullable(
                            META_KEY_ANSWER_MODE, ANSWER_MODE_TEMPLATE_DIRECT,
                            META_KEY_ACTION, "REDIS_DIRECT_FALLBACK",
                            META_KEY_USED_PRELOAD, mergedPayload != null,
                            META_KEY_PRELOAD_SOURCE, preloadSource,
                            META_KEY_ELAPSED_MS, System.currentTimeMillis() - startedAt,
                            META_KEY_REVIEW_SUMMARY, "Redis snapshot is sufficient enough to avoid database fallback."
                    ))
                    .build();
        }

        DashboardIntentParseResult fallbackIntent = toFallbackStructuredIntent(role, resolvedTargetUserId, decision);
        permissionValidator.validate(role, operatorUserId, fallbackIntent);

        return dashboardStructuredAiQueryService.execute(
                role,
                operatorUserId,
                resolvedTargetUserId,
                safeRequest.getQuery(),
                fallbackIntent,
                mergeContext(
                        mergedContext,
                        mapOfNullable(
                                META_KEY_ASK_SCENE, safeRequest.getAskScene(),
                                "responseMode", safeRequest.getResponseMode(),
                                "objectRef", safeRequest.getObjectRef(),
                                "preloadedPayload", mergedPayload,
                                "clientContext", safeRequest.getClientContext(),
                                "learningContext", learningContext,
                                "questionContext", resolveQuestionContext(mergedPayload, mergedContext),
                                "decisionReviewSummary", decision.getReviewSummary(),
                                "requiredDataScopes", safeList(decision.getRequiredDataScopes()),
                                META_KEY_ANSWER_MODE, ANSWER_MODE_FALLBACK_SQL,
                                META_KEY_ACTION, nonBlank(decision.getAction(), ACTION_GENERATE_SQL),
                                META_KEY_PRELOAD_SOURCE, preloadSource
                        )
                )
        );
    }

    private Map<String, Object> resolveLearningContext(String role,
                                                       Long operatorUserId,
                                                       Long targetUserId,
                                                       DashboardAskRequest request,
                                                       DashboardAskPreloadedPayload payload) {
        if (payload != null && payload.getLearningContext() != null && !payload.getLearningContext().isEmpty()) {
            return safeMap(payload.getLearningContext());
        }
        if (request == null || request.getObjectRef() == null || !notBlank(request.getObjectRef().getModule())) {
            return new LinkedHashMap<>();
        }
        return safeMap(dashboardLearningContextService.buildLearningContext(
                role, operatorUserId, targetUserId, safeString(request.getAskScene()), request.getObjectRef()
        ));
    }

    private Map<String, Object> resolveQuestionContext(DashboardAskPreloadedPayload payload,
                                                       Map<String, Object> mergedContext) {
        if (payload != null && payload.getQuestionContext() != null && !payload.getQuestionContext().isEmpty()) {
            return safeMap(payload.getQuestionContext());
        }
        return safeMap(mergedContext);
    }

    private boolean shouldQueryDatabase(DashboardAskDecisionResult decision,
                                        DashboardAskPreloadedPayload payload,
                                        DashboardAskRequest request) {
        if (decision == null || !ACTION_GENERATE_SQL.equalsIgnoreCase(decision.getAction())) {
            return false;
        }
        if (request != null && request.getObjectRef() != null && notBlank(request.getObjectRef().getModule())) {
            return true;
        }
        List<String> requiredScopes = safeList(decision.getRequiredDataScopes());
        List<String> availableScopes = payload == null ? new ArrayList<>() : safeList(payload.getAvailableScopes());
        for (String requiredScope : requiredScopes) {
            if (!availableScopes.contains(requiredScope)) {
                return true;
            }
        }
        return false;
    }

    private DashboardAnswerComposeResult composeDirectAnswer(String role,
                                                             Long operatorUserId,
                                                             Long targetUserId,
                                                             DashboardAskRequest request,
                                                             DashboardAskPreloadedPayload preloadedPayload,
                                                             Map<String, Object> learningContext,
                                                             Map<String, Object> questionContext) {
        Object data = buildDirectAnswerData(request, preloadedPayload, learningContext, questionContext);
        return dashboardAnswerComposeService.compose(
                DashboardAnswerComposeRequest.builder()
                        .role(role)
                        .operatorUserId(operatorUserId)
                        .targetUserId(targetUserId)
                        .originalQuery(request == null ? null : request.getQuery())
                        .capability(CAPABILITY_PRELOADED_DIRECT)
                        .filters(mapOfNullable(
                                META_KEY_ASK_SCENE, request == null ? null : request.getAskScene(),
                                "pageName", extractPageName(request == null ? null : request.getClientContext())
                        ))
                        .data(data)
                        .responseLanguage(resolveResponseLanguage(request))
                        .build()
        );
    }

    private Object buildDirectAnswerData(DashboardAskRequest request,
                                         DashboardAskPreloadedPayload preloadedPayload,
                                         Map<String, Object> learningContext,
                                         Map<String, Object> questionContext) {
        Map<String, Object> result = new LinkedHashMap<>();
        putIfPresent(result, "query", request == null ? null : request.getQuery());
        putIfPresent(result, META_KEY_ASK_SCENE, request == null ? null : request.getAskScene());
        putIfPresent(result, "responseMode", request == null ? null : request.getResponseMode());
        putIfPresent(result, "objectRef", request == null ? null : request.getObjectRef());
        putIfPresent(result, "clientContext", request == null ? null : request.getClientContext());
        putIfPresent(result, "questionContext", questionContext);
        putIfPresent(result, "learningContext", learningContext);
        putIfPresent(result, "preloadedPayload", preloadedPayload);

        if (preloadedPayload != null) {
            putIfPresent(result, "overview", preloadedPayload.getOverview());
            putIfPresent(result, "progressSummary", preloadedPayload.getProgressSummary());
            putIfPresent(result, "recentRecords", preloadedPayload.getRecentRecords());
            putIfPresent(result, "moduleStats", preloadedPayload.getModuleStats());
            putIfPresent(result, "recentQuestions", preloadedPayload.getRecentQuestions());
            putIfPresent(result, "recentPassages", preloadedPayload.getRecentPassages());
            putIfPresent(result, "aggregates", preloadedPayload.getAggregates());
        }

        return result.isEmpty() ? null : result;
    }

    private DashboardIntentParseResult toFallbackStructuredIntent(String role,
                                                                  Long targetUserId,
                                                                  DashboardAskDecisionResult decision) {
        DashboardIntentParseResult result = new DashboardIntentParseResult();
        result.setSuccess(Boolean.TRUE);
        result.setCapability(resolveCapability(nonBlank(decision == null ? null : decision.getCapability(), CAPABILITY_STRUCTURED_QUERY)));
        result.setQueryMode(DashboardIntentQueryMode.STRUCTURED_QUERY);
        result.setTargetScope(resolveFallbackTargetScope(role, targetUserId));
        result.setTargetUserId(targetUserId);
        result.setFilters(new LinkedHashMap<>(safeMap(decision == null ? null : decision.getFilters())));
        result.setClarificationQuestion(null);
        result.setReasoningSummary(safeString(decision == null ? null : decision.getReviewSummary()));
        result.setConfidence(0.6D);
        result.setSuggestions(new ArrayList<>(safeList(decision == null ? null : decision.getSuggestions())));
        return result;
    }

    private DashboardIntentTargetScope resolveFallbackTargetScope(String role, Long targetUserId) {
        if (ROLE_USER.equalsIgnoreCase(role)) {
            return DashboardIntentTargetScope.SELF;
        }
        return targetUserId != null ? DashboardIntentTargetScope.SPECIFIC_USER : DashboardIntentTargetScope.GLOBAL;
    }

    private DashboardIntentCapability resolveCapability(String capability) {
        if (!notBlank(capability)) {
            return DashboardIntentCapability.STRUCTURED_QUERY;
        }
        try {
            return DashboardIntentCapability.valueOf(capability.trim().toUpperCase());
        } catch (Exception ex) {
            log.warn("Unknown capability={}, fallback to STRUCTURED_QUERY", capability);
            return DashboardIntentCapability.STRUCTURED_QUERY;
        }
    }

    private DashboardAskDecisionResult safeDecision(DashboardAskDecisionResult decision) {
        if (decision != null) {
            if (decision.getFilters() == null) {
                decision.setFilters(new LinkedHashMap<>());
            }
            if (decision.getRequiredDataScopes() == null) {
                decision.setRequiredDataScopes(new ArrayList<>());
            }
            if (decision.getSuggestions() == null) {
                decision.setSuggestions(new ArrayList<>());
            }
            if (decision.getMeta() == null) {
                decision.setMeta(new LinkedHashMap<>());
            }
            return decision;
        }

        DashboardAskDecisionResult fallback = new DashboardAskDecisionResult();
        fallback.setAction(ACTION_GENERATE_SQL);
        fallback.setSufficient(Boolean.FALSE);
        fallback.setAnswer(null);
        fallback.setCapability(CAPABILITY_STRUCTURED_QUERY);
        fallback.setFilters(new LinkedHashMap<>());
        fallback.setReviewSummary("ask decision returned null");
        fallback.setRequiredDataScopes(new ArrayList<>());
        fallback.setSuggestions(new ArrayList<>());
        fallback.setMeta(new LinkedHashMap<>());
        return fallback;
    }

    private Long resolveTargetUserId(String role, Long operatorUserId, Long targetUserId, DashboardAskRequest request) {
        if (ROLE_USER.equalsIgnoreCase(role)) {
            return operatorUserId;
        }
        if (request != null && request.getTargetUserId() != null) {
            return request.getTargetUserId();
        }
        return targetUserId;
    }

    private String resolveResponseLanguage(DashboardAskRequest request) {
        if (request != null && request.getClientContext() != null && notBlank(request.getClientContext().getLocale())) {
            String locale = request.getClientContext().getLocale().trim().toLowerCase();
            if (locale.startsWith("zh")) {
                return RESPONSE_LANGUAGE_ZH_HANT;
            }
            return RESPONSE_LANGUAGE_EN;
        }
        return detectResponseLanguage(request == null ? null : request.getQuery());
    }

    private String detectResponseLanguage(String query) {
        if (!notBlank(query)) {
            return RESPONSE_LANGUAGE_ZH_HANT;
        }
        for (char ch : query.toCharArray()) {
            if (Character.UnicodeScript.of(ch) == Character.UnicodeScript.HAN) {
                return RESPONSE_LANGUAGE_ZH_HANT;
            }
        }
        return RESPONSE_LANGUAGE_EN;
    }

    private String extractPageName(DashboardAskClientContext clientContext) {
        return clientContext == null ? null : clientContext.getPageName();
    }

    private Map<String, Object> mergeContext(Map<String, Object> base, Map<String, Object> overlay) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (base != null && !base.isEmpty()) {
            result.putAll(base);
        }
        if (overlay != null && !overlay.isEmpty()) {
            result.putAll(overlay);
        }
        return result;
    }

    private Map<String, Object> mergeMeta(Map<String, Object> base, Map<String, Object> ext) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (base != null && !base.isEmpty()) {
            result.putAll(base);
        }
        if (ext != null && !ext.isEmpty()) {
            result.putAll(ext);
        }
        return result;
    }

    private Map<String, Object> safeMap(Map<String, Object> source) {
        return source == null ? new LinkedHashMap<>() : new LinkedHashMap<>(source);
    }

    private List<String> safeList(List<String> source) {
        return source == null ? new ArrayList<>() : new ArrayList<>(source);
    }

    private Map<String, Object> mapOfNullable(Object... keyValues) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (keyValues == null || keyValues.length == 0) {
            return result;
        }
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            Object key = keyValues[i];
            Object value = keyValues[i + 1];
            if (key instanceof String stringKey) {
                putIfPresent(result, stringKey, value);
            }
        }
        return result;
    }

    private void putIfPresent(Map<String, Object> target, String key, Object value) {
        if (target == null || !notBlank(key) || value == null) {
            return;
        }
        if (value instanceof String text) {
            if (notBlank(text)) {
                target.put(key, text.trim());
            }
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

    private boolean notBlank(String text) {
        return text != null && !text.isBlank();
    }

    private String nonBlank(String first, String fallback) {
        return notBlank(first) ? first.trim() : fallback;
    }

    private String safeString(String text) {
        return text == null ? null : text.trim();
    }
}
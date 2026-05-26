package com.andrew.smartielts.dashboard.preload.impl;

import com.andrew.smartielts.dashboard.agent.DashboardAgentContext;
import com.andrew.smartielts.dashboard.agent.DashboardCapability;
import com.andrew.smartielts.dashboard.agent.DashboardCapabilityRouter;
import com.andrew.smartielts.dashboard.controller.dto.DashboardAskObjectRef;
import com.andrew.smartielts.dashboard.controller.dto.DashboardAskPreloadedPayload;
import com.andrew.smartielts.dashboard.learning.DashboardLearningContextService;
import com.andrew.smartielts.dashboard.preload.DashboardPreloadCacheService;
import com.andrew.smartielts.dashboard.preload.DashboardPreloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardPreloadServiceImpl implements DashboardPreloadService {

    private static final long PAGE_TTL_MILLIS = 3 * 60 * 1000L;
    private static final long DETAIL_TTL_MILLIS = 10 * 60 * 1000L;

    private static final String ROLE_USER = "USER";
    private static final String PAGE_NAME_DETAIL = "detail";

    private static final String SCOPE_PRELOAD_OVERVIEW = "preload_overview";
    private static final String SCOPE_PROGRESS_SUMMARY = "progress_summary";
    private static final String SCOPE_RECENT_RECORDS = "recent_records";
    private static final String SCOPE_MODULE_STATS = "module_stats";
    private static final String SCOPE_LEARNING_CONTEXT = "learning_context";
    private static final String SCOPE_QUESTION_CONTEXT = "question_context";

    private final DashboardCapabilityRouter capabilityRouter;
    private final DashboardLearningContextService dashboardLearningContextService;
    private final DashboardPreloadCacheService dashboardPreloadCacheService;

    @Qualifier("dashboardSseExecutor")
    private final Executor dashboardSseExecutor;

    @Override
    public DashboardAskPreloadedPayload preload(String role,
                                                Long operatorUserId,
                                                Long targetUserId,
                                                String pageName,
                                                DashboardAskObjectRef objectRef,
                                                Map<String, Object> context) {

        String cacheKey = buildCacheKey(role, operatorUserId, targetUserId, pageName, objectRef, context);
        DashboardAskPreloadedPayload cached = dashboardPreloadCacheService.get(cacheKey);
        if (cached != null) {
            DashboardAskPreloadedPayload copied = copyPayload(cached);
            copied.setPreloadSource("redis");
            return copied;
        }

        DashboardAskPreloadedPayload payload = buildPayload(
                role, operatorUserId, targetUserId, pageName, objectRef, context
        );
        payload.setPreloadSource("database");

        dashboardPreloadCacheService.put(cacheKey, payload, chooseTtl(pageName, objectRef));
        return copyPayload(payload);
    }

    @Override
    public void preloadAsync(String role,
                             Long operatorUserId,
                             Long targetUserId,
                             String pageName,
                             DashboardAskObjectRef objectRef,
                             Map<String, Object> context) {
        dashboardSseExecutor.execute(() -> {
            try {
                preload(role, operatorUserId, targetUserId, pageName, objectRef, context);
            } catch (Exception e) {
                log.warn("Dashboard preload async failed, role={}, operatorUserId={}, targetUserId={}, pageName={}, reason={}",
                        role, operatorUserId, targetUserId, pageName, e.getMessage());
            }
        });
    }

    @Override
    public DashboardAskPreloadedPayload getCached(String role,
                                                  Long operatorUserId,
                                                  Long targetUserId,
                                                  String pageName,
                                                  DashboardAskObjectRef objectRef,
                                                  Map<String, Object> context) {
        String cacheKey = buildCacheKey(role, operatorUserId, targetUserId, pageName, objectRef, context);
        DashboardAskPreloadedPayload payload = dashboardPreloadCacheService.get(cacheKey);
        if (payload == null) {
            return null;
        }
        DashboardAskPreloadedPayload copied = copyPayload(payload);
        copied.setPreloadSource("redis");
        return copied;
    }

    @Override
    public void evict(String role,
                      Long operatorUserId,
                      Long targetUserId,
                      String pageName,
                      DashboardAskObjectRef objectRef) {
        dashboardPreloadCacheService.evict(buildCacheKey(role, operatorUserId, targetUserId, pageName, objectRef, null));
    }

    private DashboardAskPreloadedPayload buildPayload(String role,
                                                      Long operatorUserId,
                                                      Long targetUserId,
                                                      String pageName,
                                                      DashboardAskObjectRef objectRef,
                                                      Map<String, Object> context) {

        Long effectiveTargetUserId = ROLE_USER.equalsIgnoreCase(role)
                ? operatorUserId
                : (targetUserId != null ? targetUserId : operatorUserId);

        Map<String, Object> learningContext = safeMap(
                dashboardLearningContextService.buildLearningContext(
                        role, operatorUserId, effectiveTargetUserId, pageName, objectRef
                )
        );

        DashboardAskPreloadedPayload payload = new DashboardAskPreloadedPayload();
        payload.setSnapshotId(UUID.randomUUID().toString());
        payload.setSnapshotTime(OffsetDateTime.now().toString());

        if (!isDetailOnlyPage(pageName)) {
            payload.setOverview(loadCapability(role, operatorUserId, effectiveTargetUserId, resolveOverviewCapability(role)));
            payload.setProgressSummary(loadCapability(role, operatorUserId, effectiveTargetUserId, resolveProgressCapability(role)));
            payload.setRecentRecords(castList(loadCapability(role, operatorUserId, effectiveTargetUserId, resolveRecentRecordsCapability(role))));
            payload.setModuleStats(castList(loadCapability(role, operatorUserId, effectiveTargetUserId, resolveModuleStatsCapability(role))));
        }

        payload.setRecentQuestions(extractRecentQuestions(learningContext));
        payload.setRecentPassages(extractRecentPassages(learningContext));
        payload.setAggregates(buildAggregates(payload, learningContext, context, pageName, role, effectiveTargetUserId));

        payload.setLearningContext(learningContext);
        payload.setQuestionContext(extractQuestionContext(learningContext));
        payload.setAvailableScopes(buildAvailableScopes(payload));
        payload.setPreloadSource("database");

        return payload;
    }

    private List<String> buildAvailableScopes(DashboardAskPreloadedPayload payload) {
        List<String> scopes = new ArrayList<>();
        if (payload.getOverview() != null) {
            scopes.add(SCOPE_PRELOAD_OVERVIEW);
        }
        if (payload.getProgressSummary() != null) {
            scopes.add(SCOPE_PROGRESS_SUMMARY);
        }
        if (payload.getRecentRecords() != null) {
            scopes.add(SCOPE_RECENT_RECORDS);
        }
        if (payload.getModuleStats() != null) {
            scopes.add(SCOPE_MODULE_STATS);
        }
        if (payload.getLearningContext() != null && !payload.getLearningContext().isEmpty()) {
            scopes.add(SCOPE_LEARNING_CONTEXT);
        }
        if (payload.getQuestionContext() != null && !payload.getQuestionContext().isEmpty()) {
            scopes.add(SCOPE_QUESTION_CONTEXT);
        }
        return scopes;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractQuestionContext(Map<String, Object> learningContext) {
        if (learningContext == null || learningContext.isEmpty()) {
            return new LinkedHashMap<>();
        }
        Object value = learningContext.get("questionContext");
        if (value instanceof Map<?, ?> map && !map.isEmpty()) {
            return new LinkedHashMap<>((Map<String, Object>) map);
        }
        return new LinkedHashMap<>(learningContext);
    }

    private long chooseTtl(String pageName, DashboardAskObjectRef objectRef) {
        return isDetailOnlyPage(pageName) || objectRef != null ? DETAIL_TTL_MILLIS : PAGE_TTL_MILLIS;
    }

    private boolean isDetailOnlyPage(String pageName) {
        return pageName != null && pageName.trim().toLowerCase().contains(PAGE_NAME_DETAIL);
    }

    private DashboardAskPreloadedPayload copyPayload(DashboardAskPreloadedPayload source) {
        DashboardAskPreloadedPayload target = new DashboardAskPreloadedPayload();
        BeanUtils.copyProperties(source, target);
        target.setAggregates(safeMap(source.getAggregates()));
        target.setLearningContext(safeMap(source.getLearningContext()));
        target.setQuestionContext(safeMap(source.getQuestionContext()));
        target.setAvailableScopes(source.getAvailableScopes() == null ? new ArrayList<>() : new ArrayList<>(source.getAvailableScopes()));
        target.setRecentQuestions(source.getRecentQuestions() == null ? new ArrayList<>() : new ArrayList<>(source.getRecentQuestions()));
        target.setRecentPassages(source.getRecentPassages() == null ? new ArrayList<>() : new ArrayList<>(source.getRecentPassages()));
        return target;
    }

    private Map<String, Object> safeMap(Map<String, Object> source) {
        return source == null ? new LinkedHashMap<>() : new LinkedHashMap<>(source);
    }

    @SuppressWarnings("unchecked")
    private List<Object> castList(Object data) {
        if (data instanceof List<?> list) {
            return new ArrayList<>((List<Object>) list);
        }
        return new ArrayList<>();
    }

    private DashboardCapability resolveOverviewCapability(String role) {
        return ROLE_USER.equalsIgnoreCase(role)
                ? DashboardCapability.USER_SELF_OVERVIEW
                : DashboardCapability.ADMIN_GLOBAL_OVERVIEW;
    }

    private DashboardCapability resolveProgressCapability(String role) {
        return ROLE_USER.equalsIgnoreCase(role)
                ? DashboardCapability.USER_SELF_PROGRESS_SUMMARY
                : DashboardCapability.ADMIN_USER_COUNT;
    }

    private DashboardCapability resolveRecentRecordsCapability(String role) {
        return ROLE_USER.equalsIgnoreCase(role)
                ? DashboardCapability.USER_SELF_RECENT_RECORDS
                : DashboardCapability.ADMIN_RECENT_ISSUES;
    }

    private DashboardCapability resolveModuleStatsCapability(String role) {
        return ROLE_USER.equalsIgnoreCase(role)
                ? DashboardCapability.USER_SELF_MODULE_STATS
                : DashboardCapability.ADMIN_MODULE_STATS;
    }

    private Object loadCapability(String role, Long operatorUserId, Long targetUserId, DashboardCapability capability) {
        return capabilityRouter.route(capability, DashboardAgentContext.builder()
                .role(role)
                .operatorUserId(operatorUserId)
                .targetUserId(targetUserId)
                .filters(new LinkedHashMap<>())
                .build());
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractRecentQuestions(Map<String, Object> learningContext) {
        if (learningContext == null || learningContext.isEmpty()) {
            return List.<Map<String, Object>>of();
        }

        Object questions = learningContext.get("recordQuestions");
        if (!(questions instanceof List<?> list) || list.isEmpty()) {
            return List.<Map<String, Object>>of();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> map && !map.isEmpty()) {
                result.add(new LinkedHashMap<>((Map<String, Object>) map));
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractRecentPassages(Map<String, Object> learningContext) {
        if (learningContext == null || learningContext.isEmpty()) {
            return List.<Map<String, Object>>of();
        }

        Object passage = learningContext.get("passage");
        if (!(passage instanceof Map<?, ?> map) || map.isEmpty()) {
            return List.<Map<String, Object>>of();
        }

        Map<String, Object> row = new LinkedHashMap<>((Map<String, Object>) map);
        return List.of(row);
    }

    private Map<String, Object> buildAggregates(DashboardAskPreloadedPayload payload,
                                                Map<String, Object> learningContext,
                                                Map<String, Object> context,
                                                String pageName,
                                                String role,
                                                Long effectiveTargetUserId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("page_name", pageName);
        result.put("role", role);
        result.put("target_user_id", effectiveTargetUserId);
        result.put("has_learning_context", learningContext != null && !learningContext.isEmpty());
        result.put("has_question_context", payload.getQuestionContext() != null && !payload.getQuestionContext().isEmpty());
        if (context != null && !context.isEmpty()) {
            result.put("request_context", context);
        }
        return result;
    }

    private String buildCacheKey(String role,
                                 Long operatorUserId,
                                 Long targetUserId,
                                 String pageName,
                                 DashboardAskObjectRef objectRef,
                                 Map<String, Object> context) {
        String module = objectRef == null ? "none" : safe(objectRef.getModule());
        String objectType = objectRef == null ? "none" : safe(objectRef.getObjectType());
        String recordId = objectRef == null || objectRef.getRecordId() == null ? "none" : String.valueOf(objectRef.getRecordId());
        String questionId = objectRef == null || objectRef.getQuestionId() == null ? "none" : String.valueOf(objectRef.getQuestionId());
        String timeRange = context == null ? "none" : safe(String.valueOf(context.getOrDefault("timeRange", "none")));
        return String.join(":",
                safe(role),
                String.valueOf(operatorUserId),
                String.valueOf(targetUserId),
                safe(pageName),
                module,
                objectType,
                recordId,
                questionId,
                timeRange
        );
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "none" : value.trim().toLowerCase();
    }
}

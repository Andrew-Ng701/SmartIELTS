package com.andrew.smartielts.dashboard.query.impl;

import com.andrew.smartielts.dashboard.agent.answer.DashboardAnswerComposeService;
import com.andrew.smartielts.dashboard.agent.answer.DashboardAnswerReviewAction;
import com.andrew.smartielts.dashboard.agent.answer.DashboardAnswerReviewService;
import com.andrew.smartielts.dashboard.agent.answer.DashboardUserTargetScoreContext;
import com.andrew.smartielts.dashboard.agent.answer.dto.DashboardAnswerComposeRequest;
import com.andrew.smartielts.dashboard.agent.answer.dto.DashboardAnswerComposeResult;
import com.andrew.smartielts.dashboard.agent.answer.dto.DashboardAnswerReviewRequest;
import com.andrew.smartielts.dashboard.agent.answer.dto.DashboardAnswerReviewResult;
import com.andrew.smartielts.dashboard.agent.intent.DashboardIntentCapability;
import com.andrew.smartielts.dashboard.agent.intent.dto.DashboardIntentParseResult;
import com.andrew.smartielts.dashboard.controller.dto.DashboardAssistantResponse;
import com.andrew.smartielts.dashboard.query.DashboardSqlGenerationService;
import com.andrew.smartielts.dashboard.query.DashboardStructuredAiQueryService;
import com.andrew.smartielts.dashboard.query.SecureDashboardQueryRequest;
import com.andrew.smartielts.dashboard.query.SecureDashboardQueryService;
import com.andrew.smartielts.dashboard.query.dto.DashboardSqlGenerationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardStructuredAiQueryServiceImpl implements DashboardStructuredAiQueryService {

    private static final String ANSWER_MODE_AI_SQL_SUCCESS = "AI_SQL_SUCCESS";
    private static final String ANSWER_MODE_AI_SQL_EMPTY_RESULT = "AI_SQL_EMPTY_RESULT";
    private static final String ANSWER_MODE_AI_SQL_GENERATION_FAILED = "AI_SQL_GENERATION_FAILED";
    private static final String ANSWER_MODE_AI_SQL_REVIEW_EXIT = "AI_SQL_REVIEW_EXIT";
    private static final String ANSWER_MODE_AI_SQL_REVIEW_RETRY = "AI_SQL_REVIEW_RETRY";
    private static final String ANSWER_MODE_AI_SQL_COMPOSE_FALLBACK = "AI_SQL_COMPOSE_FALLBACK";

    private static final String META_KEY_ANSWER_MODE = "answer_mode";
    private static final String META_KEY_QUERY_PURPOSE = "query_purpose";
    private static final String META_KEY_CONFIDENCE = "confidence";
    private static final String META_KEY_ROW_COUNT = "row_count";
    private static final String META_KEY_REASONING_SUMMARY = "reasoning_summary";
    private static final String META_KEY_REVIEW_SUMMARY = "review_summary";
    private static final String META_KEY_RETRIED = "retried";

    private final DashboardSqlGenerationService dashboardSqlGenerationService;
    private final SecureDashboardQueryService secureDashboardQueryService;
    private final DashboardAnswerComposeService dashboardAnswerComposeService;
    private final DashboardAnswerReviewService dashboardAnswerReviewService;

    @Override
    public DashboardAssistantResponse execute(String role,
                                              Long operatorUserId,
                                              Long targetUserId,
                                              String originalQuery,
                                              DashboardIntentParseResult intent,
                                              Map<String, Object> context) {
        long startedAt = System.currentTimeMillis();

        DashboardStructuredExecutionResult executionResult = run_once(
                role,
                operatorUserId,
                targetUserId,
                originalQuery,
                intent,
                context
        );

        DashboardAnswerReviewResult reviewResult = review_current_result(
                role,
                operatorUserId,
                targetUserId,
                originalQuery,
                intent,
                executionResult.rows()
        );

        if (reviewResult.getAction() == DashboardAnswerReviewAction.RETRY_QUERY
                && reviewResult.getRetryFilters() != null
                && !reviewResult.getRetryFilters().isEmpty()) {
            DashboardIntentParseResult retriedIntent = cloneWithRetryFilters(intent, reviewResult.getRetryFilters());

            DashboardStructuredExecutionResult retriedExecutionResult = run_once(
                    role,
                    operatorUserId,
                    targetUserId,
                    originalQuery,
                    retriedIntent,
                    context
            );

            DashboardAnswerReviewResult retriedReviewResult = review_current_result(
                    role,
                    operatorUserId,
                    targetUserId,
                    originalQuery,
                    retriedIntent,
                    retriedExecutionResult.rows()
            );

            if (retriedReviewResult.getAction() == DashboardAnswerReviewAction.EXIT) {
                return build_exit_response(
                        retriedReviewResult,
                        retriedExecutionResult.sqlPlan(),
                        retriedExecutionResult.rows(),
                        true
                );
            }

            return buildSuccessResponse(
                    role,
                    operatorUserId,
                    targetUserId,
                    originalQuery,
                    retriedIntent,
                    retriedExecutionResult,
                    retriedReviewResult,
                    true,
                    startedAt,
                    context
            );
        }

        if (reviewResult.getAction() == DashboardAnswerReviewAction.EXIT) {
            return build_exit_response(
                    reviewResult,
                    executionResult.sqlPlan(),
                    executionResult.rows(),
                    false
            );
        }

        return buildSuccessResponse(
                role,
                operatorUserId,
                targetUserId,
                originalQuery,
                intent,
                executionResult,
                reviewResult,
                false,
                startedAt,
                context
        );
    }

    private DashboardStructuredExecutionResult run_once(String role,
                                                        Long operatorUserId,
                                                        Long targetUserId,
                                                        String originalQuery,
                                                        DashboardIntentParseResult intent,
                                                        Map<String, Object> context) {
        DashboardSqlGenerationResult sqlPlan = dashboardSqlGenerationService.generate(
                role,
                operatorUserId,
                targetUserId,
                originalQuery,
                intent,
                context
        );

        if (sqlPlan == null) {
            throw new IllegalStateException("dashboard_sql_generation_failed: null_sql_plan");
        }
        if (!Boolean.TRUE.equals(sqlPlan.getSuccess())) {
            throw new IllegalStateException(
                    "dashboard_sql_generation_failed: " + sqlPlan.getReasoningSummary()
            );
        }
        if (sqlPlan.getSql() == null || sqlPlan.getSql().isBlank()) {
            throw new IllegalStateException("dashboard_sql_generation_failed: empty_sql");
        }

        SecureDashboardQueryRequest secureRequest = new SecureDashboardQueryRequest();
        secureRequest.setRole(role);
        secureRequest.setOperatorUserId(operatorUserId);
        secureRequest.setTargetUserId(targetUserId);
        secureRequest.setIntentCapability(
                intent != null && intent.getCapability() != null ? intent.getCapability().name() : null
        );
        secureRequest.setRawSql(sqlPlan.getSql());
        secureRequest.setParams(sqlPlan.getParams());
        secureRequest.setExpectedColumns(sqlPlan.getExpectedColumns());
        secureRequest.setAiGenerated(true);

        List<Map<String, Object>> rows = (List<Map<String, Object>>) secureDashboardQueryService.execute(secureRequest);

        return new DashboardStructuredExecutionResult(
                sqlPlan,
                rows == null ? List.of() : rows
        );
    }

    private DashboardAnswerReviewResult review_current_result(String role,
                                                              Long operatorUserId,
                                                              Long targetUserId,
                                                              String originalQuery,
                                                              DashboardIntentParseResult intent,
                                                              List<Map<String, Object>> rows) {
        return dashboardAnswerReviewService.review(
                DashboardAnswerReviewRequest.builder()
                        .role(role)
                        .operatorUserId(operatorUserId)
                        .targetUserId(targetUserId)
                        .originalQuery(originalQuery)
                        .capability(intent == null || intent.getCapability() == null ? null : intent.getCapability().name())
                        .filters(intent == null || intent.getFilters() == null ? Map.of() : intent.getFilters())
                        .data(rows)
                        .build()
        );
    }

    @SuppressWarnings("unchecked")
    private DashboardAssistantResponse buildSuccessResponse(
            String role,
            Long operatorUserId,
            Long targetUserId,
            String originalQuery,
            DashboardIntentParseResult intent,
            DashboardStructuredExecutionResult executionResult,
            DashboardAnswerReviewResult reviewResult,
            boolean retried,
            long startedAt,
            Map<String, Object> context) {

        Object reviewed = dashboardSqlGenerationService.reviewAndAnswer(
                role,
                operatorUserId,
                targetUserId,
                originalQuery,
                intent,
                executionResult.sqlPlan(),
                executionResult.rows(),
                context
        );

        Map<String, Object> reviewedMap = to_string_key_map(reviewed);
        Map<String, Object> reviewedMeta = to_string_key_map(reviewedMap.get("meta"));

        Object reviewedData = reviewedMap.containsKey("data")
                ? reviewedMap.get("data")
                : executionResult.rows();

        String reviewedAnswer = reviewedMap.containsKey("answer")
                ? safe(String.valueOf(reviewedMap.get("answer")))
                : "";

        List<String> reviewedSuggestions = to_string_list(reviewedMap.get("suggestions"));

        DashboardAnswerComposeResult composeResult = dashboardAnswerComposeService.compose(
                DashboardAnswerComposeRequest.builder()
                        .role(role)
                        .operatorUserId(operatorUserId)
                        .targetUserId(targetUserId)
                        .originalQuery(originalQuery)
                        .capability(intent != null && intent.getCapability() != null
                                ? intent.getCapability().name()
                                : DashboardIntentCapability.STRUCTURED_QUERY.name())
                        .filters(intent != null && intent.getFilters() != null ? intent.getFilters() : Map.of())
                        .userTargetScores(DashboardUserTargetScoreContext.fromContext(context))
                        .data(reviewedData)
                        .responseLanguage(detect_response_language(originalQuery))
                        .build()
        );

        String composedAnswer = composeResult == null ? "" : safe(composeResult.getAnswer());
        List<String> composedSuggestions = composeResult == null || composeResult.getSuggestions() == null
                ? List.of()
                : composeResult.getSuggestions();

        boolean useReviewedAnswer = has_text(reviewedAnswer) && !is_generic_sql_answer(reviewedAnswer);

        String finalAnswer = useReviewedAnswer
                ? reviewedAnswer
                : first_non_blank(
                composedAnswer,
                reviewedAnswer,
                executionResult.rows().isEmpty()
                        ? "目前沒有符合條件的資料。"
                        : "我已根據目前資料整理出結果。"
        );

        List<String> finalSuggestions = !composedSuggestions.isEmpty()
                ? composedSuggestions
                : reviewedSuggestions;

        String finalAnswerMode;
        if (executionResult.rows().isEmpty()) {
            finalAnswerMode = ANSWER_MODE_AI_SQL_EMPTY_RESULT;
        } else if (useReviewedAnswer) {
            finalAnswerMode = ANSWER_MODE_AI_SQL_SUCCESS;
        } else {
            finalAnswerMode = ANSWER_MODE_AI_SQL_COMPOSE_FALLBACK;
        }

        Map<String, Object> meta = new LinkedHashMap<>();
        if (!reviewedMeta.isEmpty()) {
            meta.putAll(reviewedMeta);
        }
        meta.put(META_KEY_ANSWER_MODE, finalAnswerMode);
        meta.put(META_KEY_QUERY_PURPOSE, safe(executionResult.sqlPlan().getQueryPurpose()));
        meta.put(META_KEY_CONFIDENCE,
                executionResult.sqlPlan().getConfidence() == null ? 0.0D : executionResult.sqlPlan().getConfidence());
        meta.put(META_KEY_ROW_COUNT, executionResult.rows().size());
        meta.put(META_KEY_REASONING_SUMMARY, safe(executionResult.sqlPlan().getReasoningSummary()));
        meta.put(META_KEY_REVIEW_SUMMARY, safe(reviewResult.getReviewSummary()));
        meta.put(META_KEY_RETRIED, retried);
        meta.put("answer_source", useReviewedAnswer ? "sql_review" : "compose_service");
        meta.put("elapsedMs", System.currentTimeMillis() - startedAt);

        return DashboardAssistantResponse.builder()
                .answer(finalAnswer)
                .data(reviewedData)
                .meta(meta)
                .suggestions(finalSuggestions == null ? List.of() : finalSuggestions)
                .build();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> to_string_key_map(Object source) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (!(source instanceof Map<?, ?> rawMap)) {
            return result;
        }
        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
            if (entry.getKey() != null) {
                result.put(String.valueOf(entry.getKey()), entry.getValue());
            }
        }
        return result;
    }

    private List<String> to_string_list(Object source) {
        if (!(source instanceof List<?> rawList) || rawList.isEmpty()) {
            return List.of();
        }
        return rawList.stream()
                .filter(item -> item != null && !String.valueOf(item).isBlank())
                .map(String::valueOf)
                .toList();
    }

    private boolean is_generic_sql_answer(String answer) {
        if (is_blank(answer)) {
            return true;
        }
        if (answer.contains("我已完成查詢") && answer.contains("相關資料結果")) {
            return true;
        }

        String normalized = answer.trim()
                .replace("。", "")
                .replace(".", "")
                .replace(" ", "")
                .toLowerCase();

        return "已查到符合條件的資料".equals(normalized)
                || "未查到符合條件的資料".equals(normalized)
                || "查到符合條件的資料".equals(normalized)
                || "no_data_matched".equals(normalized)
                || "data_found".equals(normalized)
                || "found_matching_data".equals(normalized);
    }

    private boolean has_text(String value) {
        return value != null && !value.isBlank();
    }

    private String first_non_blank(String... values) {
        if (values == null || values.length == 0) {
            return "";
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private DashboardAssistantResponse build_exit_response(DashboardAnswerReviewResult reviewResult,
                                                           DashboardSqlGenerationResult sqlPlan,
                                                           List<Map<String, Object>> rows,
                                                           boolean retried) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put(META_KEY_ANSWER_MODE, retried ? ANSWER_MODE_AI_SQL_REVIEW_RETRY : ANSWER_MODE_AI_SQL_REVIEW_EXIT);
        meta.put(META_KEY_QUERY_PURPOSE, sqlPlan == null ? "structured_dashboard_query" : safe(sqlPlan.getQueryPurpose()));
        meta.put(META_KEY_CONFIDENCE, sqlPlan == null || sqlPlan.getConfidence() == null ? 0.0D : sqlPlan.getConfidence());
        meta.put(META_KEY_ROW_COUNT, rows == null ? 0 : rows.size());
        meta.put(META_KEY_REASONING_SUMMARY, sqlPlan == null ? "" : safe(sqlPlan.getReasoningSummary()));
        meta.put(META_KEY_REVIEW_SUMMARY, safe(reviewResult.getReviewSummary()));
        meta.put(META_KEY_RETRIED, retried);

        return DashboardAssistantResponse.builder()
                .answer(reviewResult.getExitMessage() == null || reviewResult.getExitMessage().isBlank()
                        ? "目前這批資料不足以可靠回答這個問題。"
                        : reviewResult.getExitMessage())
                .data(rows == null ? List.of() : rows)
                .meta(meta)
                .suggestions(reviewResult.getSuggestions() == null ? List.of() : reviewResult.getSuggestions())
                .build();
    }

    private DashboardIntentParseResult cloneWithRetryFilters(
            DashboardIntentParseResult source,
            Map<String, Object> retryFilters) {

        DashboardIntentParseResult target = new DashboardIntentParseResult();
        if (source != null) {
            target.setSuccess(source.getSuccess());
            target.setCapability(source.getCapability());
            target.setQueryMode(source.getQueryMode());
            target.setTargetScope(source.getTargetScope());
            target.setTargetUserId(source.getTargetUserId());
            target.setClarificationQuestion(source.getClarificationQuestion());
            target.setReasoningSummary(source.getReasoningSummary());
            target.setConfidence(source.getConfidence());
            target.setSuggestions(source.getSuggestions() == null ? List.of() : List.copyOf(source.getSuggestions()));
        }

        Map<String, Object> mergedFilters = new LinkedHashMap<>();
        if (source != null && source.getFilters() != null) {
            mergedFilters.putAll(source.getFilters());
        }
        if (retryFilters != null && !retryFilters.isEmpty()) {
            mergedFilters.putAll(retryFilters);
        }
        target.setFilters(mergedFilters);
        return target;
    }

    private String detect_response_language(String query) {
        if (query == null || query.isBlank()) {
            return "zh-Hant";
        }
        int chineseCount = 0;
        int englishCount = 0;
        for (int i = 0; i < query.length(); i++) {
            char ch = query.charAt(i);
            if (ch >= '\u4E00' && ch <= '\u9FFF') {
                chineseCount++;
            } else if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z')) {
                englishCount++;
            }
        }
        return englishCount > chineseCount ? "en" : "zh-Hant";
    }

    private boolean is_blank(String value) {
        return value == null || value.isBlank();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private record DashboardStructuredExecutionResult(
            DashboardSqlGenerationResult sqlPlan,
            List<Map<String, Object>> rows
    ) {
    }
}

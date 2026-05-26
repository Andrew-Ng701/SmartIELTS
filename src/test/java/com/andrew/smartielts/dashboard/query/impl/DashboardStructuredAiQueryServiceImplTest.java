package com.andrew.smartielts.dashboard.query.impl;

import com.andrew.smartielts.dashboard.agent.answer.DashboardAnswerComposeService;
import com.andrew.smartielts.dashboard.agent.answer.DashboardAnswerReviewAction;
import com.andrew.smartielts.dashboard.agent.answer.DashboardAnswerReviewService;
import com.andrew.smartielts.dashboard.agent.answer.dto.DashboardAnswerComposeResult;
import com.andrew.smartielts.dashboard.agent.answer.dto.DashboardAnswerReviewResult;
import com.andrew.smartielts.dashboard.agent.intent.DashboardIntentCapability;
import com.andrew.smartielts.dashboard.agent.intent.dto.DashboardIntentParseResult;
import com.andrew.smartielts.dashboard.controller.dto.DashboardAssistantResponse;
import com.andrew.smartielts.dashboard.query.DashboardSqlGenerationService;
import com.andrew.smartielts.dashboard.query.SecureDashboardQueryService;
import com.andrew.smartielts.dashboard.query.dto.DashboardSqlGenerationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardStructuredAiQueryServiceImplTest {

    @Mock
    private DashboardSqlGenerationService dashboardSqlGenerationService;

    @Mock
    private SecureDashboardQueryService secureDashboardQueryService;

    @Mock
    private DashboardAnswerComposeService dashboardAnswerComposeService;

    @Mock
    private DashboardAnswerReviewService dashboardAnswerReviewService;

    @InjectMocks
    private DashboardStructuredAiQueryServiceImpl service;

    @Test
    void fallsBackToComposedAnswerWhenSqlReviewReturnsGenericQueryMessage() {
        DashboardSqlGenerationResult sqlPlan = new DashboardSqlGenerationResult();
        sqlPlan.setSuccess(Boolean.TRUE);
        sqlPlan.setSql("select 39 as total_records");
        sqlPlan.setParams(new LinkedHashMap<>());
        sqlPlan.setExpectedColumns(List.of("total_records"));
        sqlPlan.setQueryPurpose("overall_performance");
        sqlPlan.setReasoningSummary("query user dashboard records");
        sqlPlan.setConfidence(0.9D);
        sqlPlan.setSuggestions(List.of());

        List<Map<String, Object>> rows = List.of(Map.of("total_records", 39));

        when(dashboardSqlGenerationService.generate(anyString(), eq(2L), eq(2L), anyString(), any(), anyMap()))
                .thenReturn(sqlPlan);
        when(secureDashboardQueryService.execute(any())).thenReturn(rows);
        when(dashboardAnswerReviewService.review(any())).thenReturn(DashboardAnswerReviewResult.builder()
                .action(DashboardAnswerReviewAction.PROCEED)
                .reviewSummary("Current data is acceptable for answer generation.")
                .suggestions(List.of())
                .build());
        when(dashboardSqlGenerationService.reviewAndAnswer(anyString(), eq(2L), eq(2L), anyString(), any(), eq(sqlPlan), anyList(), anyMap()))
                .thenReturn(Map.of(
                        "answer", "我已完成查詢，下面是相關資料結果。",
                        "data", rows,
                        "suggestions", List.of(),
                        "meta", Map.of()
                ));
        when(dashboardAnswerComposeService.compose(any())).thenReturn(DashboardAnswerComposeResult.builder()
                .answer("Your dashboard shows 39 active practice records.")
                .suggestions(List.of("Review my recent IELTS practice records"))
                .build());

        DashboardIntentParseResult intent = new DashboardIntentParseResult();
        intent.setCapability(DashboardIntentCapability.STRUCTURED_QUERY);
        intent.setFilters(Map.of());

        DashboardAssistantResponse response = service.execute(
                "USER",
                2L,
                2L,
                "Explain my overall IELTS performance using my latest practice records.",
                intent,
                Map.of()
        );

        assertEquals("Your dashboard shows 39 active practice records.", response.getAnswer());
        assertFalse(response.getAnswer().contains("我已完成查詢"));
        assertEquals("AI_SQL_COMPOSE_FALLBACK", response.getMeta().get("answer_mode"));
        assertEquals("compose_service", response.getMeta().get("answer_source"));
    }
}

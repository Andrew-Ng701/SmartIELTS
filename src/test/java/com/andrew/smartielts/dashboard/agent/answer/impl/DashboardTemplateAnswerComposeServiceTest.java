package com.andrew.smartielts.dashboard.agent.answer.impl;

import com.andrew.smartielts.dashboard.agent.answer.DashboardSuggestionService;
import com.andrew.smartielts.dashboard.agent.answer.dto.DashboardAnswerComposeRequest;
import com.andrew.smartielts.dashboard.agent.answer.dto.DashboardAnswerComposeResult;
import com.andrew.smartielts.dashboard.domain.vo.UserOverviewVO;
import com.andrew.smartielts.dashboard.domain.vo.UserProgressSummaryVO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DashboardTemplateAnswerComposeServiceTest {

    @Test
    void summarizesPreloadedDashboardPayloadInsteadOfReturningGenericQueryMessage() {
        DashboardSuggestionService suggestionService = mock(DashboardSuggestionService.class);
        when(suggestionService.buildSuggestions(anyString(), anyString(), anyString(), anyString(), isNull(), anyMap(), any()))
                .thenReturn(List.of("Review my recent IELTS practice records"));

        DashboardTemplateAnswerComposeService service = new DashboardTemplateAnswerComposeService(suggestionService);

        UserOverviewVO overview = new UserOverviewVO();
        overview.setTotalActiveRecords(39L);
        overview.setTotalDeletedRecords(18L);

        UserProgressSummaryVO progressSummary = new UserProgressSummaryVO();
        progressSummary.setOverallAverageScore(new BigDecimal("5.75"));

        DashboardAnswerComposeResult result = service.compose(DashboardAnswerComposeRequest.builder()
                .role("USER")
                .originalQuery("Explain my overall IELTS performance using my latest practice records.")
                .capability("PRELOADED_DIRECT")
                .filters(Map.of())
                .responseLanguage("en")
                .data(Map.of(
                        "overview", overview,
                        "progressSummary", progressSummary,
                        "moduleStats", List.of(Map.of("module", "reading")),
                        "recentRecords", List.of(Map.of("module", "writing"))
                ))
                .build());

        assertFalse(result.getAnswer().contains("我已完成查詢"));
        assertTrue(result.getAnswer().contains("39 active practice records"));
        assertTrue(result.getAnswer().contains("5.75"));
    }
}

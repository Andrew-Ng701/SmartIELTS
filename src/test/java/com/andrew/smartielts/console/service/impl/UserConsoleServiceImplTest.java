package com.andrew.smartielts.console.service.impl;

import com.andrew.smartielts.console.domain.vo.UserConsoleVO;
import com.andrew.smartielts.console.service.LearningConsoleQueryService;
import com.andrew.smartielts.dashboard.domain.vo.UserModuleStatVO;
import com.andrew.smartielts.dashboard.domain.vo.UserOverviewVO;
import com.andrew.smartielts.dashboard.domain.vo.UserProgressSummaryVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserConsoleServiceImplTest {

    @Mock
    private LearningConsoleQueryService learningConsoleQueryService;

    @Test
    void console_shouldBuildFullPayloadWithThreeCharts() {
        UserConsoleServiceImpl service = new UserConsoleServiceImpl(learningConsoleQueryService);
        UserOverviewVO overview = new UserOverviewVO();
        overview.setUserId(9L);
        overview.setEmail("u@example.com");
        overview.setConsecutiveLoginDays(6);
        overview.setTotalActiveRecords(10L);
        overview.setTotalDeletedRecords(2L);
        overview.setListeningTargetScore(BigDecimal.valueOf(7));
        UserProgressSummaryVO progress = new UserProgressSummaryVO();
        progress.setListeningAverageScore(BigDecimal.valueOf(6));
        progress.setReadingAverageScore(BigDecimal.valueOf(7));
        progress.setWritingAverageScore(BigDecimal.valueOf(5));
        progress.setSpeakingAverageScore(BigDecimal.valueOf(8));
        progress.setOverallAverageScore(BigDecimal.valueOf(6.5));
        UserModuleStatVO module = new UserModuleStatVO();
        module.setModule("listening");
        module.setActiveCount(3L);
        module.setDeletedCount(1L);

        when(learningConsoleQueryService.userOverview(9L)).thenReturn(overview);
        when(learningConsoleQueryService.userProgressSummary(9L)).thenReturn(progress);
        when(learningConsoleQueryService.userRecentRecords(9L)).thenReturn(List.of());
        when(learningConsoleQueryService.userModuleStats(9L)).thenReturn(List.of(module));

        UserConsoleVO result = service.console(9L);

        assertNotNull(result.getSnapshotId());
        assertEquals("u@example.com", result.getProfile().getEmail());
        assertEquals(6, result.getProfile().getConsecutiveLoginDays());
        assertEquals(12L, result.getKpis().getTotalRecords());
        assertEquals(BigDecimal.valueOf(6.5), result.getKpis().getOverallAverageScore());
        assertEquals(result.getKpis().getOverallAverageScore(), result.getKpis().getOverallAverage());
        assertEquals(1, result.getModuleStats().size());
        assertEquals(0, result.getRecentRecords().size());
        assertNotNull(result.getInsights());
        assertEquals(3, result.getCharts().size());
        assertEquals("scoreRadar", result.getCharts().get(0).getCode());
        assertEquals("module", result.getCharts().get(0).getDimensionKey());
        assertEquals("average_score", result.getCharts().get(0).getYKey());
        assertEquals(4, result.getCharts().get(0).getValues().size());
        assertEquals(4, result.getCharts().get(0).getRows().size());
        assertEquals(1, result.getCharts().get(0).getSeries().size());
        verify(learningConsoleQueryService).userModuleStats(9L);
    }
}

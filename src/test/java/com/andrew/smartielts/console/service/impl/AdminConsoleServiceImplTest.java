package com.andrew.smartielts.console.service.impl;

import com.andrew.smartielts.console.domain.vo.AdminConsoleVO;
import com.andrew.smartielts.console.service.LearningConsoleQueryService;
import com.andrew.smartielts.dashboard.domain.vo.AdminAiFailureVO;
import com.andrew.smartielts.dashboard.domain.vo.AdminModuleStatVO;
import com.andrew.smartielts.dashboard.domain.vo.AdminOverviewVO;
import com.andrew.smartielts.dashboard.domain.vo.AdminUserCountVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminConsoleServiceImplTest {

    @Mock
    private LearningConsoleQueryService learningConsoleQueryService;

    @Test
    void console_shouldBuildFullPayloadWithLeaderboardsAndThreeCharts() {
        AdminConsoleServiceImpl service = new AdminConsoleServiceImpl(learningConsoleQueryService);
        AdminOverviewVO overview = new AdminOverviewVO();
        overview.setTotalActiveRecords(10L);
        overview.setTotalDeletedRecords(1L);
        AdminUserCountVO userCount = new AdminUserCountVO();
        userCount.setTotalUsers(9L);
        userCount.setActiveUsers(8L);
        userCount.setDeletedUsers(1L);
        AdminModuleStatVO module = new AdminModuleStatVO();
        module.setModule("writing");
        module.setActiveCount(4L);
        module.setDeletedCount(1L);
        AdminAiFailureVO failure = new AdminAiFailureVO();
        failure.setModule("writing");
        failure.setFailureCount(2L);

        when(learningConsoleQueryService.adminOverview()).thenReturn(overview);
        when(learningConsoleQueryService.adminUserCount()).thenReturn(userCount);
        when(learningConsoleQueryService.adminModuleStats()).thenReturn(List.of(module));
        when(learningConsoleQueryService.adminAiFailureSummary()).thenReturn(List.of(failure));
        when(learningConsoleQueryService.adminRecentIssues()).thenReturn(List.of());
        when(learningConsoleQueryService.adminUserLeaderboards(10)).thenReturn(List.of());

        AdminConsoleVO result = service.console();

        assertNotNull(result.getSnapshotId());
        assertEquals(9L, result.getKpis().getTotalUsers());
        assertEquals(2L, result.getKpis().getAiFailureCount());
        assertEquals(1, result.getModuleStats().size());
        assertEquals(0, result.getRecentIssues().size());
        assertEquals(5, result.getQuickLinks().size());
        assertEquals(0, result.getLeaderboards().size());
        assertEquals(3, result.getCharts().size());
        assertEquals("moduleRecordsBar", result.getCharts().get(0).getCode());
        verify(learningConsoleQueryService).adminUserLeaderboards(10);
    }
}

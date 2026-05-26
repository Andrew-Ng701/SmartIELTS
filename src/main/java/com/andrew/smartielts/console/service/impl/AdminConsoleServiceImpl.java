package com.andrew.smartielts.console.service.impl;

import com.andrew.smartielts.admin.domain.vo.AdminQuickLinkVO;
import com.andrew.smartielts.admin.domain.vo.AdminRecentIssueVO;
import com.andrew.smartielts.console.domain.vo.AdminConsoleKpiVO;
import com.andrew.smartielts.console.domain.vo.AdminConsoleLeaderboardVO;
import com.andrew.smartielts.console.domain.vo.AdminConsoleModuleStatVO;
import com.andrew.smartielts.console.domain.vo.AdminConsoleVO;
import com.andrew.smartielts.console.domain.vo.ConsoleChartSeriesVO;
import com.andrew.smartielts.console.domain.vo.ConsoleChartVO;
import com.andrew.smartielts.console.service.AdminConsoleService;
import com.andrew.smartielts.console.service.LearningConsoleQueryService;
import com.andrew.smartielts.dashboard.domain.vo.AdminAiFailureVO;
import com.andrew.smartielts.dashboard.domain.vo.AdminModuleStatVO;
import com.andrew.smartielts.dashboard.domain.vo.AdminOverviewVO;
import com.andrew.smartielts.dashboard.domain.vo.AdminUserCountVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminConsoleServiceImpl implements AdminConsoleService {

    private static final int LEADERBOARD_LIMIT = 10;

    private final LearningConsoleQueryService learningConsoleQueryService;

    @Override
    public AdminConsoleVO console() {
        AdminOverviewVO overview = learningConsoleQueryService.adminOverview();
        AdminUserCountVO userStats = learningConsoleQueryService.adminUserCount();
        List<AdminModuleStatVO> rawModuleStats = safeList(learningConsoleQueryService.adminModuleStats());
        List<AdminAiFailureVO> aiFailures = safeList(learningConsoleQueryService.adminAiFailureSummary());
        List<AdminRecentIssueVO> recentIssues = safeList(learningConsoleQueryService.adminRecentIssues());
        List<AdminConsoleLeaderboardVO> leaderboards =
                safeList(learningConsoleQueryService.adminUserLeaderboards(LEADERBOARD_LIMIT));
        List<AdminConsoleModuleStatVO> moduleStats = buildModuleStats(rawModuleStats, aiFailures);

        AdminConsoleVO vo = new AdminConsoleVO();
        vo.setSnapshotId(UUID.randomUUID().toString());
        vo.setSnapshotTime(OffsetDateTime.now().toString());
        vo.setKpis(buildKpis(overview, userStats, aiFailures));
        vo.setModuleStats(moduleStats);
        vo.setUserStats(userStats);
        vo.setRecentIssues(recentIssues);
        vo.setQuickLinks(quickLinks());
        vo.setLeaderboards(leaderboards);
        vo.setCharts(List.of(
                moduleRecordsBarChart(moduleStats),
                moduleRecordsDonutChart(moduleStats),
                aiFailuresBarChart(moduleStats)
        ));
        return vo;
    }

    private AdminConsoleKpiVO buildKpis(AdminOverviewVO overview,
                                        AdminUserCountVO userStats,
                                        List<AdminAiFailureVO> aiFailures) {
        AdminConsoleKpiVO vo = new AdminConsoleKpiVO();
        long activeRecords = overview == null ? 0L : overview.getTotalActiveRecords();
        long deletedRecords = overview == null ? 0L : overview.getTotalDeletedRecords();
        vo.setTotalUsers(userStats == null ? 0L : userStats.getTotalUsers());
        vo.setActiveUsers(userStats == null ? 0L : userStats.getActiveUsers());
        vo.setDeletedUsers(userStats == null ? 0L : userStats.getDeletedUsers());
        vo.setTotalActiveRecords(activeRecords);
        vo.setTotalDeletedRecords(deletedRecords);
        vo.setTotalRecords(activeRecords + deletedRecords);
        vo.setAiFailureCount(aiFailures.stream().mapToLong(AdminAiFailureVO::getFailureCount).sum());
        return vo;
    }

    private List<AdminConsoleModuleStatVO> buildModuleStats(List<AdminModuleStatVO> moduleStats,
                                                            List<AdminAiFailureVO> aiFailures) {
        return moduleStats.stream()
                .map(stat -> toModuleStat(stat, aiFailures))
                .toList();
    }

    private AdminConsoleModuleStatVO toModuleStat(AdminModuleStatVO stat, List<AdminAiFailureVO> aiFailures) {
        AdminConsoleModuleStatVO vo = new AdminConsoleModuleStatVO();
        String module = stat == null ? null : stat.getModule();
        long active = stat == null ? 0L : stat.getActiveCount();
        long deleted = stat == null ? 0L : stat.getDeletedCount();
        vo.setModule(module);
        vo.setActiveCount(active);
        vo.setDeletedCount(deleted);
        vo.setTotalCount(active + deleted);
        vo.setAiFailureCount(aiFailures.stream()
                .filter(item -> item != null && module != null && module.equals(item.getModule()))
                .mapToLong(AdminAiFailureVO::getFailureCount)
                .sum());
        return vo;
    }

    private List<AdminQuickLinkVO> quickLinks() {
        return List.of(
                quickLink("users", "Users", "/admin/users"),
                quickLink("listening", "Listening Records", "/admin/listening/records"),
                quickLink("reading", "Reading Records", "/admin/reading/records"),
                quickLink("writing", "Writing Records", "/admin/writing/records"),
                quickLink("speaking", "Speaking Records", "/admin/speaking/records")
        );
    }

    private ConsoleChartVO moduleRecordsBarChart(List<AdminConsoleModuleStatVO> moduleStats) {
        ConsoleChartVO chart = new ConsoleChartVO();
        chart.setCode("moduleRecordsBar");
        chart.setTitle("Module records");
        chart.setChartType("bar");
        chart.setDimensionKey("module");
        chart.setXKey("module");
        chart.setRows(safeList(moduleStats));
        chart.setSeries(List.of(
                series("active_count", "activeCount"),
                series("deleted_count", "deletedCount")
        ));
        chart.setIndicators(List.of());
        chart.setValues(List.of());
        chart.setMeta(Map.of());
        return chart;
    }

    private ConsoleChartVO moduleRecordsDonutChart(List<AdminConsoleModuleStatVO> moduleStats) {
        ConsoleChartVO chart = new ConsoleChartVO();
        chart.setCode("moduleRecordsDonut");
        chart.setTitle("Module total records");
        chart.setChartType("donut");
        chart.setDimensionKey("module");
        chart.setRows(safeList(moduleStats));
        chart.setSeries(List.of(series("total_count", "totalCount")));
        chart.setIndicators(List.of());
        chart.setValues(List.of());
        chart.setMeta(Map.of("value_field", "totalCount"));
        return chart;
    }

    private ConsoleChartVO aiFailuresBarChart(List<AdminConsoleModuleStatVO> moduleStats) {
        ConsoleChartVO chart = new ConsoleChartVO();
        chart.setCode("aiFailuresBar");
        chart.setTitle("AI failures by module");
        chart.setChartType("bar");
        chart.setDimensionKey("module");
        chart.setXKey("module");
        chart.setYKey("aiFailureCount");
        chart.setRows(safeList(moduleStats));
        chart.setSeries(List.of(series("ai_failure_count", "aiFailureCount")));
        chart.setIndicators(List.of());
        chart.setValues(List.of());
        chart.setMeta(Map.of());
        return chart;
    }

    private ConsoleChartSeriesVO series(String name, String field) {
        ConsoleChartSeriesVO vo = new ConsoleChartSeriesVO();
        vo.setName(name);
        vo.setField(field);
        return vo;
    }

    private AdminQuickLinkVO quickLink(String code, String title, String path) {
        AdminQuickLinkVO vo = new AdminQuickLinkVO();
        vo.setCode(code);
        vo.setTitle(title);
        vo.setPath(path);
        return vo;
    }

    private <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list;
    }
}

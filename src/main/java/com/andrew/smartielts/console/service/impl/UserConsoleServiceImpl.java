package com.andrew.smartielts.console.service.impl;

import com.andrew.smartielts.console.domain.vo.ConsoleChartSeriesVO;
import com.andrew.smartielts.console.domain.vo.ConsoleChartVO;
import com.andrew.smartielts.console.domain.vo.UserConsoleInsightsVO;
import com.andrew.smartielts.console.domain.vo.UserConsoleKpiVO;
import com.andrew.smartielts.console.domain.vo.UserConsoleModuleStatVO;
import com.andrew.smartielts.console.domain.vo.UserConsoleProfileVO;
import com.andrew.smartielts.console.domain.vo.UserConsoleVO;
import com.andrew.smartielts.console.service.LearningConsoleQueryService;
import com.andrew.smartielts.console.service.UserConsoleService;
import com.andrew.smartielts.dashboard.domain.vo.UserModuleStatVO;
import com.andrew.smartielts.dashboard.domain.vo.UserOverviewVO;
import com.andrew.smartielts.dashboard.domain.vo.UserProgressSummaryVO;
import com.andrew.smartielts.dashboard.domain.vo.UserRecentRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserConsoleServiceImpl implements UserConsoleService {

    private final LearningConsoleQueryService learningConsoleQueryService;

    @Override
    public UserConsoleVO console(Long userId) {
        UserOverviewVO overview = learningConsoleQueryService.userOverview(userId);
        UserProgressSummaryVO progress = learningConsoleQueryService.userProgressSummary(userId);
        List<UserRecentRecordVO> recentRecords = safeList(learningConsoleQueryService.userRecentRecords(userId));
        List<UserModuleStatVO> rawModuleStats = safeList(learningConsoleQueryService.userModuleStats(userId));
        List<UserConsoleModuleStatVO> moduleStats = buildModuleStats(rawModuleStats, overview, progress);

        UserConsoleVO vo = new UserConsoleVO();
        vo.setSnapshotId(UUID.randomUUID().toString());
        vo.setSnapshotTime(OffsetDateTime.now().toString());
        vo.setProfile(buildProfile(overview));
        vo.setKpis(buildKpis(overview, progress, recentRecords));
        vo.setModuleStats(moduleStats);
        vo.setRecentRecords(recentRecords);
        vo.setInsights(buildInsights(moduleStats, recentRecords));
        vo.setCharts(List.of(
                scoreRadarChart(progress),
                scoreTrendChart(recentRecords),
                moduleActivityChart(moduleStats)
        ));
        return vo;
    }

    private UserConsoleProfileVO buildProfile(UserOverviewVO overview) {
        UserConsoleProfileVO vo = new UserConsoleProfileVO();
        if (overview == null) {
            return vo;
        }
        vo.setUserId(overview.getUserId());
        vo.setEmail(overview.getEmail());
        vo.setUsername(overview.getUsername());
        vo.setLastLoginTime(overview.getLastLoginTime());
        vo.setConsecutiveLoginDays(overview.getConsecutiveLoginDays());
        vo.setListeningTargetScore(overview.getListeningTargetScore());
        vo.setReadingTargetScore(overview.getReadingTargetScore());
        vo.setWritingTargetScore(overview.getWritingTargetScore());
        vo.setSpeakingTargetScore(overview.getSpeakingTargetScore());
        return vo;
    }

    private UserConsoleKpiVO buildKpis(UserOverviewVO overview,
                                       UserProgressSummaryVO progress,
                                       List<UserRecentRecordVO> recentRecords) {
        UserConsoleKpiVO vo = new UserConsoleKpiVO();
        long active = overview == null ? 0L : overview.getTotalActiveRecords();
        long deleted = overview == null ? 0L : overview.getTotalDeletedRecords();
        vo.setTotalActiveRecords(active);
        vo.setTotalDeletedRecords(deleted);
        vo.setTotalRecords(active + deleted);
        vo.setOverallAverageScore(progress == null ? BigDecimal.ZERO : defaultDecimal(progress.getOverallAverageScore()));
        vo.setOverallAverage(vo.getOverallAverageScore());
        vo.setRecentActivityCount(recentRecords == null ? 0 : recentRecords.size());
        vo.setTargetAverageScore(buildTargetAverage(overview));
        vo.setOverallTargetGap(diff(vo.getOverallAverageScore(), vo.getTargetAverageScore()));
        vo.setAiPendingCount(countAiStatus(recentRecords, "PENDING", "PROCESSING"));
        vo.setAiFailedCount(countAiStatus(recentRecords, "FAILED", "FAIL"));
        return vo;
    }

    private List<UserConsoleModuleStatVO> buildModuleStats(List<UserModuleStatVO> stats,
                                                           UserOverviewVO overview,
                                                           UserProgressSummaryVO progress) {
        return stats.stream()
                .map(stat -> toModuleStat(stat, overview, progress))
                .toList();
    }

    private UserConsoleModuleStatVO toModuleStat(UserModuleStatVO stat,
                                                 UserOverviewVO overview,
                                                 UserProgressSummaryVO progress) {
        UserConsoleModuleStatVO vo = new UserConsoleModuleStatVO();
        String module = stat == null ? null : stat.getModule();
        long active = stat == null ? 0L : stat.getActiveCount();
        long deleted = stat == null ? 0L : stat.getDeletedCount();
        BigDecimal average = moduleAverage(module, progress);
        BigDecimal target = moduleTarget(module, overview);
        vo.setModule(module);
        vo.setActiveCount(active);
        vo.setDeletedCount(deleted);
        vo.setTotalCount(active + deleted);
        vo.setAverageScore(average);
        vo.setTargetScore(target);
        vo.setTargetGap(diff(average, target));
        return vo;
    }

    private UserConsoleInsightsVO buildInsights(List<UserConsoleModuleStatVO> moduleStats,
                                                List<UserRecentRecordVO> recentRecords) {
        UserConsoleInsightsVO vo = new UserConsoleInsightsVO();
        vo.setBestModule(moduleStats.stream()
                .filter(stat -> stat.getAverageScore() != null)
                .max(Comparator.comparing(UserConsoleModuleStatVO::getAverageScore))
                .map(UserConsoleModuleStatVO::getModule)
                .orElse(null));
        vo.setWeakestModule(moduleStats.stream()
                .filter(stat -> stat.getAverageScore() != null)
                .min(Comparator.comparing(UserConsoleModuleStatVO::getAverageScore))
                .map(UserConsoleModuleStatVO::getModule)
                .orElse(null));
        vo.setLatestActivitySummary(recentRecords.isEmpty() ? "No recent activity" : recentRecords.get(0).getSummary());
        long failed = countAiStatus(recentRecords, "FAILED", "FAIL");
        long pending = countAiStatus(recentRecords, "PENDING", "PROCESSING");
        vo.setAiIssueSummary("pending=" + pending + ", failed=" + failed);
        return vo;
    }

    private ConsoleChartVO scoreRadarChart(UserProgressSummaryVO progress) {
        ConsoleChartVO chart = new ConsoleChartVO();
        chart.setCode("scoreRadar");
        chart.setTitle("Average score radar");
        chart.setChartType("radar");
        chart.setDimensionKey("module");
        chart.setYKey("average_score");
        chart.setIndicators(List.of("listening", "reading", "writing", "speaking"));
        List<BigDecimal> values = List.of(
                progress == null ? BigDecimal.ZERO : defaultDecimal(progress.getListeningAverageScore()),
                progress == null ? BigDecimal.ZERO : defaultDecimal(progress.getReadingAverageScore()),
                progress == null ? BigDecimal.ZERO : defaultDecimal(progress.getWritingAverageScore()),
                progress == null ? BigDecimal.ZERO : defaultDecimal(progress.getSpeakingAverageScore())
        );
        chart.setValues(values);
        chart.setRows(List.of(
                Map.of("module", "listening", "average_score", values.get(0)),
                Map.of("module", "reading", "average_score", values.get(1)),
                Map.of("module", "writing", "average_score", values.get(2)),
                Map.of("module", "speaking", "average_score", values.get(3))
        ));
        chart.setSeries(List.of(series("average_score", "average_score")));
        chart.setMeta(Map.of("max_score", BigDecimal.valueOf(9)));
        return chart;
    }

    private ConsoleChartVO scoreTrendChart(List<UserRecentRecordVO> recentRecords) {
        ConsoleChartVO chart = new ConsoleChartVO();
        chart.setCode("scoreTrend");
        chart.setTitle("Recent score trend");
        chart.setChartType("line");
        chart.setXKey("createdTime");
        chart.setYKey("summary");
        chart.setRows(safeList(recentRecords));
        chart.setSeries(List.of(series("score", "summary")));
        chart.setIndicators(List.of());
        chart.setValues(List.of());
        chart.setMeta(Map.of("source", "recent_records"));
        return chart;
    }

    private ConsoleChartVO moduleActivityChart(List<UserConsoleModuleStatVO> moduleStats) {
        ConsoleChartVO chart = new ConsoleChartVO();
        chart.setCode("moduleActivity");
        chart.setTitle("Module activity");
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

    private ConsoleChartSeriesVO series(String name, String field) {
        ConsoleChartSeriesVO vo = new ConsoleChartSeriesVO();
        vo.setName(name);
        vo.setField(field);
        return vo;
    }

    private BigDecimal moduleAverage(String module, UserProgressSummaryVO progress) {
        if (progress == null || module == null) {
            return BigDecimal.ZERO;
        }
        return switch (module) {
            case "listening" -> defaultDecimal(progress.getListeningAverageScore());
            case "reading" -> defaultDecimal(progress.getReadingAverageScore());
            case "writing" -> defaultDecimal(progress.getWritingAverageScore());
            case "speaking" -> defaultDecimal(progress.getSpeakingAverageScore());
            default -> BigDecimal.ZERO;
        };
    }

    private BigDecimal moduleTarget(String module, UserOverviewVO overview) {
        if (overview == null || module == null) {
            return null;
        }
        return switch (module) {
            case "listening" -> overview.getListeningTargetScore();
            case "reading" -> overview.getReadingTargetScore();
            case "writing" -> overview.getWritingTargetScore();
            case "speaking" -> overview.getSpeakingTargetScore();
            default -> null;
        };
    }

    private BigDecimal buildTargetAverage(UserOverviewVO overview) {
        if (overview == null) {
            return null;
        }
        List<BigDecimal> targets = java.util.stream.Stream.of(
                        overview.getListeningTargetScore(),
                        overview.getReadingTargetScore(),
                        overview.getWritingTargetScore(),
                        overview.getSpeakingTargetScore()
                )
                .filter(Objects::nonNull)
                .toList();
        if (targets.isEmpty()) {
            return null;
        }
        BigDecimal sum = targets.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(targets.size()), 2, java.math.RoundingMode.HALF_UP);
    }

    private BigDecimal diff(BigDecimal actual, BigDecimal target) {
        return actual == null || target == null ? null : actual.subtract(target);
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private long countAiStatus(List<UserRecentRecordVO> records, String... tokens) {
        if (records == null || records.isEmpty()) {
            return 0L;
        }
        return records.stream()
                .filter(record -> matchesStatus(record == null ? null : record.getStatus(), tokens))
                .count();
    }

    private boolean matchesStatus(String status, String... tokens) {
        if (status == null || tokens == null) {
            return false;
        }
        String normalized = status.trim().toUpperCase();
        for (String token : tokens) {
            if (token != null && normalized.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list;
    }
}

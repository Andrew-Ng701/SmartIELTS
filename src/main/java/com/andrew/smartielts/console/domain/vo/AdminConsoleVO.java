package com.andrew.smartielts.console.domain.vo;

import com.andrew.smartielts.admin.domain.vo.AdminQuickLinkVO;
import com.andrew.smartielts.admin.domain.vo.AdminRecentIssueVO;
import com.andrew.smartielts.dashboard.domain.vo.AdminUserCountVO;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AdminConsoleVO {

    private String snapshotId;
    private String snapshotTime;
    private AdminConsoleKpiVO kpis;
    private List<AdminConsoleModuleStatVO> moduleStats;
    private AdminUserCountVO userStats;
    private List<AdminRecentIssueVO> recentIssues;
    private List<AdminQuickLinkVO> quickLinks;
    private List<AdminConsoleLeaderboardVO> leaderboards;
    private List<ConsoleChartVO> charts;
}

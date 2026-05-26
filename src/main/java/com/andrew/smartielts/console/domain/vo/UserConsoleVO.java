package com.andrew.smartielts.console.domain.vo;

import com.andrew.smartielts.dashboard.domain.vo.UserRecentRecordVO;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserConsoleVO {

    private String snapshotId;
    private String snapshotTime;
    private UserConsoleProfileVO profile;
    private UserConsoleKpiVO kpis;
    private List<UserConsoleModuleStatVO> moduleStats;
    private List<UserRecentRecordVO> recentRecords;
    private UserConsoleInsightsVO insights;
    private List<ConsoleChartVO> charts;
}

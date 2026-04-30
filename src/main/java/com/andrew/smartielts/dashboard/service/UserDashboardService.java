package com.andrew.smartielts.dashboard.service;

import com.andrew.smartielts.dashboard.domain.vo.UserModuleStatVO;
import com.andrew.smartielts.dashboard.domain.vo.UserOverviewVO;
import com.andrew.smartielts.dashboard.domain.vo.UserProgressSummaryVO;
import com.andrew.smartielts.dashboard.domain.vo.UserRecentRecordVO;
import com.andrew.smartielts.dashboard.domain.vo.UserDashboardOverviewVisualVO;
import com.andrew.smartielts.dashboard.domain.vo.UserExecutiveSummaryVO;

import java.util.List;

public interface UserDashboardService {

    UserOverviewVO overview(Long userId);

    List<UserModuleStatVO> deletedSummary(Long userId);

    List<UserModuleStatVO> userStats(Long userId);

    List<UserRecentRecordVO> recentRecords(Long userId);

    UserProgressSummaryVO progressSummary(Long userId);

    UserDashboardOverviewVisualVO userOverviewVisual(Long userId, String timeRange);

    UserExecutiveSummaryVO userExecutiveSummary(Long userId, String timeRange);
}
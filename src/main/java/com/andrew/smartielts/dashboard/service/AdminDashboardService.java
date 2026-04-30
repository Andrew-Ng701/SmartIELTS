package com.andrew.smartielts.dashboard.service;

import com.andrew.smartielts.dashboard.domain.vo.*;
import com.andrew.smartielts.dashboard.domain.vo.AdminDashboardOverviewVisualVO;
import com.andrew.smartielts.dashboard.domain.vo.AdminExecutiveSummaryVO;

import java.util.List;

public interface AdminDashboardService {

    AdminOverviewVO overview();

    AdminUserCountVO userCount();

    List<AdminModuleStatVO> moduleStats();

    List<AdminAiFailureVO> aiFailureSummary();

    AdminUserRecordSummaryVO userRecordSummary(Long targetUserId);

    List<AdminRecentIssueVO> recentIssues();

    AdminDashboardOverviewVisualVO adminOverviewVisual(Long operatorUserId, Long targetUserId, String timeRange);

    AdminExecutiveSummaryVO adminExecutiveSummary(Long operatorUserId, Long targetUserId, String timeRange);
}
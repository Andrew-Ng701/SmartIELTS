package com.andrew.smartielts.dashboard.service;

import com.andrew.smartielts.dashboard.domain.vo.*;

import java.util.List;

public interface AdminDashboardService {

    AdminOverviewVO overview();

    AdminUserCountVO userCount();

    List<AdminModuleStatVO> moduleStats();

    List<AdminAiFailureVO> aiFailureSummary();


    AdminUserRecordSummaryVO userRecordSummary(Long targetUserId);

    List<AdminRecentIssueVO> recentIssues();
}
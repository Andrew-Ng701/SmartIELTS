package com.andrew.smartielts.admin.service;

import com.andrew.smartielts.admin.domain.vo.*;

import java.util.List;

public interface AdminService {

    AdminOverviewVO overview();

    List<AdminRecentIssueVO> recentIssues();

    List<AdminQuickLinkVO> quickLinks();

    List<AdminModuleStatVO> moduleStats();

    AdminUserConsoleSummaryVO userConsoleSummary(Long userId);
}
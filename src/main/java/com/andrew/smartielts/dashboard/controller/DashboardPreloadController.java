package com.andrew.smartielts.dashboard.controller;

import com.andrew.smartielts.common.resultDTO.Result;
import com.andrew.smartielts.dashboard.constants.DashboardOverviewConstants;
import com.andrew.smartielts.dashboard.controller.dto.DashboardAskPreloadedPayload;
import com.andrew.smartielts.dashboard.preload.DashboardPreloadService;
import com.andrew.smartielts.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/smartielts/dashboard")
@RequiredArgsConstructor
public class DashboardPreloadController {

    private final DashboardPreloadService dashboardPreloadService;

    @GetMapping("/user/preload")
    public Result<DashboardAskPreloadedPayload> preloadUser(
            @RequestParam(name = DashboardOverviewConstants.QUERY_PARAM_TIME_RANGE,
                    defaultValue = DashboardOverviewConstants.DEFAULT_TIME_RANGE) String timeRange) {
        Long operatorUserId = SecurityUtils.getCurrentUserId();
        DashboardAskPreloadedPayload payload = dashboardPreloadService.preload(
                DashboardOverviewConstants.ROLE_USER,
                operatorUserId,
                operatorUserId,
                DashboardOverviewConstants.PAGE_NAME_USER_OVERVIEW,
                null,
                Map.of(DashboardOverviewConstants.CONTEXT_KEY_TIME_RANGE, timeRange)
        );
        return Result.success(payload);
    }

    @GetMapping("/admin/preload")
    public Result<DashboardAskPreloadedPayload> preloadAdmin(
            @RequestParam(name = DashboardOverviewConstants.QUERY_PARAM_TARGET_USER_ID, required = false) Long targetUserId,
            @RequestParam(name = DashboardOverviewConstants.QUERY_PARAM_TIME_RANGE,
                    defaultValue = DashboardOverviewConstants.DEFAULT_TIME_RANGE) String timeRange) {
        Long operatorUserId = SecurityUtils.getCurrentUserId();
        Long effectiveTargetUserId = targetUserId != null ? targetUserId : operatorUserId;
        DashboardAskPreloadedPayload payload = dashboardPreloadService.preload(
                DashboardOverviewConstants.ROLE_ADMIN,
                operatorUserId,
                effectiveTargetUserId,
                DashboardOverviewConstants.PAGE_NAME_ADMIN_OVERVIEW,
                null,
                Map.of(DashboardOverviewConstants.CONTEXT_KEY_TIME_RANGE, timeRange)
        );
        return Result.success(payload);
    }
}

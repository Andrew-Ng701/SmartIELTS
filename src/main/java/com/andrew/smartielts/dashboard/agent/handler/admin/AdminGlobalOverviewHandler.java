package com.andrew.smartielts.dashboard.agent.handler.admin;

import com.andrew.smartielts.dashboard.agent.DashboardAgentContext;
import com.andrew.smartielts.dashboard.agent.DashboardCapability;
import com.andrew.smartielts.dashboard.agent.DashboardCapabilityHandler;
import com.andrew.smartielts.dashboard.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminGlobalOverviewHandler implements DashboardCapabilityHandler {

    private final AdminDashboardService adminDashboardService;

    @Override
    public DashboardCapability support() {
        return DashboardCapability.ADMIN_GLOBAL_OVERVIEW;
    }

    @Override
    public Object handle(DashboardAgentContext context) {
        return adminDashboardService.overview();
    }
}
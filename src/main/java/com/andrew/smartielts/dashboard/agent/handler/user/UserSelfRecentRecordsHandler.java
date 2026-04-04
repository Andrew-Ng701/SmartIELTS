package com.andrew.smartielts.dashboard.agent.handler.user;

import com.andrew.smartielts.dashboard.agent.DashboardAgentContext;
import com.andrew.smartielts.dashboard.agent.DashboardCapability;
import com.andrew.smartielts.dashboard.agent.DashboardCapabilityHandler;
import com.andrew.smartielts.dashboard.service.UserDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserSelfRecentRecordsHandler implements DashboardCapabilityHandler {

    private final UserDashboardService userDashboardService;

    @Override
    public DashboardCapability support() {
        return DashboardCapability.USER_SELF_RECENT_RECORDS;
    }

    @Override
    public Object handle(DashboardAgentContext context) {
        return userDashboardService.recentRecords(context.getOperatorUserId());
    }
}
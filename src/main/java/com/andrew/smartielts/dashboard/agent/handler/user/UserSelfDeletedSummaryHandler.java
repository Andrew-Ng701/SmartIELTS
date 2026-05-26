package com.andrew.smartielts.dashboard.agent.handler.user;

import com.andrew.smartielts.dashboard.agent.DashboardAgentContext;
import com.andrew.smartielts.dashboard.agent.DashboardCapability;
import com.andrew.smartielts.dashboard.agent.DashboardCapabilityHandler;
import com.andrew.smartielts.console.service.LearningConsoleQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserSelfDeletedSummaryHandler implements DashboardCapabilityHandler {

    private final LearningConsoleQueryService learningConsoleQueryService;

    @Override
    public DashboardCapability support() {
        return DashboardCapability.USER_SELF_DELETED_SUMMARY;
    }

    @Override
    public Object handle(DashboardAgentContext context) {
        return learningConsoleQueryService.userDeletedSummary(context.getOperatorUserId());
    }
}

package com.andrew.smartielts.dashboard.agent.intent;

import com.andrew.smartielts.dashboard.agent.intent.dto.DashboardIntentParseResult;
import org.springframework.stereotype.Component;

@Component
public class DashboardIntentPermissionValidator {

    public void validate(String role, Long operatorUserId, DashboardIntentParseResult result) {
        if (result == null) {
            throw new IllegalArgumentException("Missing intent result");
        }

        if ("USER".equalsIgnoreCase(role)) {
            if (result.getTargetScope() == DashboardIntentTargetScope.GLOBAL) {
                throw new IllegalArgumentException("USER cannot access global dashboard data");
            }
            if (result.getTargetUserId() != null && !result.getTargetUserId().equals(operatorUserId)) {
                throw new IllegalArgumentException("USER cannot query other users");
            }
        }
    }
}
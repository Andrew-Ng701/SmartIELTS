package com.andrew.smartielts.dashboard.agent.intent;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum DashboardIntentTargetScope {
    SELF,
    SPECIFIC_USER,
    GLOBAL;

    @JsonCreator
    public static DashboardIntentTargetScope fromValue(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase();

        return switch (normalized) {
            case "SPECIFICUSER" -> SPECIFIC_USER;
            default -> DashboardIntentTargetScope.valueOf(normalized);
        };
    }
}
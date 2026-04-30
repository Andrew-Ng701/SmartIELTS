package com.andrew.smartielts.dashboard.agent.intent;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum DashboardIntentQueryMode {
    SIMPLE_HANDLER,
    STRUCTURED_QUERY,
    CLARIFICATION,
    UNSUPPORTED;

    @JsonCreator
    public static DashboardIntentQueryMode fromValue(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase();

        return switch (normalized) {
            case "SIMPLE_HANDLER" -> SIMPLE_HANDLER;
            case "STRUCTURED_QUERY" -> STRUCTURED_QUERY;
            default -> DashboardIntentQueryMode.valueOf(normalized);
        };
    }
}
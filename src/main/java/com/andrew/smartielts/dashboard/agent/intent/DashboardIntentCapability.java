package com.andrew.smartielts.dashboard.agent.intent;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum DashboardIntentCapability {

    USER_SELF_OVERVIEW("user_self_overview"),
    USER_SELF_RECENT_RECORDS("user_self_recent_records"),
    USER_SELF_PROGRESS_SUMMARY("user_self_progress_summary"),
    USER_SELF_DELETED_SUMMARY("user_self_deleted_summary"),
    USER_SELF_MODULE_STATS("user_self_module_stats"),

    ADMIN_GLOBAL_OVERVIEW("admin_global_overview"),
    ADMIN_USER_COUNT("admin_user_count"),
    ADMIN_AI_FAILURE_SUMMARY("admin_ai_failure_summary"),
    ADMIN_MODULE_STATS("admin_module_stats"),
    ADMIN_USER_RECORD_SUMMARY("admin_user_record_summary"),
    ADMIN_RECENT_ISSUES("admin_recent_issues"),

    STRUCTURED_QUERY("structured_query"),
    CLARIFICATION_REQUIRED("clarification_required"),
    UNSUPPORTED("unsupported");

    private final String value;

    DashboardIntentCapability(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static DashboardIntentCapability fromValue(String raw) {
        if (raw == null || raw.isBlank()) {
            return STRUCTURED_QUERY;
        }

        String normalized = raw.trim()
                .toUpperCase(Locale.ROOT)
                .replace("-", "_")
                .replace(" ", "_");

        try {
            return DashboardIntentCapability.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            return STRUCTURED_QUERY;
        }
    }
}
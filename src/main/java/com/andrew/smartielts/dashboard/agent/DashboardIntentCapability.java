package com.andrew.smartielts.dashboard.agent;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;
import java.util.Map;

public enum DashboardIntentCapability {

    USER_SELF_OVERVIEW("USER_SELF_OVERVIEW"),
    USER_SELF_RECENT_RECORDS("USER_SELF_RECENT_RECORDS"),
    USER_SELF_PROGRESS_SUMMARY("USER_SELF_PROGRESS_SUMMARY"),
    USER_SELF_DELETED_SUMMARY("USER_SELF_DELETED_SUMMARY"),
    USER_SELF_MODULE_STATS("USER_SELF_MODULE_STATS"),

    ADMIN_GLOBAL_OVERVIEW("ADMIN_GLOBAL_OVERVIEW"),
    ADMIN_USER_COUNT("ADMIN_USER_COUNT"),
    ADMIN_AI_FAILURE_SUMMARY("ADMIN_AI_FAILURE_SUMMARY"),
    ADMIN_MODULE_STATS("ADMIN_MODULE_STATS"),
    ADMIN_USER_RECORD_SUMMARY("ADMIN_USER_RECORD_SUMMARY"),
    ADMIN_RECENT_ISSUES("ADMIN_RECENT_ISSUES"),

    STRUCTURED_QUERY("STRUCTURED_QUERY"),
    CLARIFICATION_REQUIRED("CLARIFICATION_REQUIRED"),
    UNSUPPORTED("UNSUPPORTED");

    private static final Map<String, String> ALIAS = Map.ofEntries(
            Map.entry("USERSELFOVERVIEW", "USER_SELF_OVERVIEW"),
            Map.entry("USERSELFRECENTRECORDS", "USER_SELF_RECENT_RECORDS"),
            Map.entry("USERSELFTESTHISTORY", "USER_SELF_RECENT_RECORDS"),
            Map.entry("USERSELFHISTORY", "USER_SELF_RECENT_RECORDS"),
            Map.entry("USERSELFLATESTRECORD", "USER_SELF_RECENT_RECORDS"),
            Map.entry("USERSELFPROGRESSSUMMARY", "USER_SELF_PROGRESS_SUMMARY"),
            Map.entry("USERSELFDELETEDSUMMARY", "USER_SELF_DELETED_SUMMARY"),
            Map.entry("USERSELFMODULESTATS", "USER_SELF_MODULE_STATS"),

            Map.entry("ADMINGLOBALOVERVIEW", "ADMIN_GLOBAL_OVERVIEW"),
            Map.entry("ADMINUSERCOUNT", "ADMIN_USER_COUNT"),
            Map.entry("ADMINAIFAILURESUMMARY", "ADMIN_AI_FAILURE_SUMMARY"),
            Map.entry("ADMINMODULESTATS", "ADMIN_MODULE_STATS"),
            Map.entry("ADMINUSERRECORDSUMMARY", "ADMIN_USER_RECORD_SUMMARY"),
            Map.entry("ADMINRECENTISSUES", "ADMIN_RECENT_ISSUES"),

            Map.entry("STRUCTUREDQUERY", "STRUCTURED_QUERY"),
            Map.entry("CLARIFICATIONREQUIRED", "CLARIFICATION_REQUIRED"),
            Map.entry("ADMINUSERACTIVITYRANKING", "STRUCTURED_QUERY")
    );

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
            throw new IllegalArgumentException("DashboardIntentCapability cannot be blank");
        }

        String normalized = raw.trim().toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');

        normalized = ALIAS.getOrDefault(normalized, normalized);

        return DashboardIntentCapability.valueOf(normalized);
    }
}
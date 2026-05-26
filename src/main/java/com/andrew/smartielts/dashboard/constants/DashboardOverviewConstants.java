package com.andrew.smartielts.dashboard.constants;

public final class DashboardOverviewConstants {

    private DashboardOverviewConstants() {
    }

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";

    public static final String PAGE_NAME_ADMIN_OVERVIEW = "admin_overview";
    public static final String PAGE_NAME_USER_OVERVIEW = "user_overview";

    public static final String ASK_SCENE_CHAT = "CHAT";
    public static final String RESPONSE_MODE_DEFAULT = "default";
    public static final String RESPONSE_LANGUAGE_ZH_HANT = "zh-Hant";
    public static final String RESPONSE_LANGUAGE_EN = "en";

    public static final String QUERY_PARAM_TARGET_USER_ID = "target_user_id";
    public static final String QUERY_PARAM_TIME_RANGE = "time_range";
    public static final String QUERY_PARAM_SUMMARY_CACHE_KEY = "summary_cache_key";

    public static final String DEFAULT_TIME_RANGE = "last30days";

    // Shared keys used by ask, preload, and intent flows.
    public static final String CONTEXT_KEY_TIME_RANGE = "timeRange";
    public static final String CONTEXT_KEY_MODULE = "module";

    public static final String SUMMARY_TYPE_AI = "ai_summary";
    public static final String SUMMARY_SOURCE_PRELOAD_PLUS_ASK = "preload_plus_ask";
}

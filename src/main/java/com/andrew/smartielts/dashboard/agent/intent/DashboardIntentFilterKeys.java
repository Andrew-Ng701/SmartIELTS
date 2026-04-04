package com.andrew.smartielts.dashboard.agent.intent;

public final class DashboardIntentFilterKeys {

    private DashboardIntentFilterKeys() {
    }

    // module / modules 變形
    public static final String MODULE = "module";
    public static final String MODULES = "modules";

    // time / status / aggregation / paging / sort
    public static final String TIME_RANGE = "timeRange";
    public static final String STATUS = "status";
    public static final String AGGREGATION = "aggregation";
    public static final String LIMIT = "limit";
    public static final String SORT_BY = "sortBy";
    public static final String SORT_DIRECTION = "sortDirection";

    // metric 相關所有可能變形
    public static final String METRIC_FOCUS = "metricFocus";
    public static final String METRICS = "metrics";
    public static final String FOCUS_METRIC = "focusMetric";
}
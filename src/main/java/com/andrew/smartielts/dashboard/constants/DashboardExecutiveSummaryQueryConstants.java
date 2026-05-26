package com.andrew.smartielts.dashboard.constants;

public final class DashboardExecutiveSummaryQueryConstants {

    private DashboardExecutiveSummaryQueryConstants() {
    }

    public static final String ADMIN_EXECUTIVE_SUMMARY_DEFAULT_QUERY =
            "Write a concise English executive summary for the admin dashboard. "
                    + "Use only global admin dashboard and admin console facts. Do not describe the admin account "
                    + "as a learner, test taker, or target user, and do not mention records completed by user 1. "
                    + "Cover platform-wide users, records, module activity, AI failure signals if present, "
                    + "and the 1-2 most important operational actions. "
                    + "Keep it to 2-3 short sentences.";

    public static final String USER_EXECUTIVE_SUMMARY_DEFAULT_QUERY =
            "Write a concise English executive summary for the user's IELTS dashboard. "
                    + "Use only the preloaded dashboard facts. Cover recent activity, progress against targets, "
                    + "strongest or weakest modules when available, and 1-2 next study priorities. "
                    + "Keep it to 2-3 short sentences.";
}

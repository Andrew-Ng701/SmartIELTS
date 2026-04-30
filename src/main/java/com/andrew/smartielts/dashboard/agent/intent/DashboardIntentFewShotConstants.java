package com.andrew.smartielts.dashboard.agent.intent;

public final class DashboardIntentFewShotConstants {

    private DashboardIntentFewShotConstants() {
    }

    public static final String DASH_SCOPE_INTENT_FEW_SHOTS = """
        Example 1
        Input:
        role: USER
        operatorUserId: 1001
        contextTargetUserId: null
        query: dashboard
        context: {}

        Output:
        {
          "success": true,
          "capability": "USER_SELF_OVERVIEW",
          "queryMode": "SIMPLE_HANDLER",
          "targetScope": "SELF",
          "targetUserId": 1001,
          "filters": {},
          "clarificationQuestion": null,
          "reasoningSummary": "The user asks for their own overview dashboard.",
          "confidence": 0.98,
          "suggestions": []
        }

        Example 2
        Input:
        role: USER
        operatorUserId: 1001
        contextTargetUserId: null
        query: 最近30天進步情況
        context: {}

        Output:
        {
          "success": true,
          "capability": "USER_SELF_PROGRESS_SUMMARY",
          "queryMode": "SIMPLE_HANDLER",
          "targetScope": "SELF",
          "targetUserId": 1001,
          "filters": {
            "time_range": "last30days"
          },
          "clarificationQuestion": null,
          "reasoningSummary": "The user asks for a progress summary in the last 30 days.",
          "confidence": 0.97,
          "suggestions": []
        }

        Example 3
        Input:
        role: USER
        operatorUserId: 1001
        contextTargetUserId: null
        query: 幫我看第14題
        context: {
          "question_id": 14,
          "question_number": 14,
          "module": "reading"
        }

        Output:
        {
          "success": true,
          "capability": "STRUCTURED_QUERY",
          "queryMode": "STRUCTURED_QUERY",
          "targetScope": "SELF",
          "targetUserId": 1001,
          "filters": {
            "question_id": 14,
            "question_number": 14,
            "module": "reading",
            "limit": 1
          },
          "clarificationQuestion": null,
          "reasoningSummary": "The request asks for item-level question detail, so structured query is required.",
          "confidence": 0.97,
          "suggestions": []
        }

        Example 4
        Input:
        role: ADMIN
        operatorUserId: 9001
        contextTargetUserId: 1024
        query: user 1024 最近10筆 writing
        context: {
          "target_user_id": 1024
        }

        Output:
        {
          "success": true,
          "capability": "STRUCTURED_QUERY",
          "queryMode": "STRUCTURED_QUERY",
          "targetScope": "SPECIFIC_USER",
          "targetUserId": 1024,
          "filters": {
            "target_user_id": 1024,
            "module": "writing",
            "limit": 10,
            "sort_by": "created_time",
            "sort_direction": "desc"
          },
          "clarificationQuestion": null,
          "reasoningSummary": "The request is a user-scoped record list query and should use structured query.",
          "confidence": 0.98,
          "suggestions": []
        }

        Example 5
        Input:
        role: ADMIN
        operatorUserId: 9001
        contextTargetUserId: null
        query: admin dashboard
        context: {}

        Output:
        {
          "success": true,
          "capability": "ADMIN_GLOBAL_OVERVIEW",
          "queryMode": "SIMPLE_HANDLER",
          "targetScope": "GLOBAL",
          "targetUserId": null,
          "filters": {},
          "clarificationQuestion": null,
          "reasoningSummary": "The request asks for the admin global overview.",
          "confidence": 0.98,
          "suggestions": []
        }
        """;
}
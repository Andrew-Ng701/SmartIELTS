package com.andrew.smartielts.dashboard.agent.intent;

public final class DashboardIntentSchemaConstants {

    public static final String DASHSCOPE_INTENT_JSON_SCHEMA = """
{
  "type": "object",
  "additionalProperties": false,
  "required": ["success", "capability", "queryMode", "targetScope", "targetUserId",
               "filters", "clarificationQuestion", "reasoningSummary", "confidence", "suggestions"],
  "properties": {
    "success": { "type": "boolean" },
    "capability": {
      "type": "string",
      "enum": [
          "USER_SELF_OVERVIEW",
          "USER_SELF_RECENT_RECORDS",
          "USER_SELF_PROGRESS_SUMMARY",
          "USER_SELF_DELETED_SUMMARY",
          "USER_SELF_MODULE_STATS",
          "ADMIN_GLOBAL_OVERVIEW",
          "ADMIN_USER_COUNT",
          "ADMIN_AI_FAILURE_SUMMARY",
          "ADMIN_MODULE_STATS",
          "ADMIN_USER_RECORD_SUMMARY",
          "ADMIN_RECENT_ISSUES",
          "STRUCTURED_QUERY",
          "CLARIFICATION_REQUIRED",
          "UNSUPPORTED"
       ]
    },
    "queryMode": {
      "type": "string",
      "enum": ["SIMPLE_HANDLER", "STRUCTURED_QUERY", "CLARIFICATION", "UNSUPPORTED"]
    },
    "targetScope": {
      "type": "string",
      "enum": ["SELF", "SPECIFIC_USER", "GLOBAL"]
    },
    "targetUserId": {
      "type": ["integer", "null"]
    },
    "filters": {
      "type": "object",
      "additionalProperties": true
    },
    "clarificationQuestion": {
      "type": ["string", "null"]
    },
    "reasoningSummary": {
      "type": "string"
    },
    "confidence": {
      "type": "number",
      "minimum": 0,
      "maximum": 1
    },
    "suggestions": {
      "type": "array",
      "items": { "type": "string" }
    }
  }
}
""";
}
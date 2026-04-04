package com.andrew.smartielts.dashboard.agent.ask;

public final class DashboardAskDecisionSchemaConstants {

    private DashboardAskDecisionSchemaConstants() {
    }

    public static final String DASHSCOPE_ASK_DECISION_JSON_SCHEMA = """
{
  "type": "object",
  "additionalProperties": false,
  "required": [
    "action",
    "sufficient",
    "answer",
    "capability",
    "filters",
    "reviewSummary",
    "requiredDataScopes",
    "suggestions",
    "meta"
  ],
  "properties": {
    "action": {
      "type": "string",
      "enum": ["DIRECT_ANSWER", "GENERATE_SQL", "NEED_CLARIFICATION", "EXIT"]
    },
    "sufficient": {
      "type": "boolean"
    },
    "answer": {
      "type": ["string", "null"]
    },
    "capability": {
      "type": ["string", "null"],
      "enum": [
        "USERSELFOVERVIEW",
        "USERSELFRECENTRECORDS",
        "USERSELFPROGRESSSUMMARY",
        "USERSELFDELETEDSUMMARY",
        "USERSELFMODULESTATS",
        "ADMINGLOBALOVERVIEW",
        "ADMINUSERCOUNT",
        "ADMINAIFAILURESUMMARY",
        "ADMINMODULESTATS",
        "ADMINUSERRECORDSUMMARY",
        "ADMINRECENTISSUES",
        "STRUCTUREDQUERY",
        null
      ]
    },
    "filters": {
      "type": "object",
      "additionalProperties": true
    },
    "reviewSummary": {
      "type": "string"
    },
    "requiredDataScopes": {
      "type": "array",
      "items": {
        "type": "string"
      }
    },
    "suggestions": {
      "type": "array",
      "items": {
        "type": "string"
      }
    },
    "meta": {
      "type": "object",
      "additionalProperties": true
    }
  }
}
""";
}
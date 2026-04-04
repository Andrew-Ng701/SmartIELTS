package com.andrew.smartielts.dashboard.query;

public final class DashboardSqlSchemaConstants {

    private DashboardSqlSchemaConstants() {
    }

    public static final String DASHSCOPE_SQL_GENERATION_JSON_SCHEMA = """
            {
              "type": "object",
              "additionalProperties": false,
              "required": [
                "success",
                "sql",
                "params",
                "expectedColumns",
                "queryPurpose",
                "reasoningSummary",
                "confidence",
                "suggestions"
              ],
              "properties": {
                "success": {
                  "type": "boolean"
                },
                "sql": {
                  "type": "string"
                },
                "params": {
                  "type": "object",
                  "additionalProperties": {
                    "type": ["string", "number", "integer", "boolean", "null"]
                  }
                },
                "expectedColumns": {
                  "type": "array",
                  "items": {
                    "type": "string"
                  }
                },
                "queryPurpose": {
                  "type": "string"
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
                  "items": {
                    "type": "string"
                  }
                }
              }
            }
            """;

    public static final String DASHSCOPE_SQL_REVIEW_JSON_SCHEMA = """
            {
              "type": "object",
              "additionalProperties": false,
              "required": [
                "answer",
                "data",
                "suggestions",
                "meta"
              ],
              "properties": {
                "answer": {
                  "type": "string"
                },
                "data": {
                  "type": ["object", "array", "null"]
                },
                "suggestions": {
                  "type": "array",
                  "items": {
                    "type": "string"
                  }
                },
                "meta": {
                  "type": "object",
                  "additionalProperties": false,
                  "required": [
                    "reviewAction",
                    "reviewSummary"
                  ],
                  "properties": {
                    "reviewAction": {
                      "type": "string",
                      "enum": ["PROCEED", "PARTIAL", "INSUFFICIENT"]
                    },
                    "reviewSummary": {
                      "type": "string"
                    }
                  }
                }
              }
            }
            """;
}
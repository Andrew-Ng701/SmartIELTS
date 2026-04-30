// query/DashboardSqlSchemaConstants.java
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
                "expected_columns",
                "query_purpose",
                "reasoning_summary",
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
                  "type": "object"
                },
                "expected_columns": {
                  "type": "array",
                  "items": {
                    "type": "string"
                  }
                },
                "query_purpose": {
                  "type": "string"
                },
                "reasoning_summary": {
                  "type": "string"
                },
                "confidence": {
                  "type": "number"
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
                  "type": "object"
                }
              }
            }
            """;
}
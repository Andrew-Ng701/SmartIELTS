package com.andrew.smartielts.dashboard.agent.intent;

public final class DashboardIntentFewShotConstants {

    public static final String DASHSCOPE_INTENT_FEW_SHOTS = """
Example 1
Input:
role: USER
operatorUserId: 1001
contextTargetUserId: null
query: ...
context: ...

Output:
{
  "success": true,
  "capability": "USERSELFOVERVIEW",
  "queryMode": "SIMPLEHANDLER",
  "targetScope": "SELF",
  "targetUserId": 1001,
  "filters": {},
  "clarificationQuestion": null,
  "reasoningSummary": "The user asks for their own overview dashboard.",
  "confidence": 0.98,
  "suggestions": []
}

Example 2
...
  "capability": "USERSELFMODULESTATS",
  "queryMode": "SIMPLEHANDLER",
  "targetScope": "SELF",
...

Example 3
...
  "capability": "ADMINGLOBALOVERVIEW",
  "queryMode": "SIMPLEHANDLER",
  "targetScope": "GLOBAL",
...

Example 4
...
  "capability": "ADMINUSERRECORDSUMMARY",
  "queryMode": "SIMPLEHANDLER",
  "targetScope": "SPECIFICUSER",
...
""";
}
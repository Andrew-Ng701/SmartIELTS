package com.andrew.smartielts.dashboard.agent.ask.llm;

import com.andrew.smartielts.dashboard.agent.ask.dto.DashboardAskDecisionRequest;
import com.andrew.smartielts.dashboard.agent.ask.dto.DashboardAskDecisionResult;

public interface DashboardAskDecisionLlmClient {

    DashboardAskDecisionResult decide(DashboardAskDecisionRequest request);
}
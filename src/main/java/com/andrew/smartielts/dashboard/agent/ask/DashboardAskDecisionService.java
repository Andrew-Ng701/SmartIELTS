package com.andrew.smartielts.dashboard.agent.ask;

import com.andrew.smartielts.dashboard.agent.ask.dto.DashboardAskDecisionRequest;
import com.andrew.smartielts.dashboard.agent.ask.dto.DashboardAskDecisionResult;

public interface DashboardAskDecisionService {

    DashboardAskDecisionResult decide(DashboardAskDecisionRequest request);
}
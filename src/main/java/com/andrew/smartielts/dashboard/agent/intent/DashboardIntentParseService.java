package com.andrew.smartielts.dashboard.agent.intent;

import com.andrew.smartielts.dashboard.agent.intent.dto.DashboardIntentParseRequest;
import com.andrew.smartielts.dashboard.agent.intent.dto.DashboardIntentParseResponse;

public interface DashboardIntentParseService {

    DashboardIntentParseResponse parse(DashboardIntentParseRequest request);
}
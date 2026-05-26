package com.andrew.smartielts.dashboard.agent;

import java.util.Map;

public interface DashboardAskProgressListener {

    void onDecisionResolved(String displayAnswer, Map<String, Object> meta);
}

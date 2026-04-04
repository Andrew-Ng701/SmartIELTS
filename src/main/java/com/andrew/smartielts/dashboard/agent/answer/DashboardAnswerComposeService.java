package com.andrew.smartielts.dashboard.agent.answer;

import com.andrew.smartielts.dashboard.agent.answer.dto.DashboardAnswerComposeRequest;
import com.andrew.smartielts.dashboard.agent.answer.dto.DashboardAnswerComposeResult;

public interface DashboardAnswerComposeService {

    DashboardAnswerComposeResult compose(DashboardAnswerComposeRequest request);
}
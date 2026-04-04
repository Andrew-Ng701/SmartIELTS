package com.andrew.smartielts.dashboard.agent.answer.llm;

import com.andrew.smartielts.dashboard.agent.answer.dto.DashboardAnswerRewriteRequest;
import com.andrew.smartielts.dashboard.agent.answer.dto.DashboardAnswerRewriteResult;

public interface DashboardAnswerRewriteLlmClient {

    DashboardAnswerRewriteResult rewrite(DashboardAnswerRewriteRequest request);
}
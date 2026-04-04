package com.andrew.smartielts.dashboard.agent.answer.llm;

import com.andrew.smartielts.dashboard.agent.answer.dto.DashboardAnswerReviewRequest;
import com.andrew.smartielts.dashboard.agent.answer.dto.DashboardAnswerReviewResult;

public interface DashboardAnswerReviewLlmClient {

    DashboardAnswerReviewResult review(DashboardAnswerReviewRequest request);
}
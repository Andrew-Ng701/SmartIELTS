package com.andrew.smartielts.dashboard.agent.answer;

import com.andrew.smartielts.dashboard.agent.answer.dto.DashboardAnswerReviewRequest;
import com.andrew.smartielts.dashboard.agent.answer.dto.DashboardAnswerReviewResult;

public interface DashboardAnswerReviewService {

    DashboardAnswerReviewResult review(DashboardAnswerReviewRequest request);
}
package com.andrew.smartielts.dashboard.agent.answer.impl;

import com.andrew.smartielts.dashboard.agent.answer.DashboardAnswerReviewService;
import com.andrew.smartielts.dashboard.agent.answer.dto.DashboardAnswerReviewRequest;
import com.andrew.smartielts.dashboard.agent.answer.dto.DashboardAnswerReviewResult;
import com.andrew.smartielts.dashboard.agent.answer.llm.DashboardAnswerReviewLlmClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class DashboardAiAnswerReviewService implements DashboardAnswerReviewService {

    private final DashboardAnswerReviewLlmClient reviewLlmClient;
    private final DashboardFallbackAnswerReviewService fallbackReviewService;

    @Override
    public DashboardAnswerReviewResult review(DashboardAnswerReviewRequest request) {
        try {
            return reviewLlmClient.review(request);
        } catch (Exception e) {
            log.warn("AI answer review failed, fallback to local review: {}", e.getMessage());
            return fallbackReviewService.review(request);
        }
    }
}
package com.andrew.smartielts.dashboard.query.llm;

import com.andrew.smartielts.dashboard.query.dto.DashboardSqlGenerationRequest;
import com.andrew.smartielts.dashboard.query.dto.DashboardSqlGenerationResult;
import com.andrew.smartielts.dashboard.query.dto.DashboardSqlReviewRequest;
import com.andrew.smartielts.dashboard.query.dto.DashboardSqlReviewResult;

public interface DashboardSqlLlmClient {
    DashboardSqlGenerationResult generateSql(DashboardSqlGenerationRequest request);

    DashboardSqlReviewResult reviewAnswer(DashboardSqlReviewRequest request);
}
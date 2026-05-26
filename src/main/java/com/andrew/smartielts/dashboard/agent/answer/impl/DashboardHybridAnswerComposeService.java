package com.andrew.smartielts.dashboard.agent.answer.impl;

import com.andrew.smartielts.dashboard.agent.answer.DashboardAnswerComposeService;
import com.andrew.smartielts.dashboard.agent.answer.DashboardSuggestionPerspectiveNormalizer;
import com.andrew.smartielts.dashboard.agent.answer.DashboardUserTargetScoreContext;
import com.andrew.smartielts.dashboard.agent.answer.dto.DashboardAnswerComposeRequest;
import com.andrew.smartielts.dashboard.agent.answer.dto.DashboardAnswerComposeResult;
import com.andrew.smartielts.dashboard.agent.answer.dto.DashboardAnswerRewriteRequest;
import com.andrew.smartielts.dashboard.agent.answer.dto.DashboardAnswerRewriteResult;
import com.andrew.smartielts.dashboard.agent.answer.llm.DashboardAnswerRewriteLlmClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class DashboardHybridAnswerComposeService implements DashboardAnswerComposeService {

    private final DashboardTemplateAnswerComposeService templateAnswerComposeService;
    private final DashboardAnswerRewriteLlmClient rewriteLlmClient;

    @Override
    public DashboardAnswerComposeResult compose(DashboardAnswerComposeRequest request) {
        DashboardAnswerComposeResult templateResult = templateAnswerComposeService.compose(request);
        Map<String, Object> userTargetScores = resolveUserTargetScores(request);

        try {
            DashboardAnswerRewriteResult rewriteResult = rewriteLlmClient.rewrite(
                    DashboardAnswerRewriteRequest.builder()
                            .role(request.getRole())
                            .originalQuery(request.getOriginalQuery())
                            .capability(request.getCapability())
                            .filters(request.getFilters())
                            .userTargetScores(userTargetScores)
                            .data(request.getData())
                            .factualSummary(templateResult.getAnswer())
                            .suggestions(templateResult.getSuggestions())
                            .responseLanguage(request.getResponseLanguage())
                            .build()
            );

            return DashboardAnswerComposeResult.builder()
                    .answer(rewriteResult.getAnswer())
                    .suggestions(DashboardSuggestionPerspectiveNormalizer.normalize(
                            rewriteResult.getSuggestions() == null || rewriteResult.getSuggestions().isEmpty()
                                    ? templateResult.getSuggestions()
                                    : rewriteResult.getSuggestions()))
                    .build();
        } catch (Exception e) {
            log.warn("AI answer rewrite failed, fallback to template answer {}", e.getMessage());
            return templateResult;
        }
    }

    private Map<String, Object> resolveUserTargetScores(DashboardAnswerComposeRequest request) {
        if (request == null) {
            return Map.of();
        }
        Map<String, Object> explicit = request.getUserTargetScores();
        if (DashboardUserTargetScoreContext.hasAnyScore(explicit)) {
            return explicit;
        }
        return DashboardUserTargetScoreContext.fromData(request.getData());
    }
}

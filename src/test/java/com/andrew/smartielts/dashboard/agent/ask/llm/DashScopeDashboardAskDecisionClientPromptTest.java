package com.andrew.smartielts.dashboard.agent.ask.llm;

import com.andrew.smartielts.dashboard.agent.ask.dto.DashboardAskDecisionRequest;
import com.andrew.smartielts.dashboard.agent.intent.DashboardIntentLlmProperties;
import com.andrew.smartielts.dashboard.controller.dto.DashboardAskConversationMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DashScopeDashboardAskDecisionClientPromptTest {

    @Test
    void includesConversationHistoryInDecisionPrompt() {
        DashScopeDashboardAskDecisionClient client = new DashScopeDashboardAskDecisionClient(
                null,
                new ObjectMapper(),
                new DashboardIntentLlmProperties()
        );

        DashboardAskConversationMessage previousUserMessage = new DashboardAskConversationMessage();
        previousUserMessage.setRole("user");
        previousUserMessage.setContent("請先解釋第 3 題為什麼錯");

        DashboardAskConversationMessage previousAssistantMessage = new DashboardAskConversationMessage();
        previousAssistantMessage.setRole("assistant");
        previousAssistantMessage.setContent("第 3 題主要錯在定位句理解。");

        DashboardAskDecisionRequest request = DashboardAskDecisionRequest.builder()
                .role("USER")
                .operatorUserId(2L)
                .targetUserId(2L)
                .query("那第 4 題呢？")
                .conversationHistory(List.of(previousUserMessage, previousAssistantMessage))
                .responseLanguage("zh-Hant")
                .build();

        String prompt = ReflectionTestUtils.invokeMethod(client, "buildUserPrompt", request);

        assertTrue(prompt.contains("CONVERSATION_HISTORY_JSON"));
        assertTrue(prompt.contains("請先解釋第 3 題為什麼錯"));
        assertTrue(prompt.contains("第 3 題主要錯在定位句理解。"));
        assertTrue(prompt.contains("那第 4 題呢？"));
    }
}

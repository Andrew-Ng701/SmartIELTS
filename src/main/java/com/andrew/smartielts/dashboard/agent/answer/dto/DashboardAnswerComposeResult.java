package com.andrew.smartielts.dashboard.agent.answer.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardAnswerComposeResult {
    private String answer;
    private List<String> suggestions;
}
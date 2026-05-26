package com.andrew.smartielts.dashboard.controller.dto;

import lombok.Data;

import java.util.Map;

@Data
public class DashboardAskConversationMessage {
    private String role;
    private String content;
    private String createdAt;
    private Map<String, Object> meta;
}

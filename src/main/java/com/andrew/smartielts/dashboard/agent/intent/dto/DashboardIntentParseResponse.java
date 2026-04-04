package com.andrew.smartielts.dashboard.agent.intent.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class DashboardIntentParseResponse {

    private Integer code;
    private String msg;
    private DashboardIntentParseResult data;
    private Map<String, Object> meta;
}
package com.andrew.smartielts.dashboard.controller.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardAssistantResponse {
    /**
     * 给前端直接显示的助手自然语言回复
     */
    private String answer;

    /**
     * 原始结构化数据，给前端做卡片 / 图表 / 表格
     */
    private Object data;

    /**
     * 快捷追问建议
     */
    private List<String> suggestions;

    /**
     * 元信息，便于调试 / 埋点 / SSE
     */
    private Map<String, Object> meta;
}
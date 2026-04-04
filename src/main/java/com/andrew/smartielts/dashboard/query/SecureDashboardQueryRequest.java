package com.andrew.smartielts.dashboard.query;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SecureDashboardQueryRequest {

    /**
     * 舊模板模式仍可保留，兼容現有 StructuredQueryHandler。
     */
    private QueryTemplateCode templateCode;

    /**
     * 新增：AI 生成 SQL 模式使用。
     */
    private String rawSql;

    /**
     * 是否為 AI 生成 SQL。
     */
    private boolean aiGenerated;

    private String role;
    private Long operatorUserId;
    private Long targetUserId;
    private Map<String, Object> params;

    /**
     * 審計與 review 用。
     */
    private String originalQuery;
    private String intentCapability;
    private List<String> expectedColumns;
}
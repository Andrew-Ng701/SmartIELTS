package com.andrew.smartielts.dashboard.query;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class SecureDashboardQueryRequest {

    private QueryTemplateCode templateCode;
    private String rawSql;
    private boolean aiGenerated;
    private String role;
    private Long operatorUserId;
    private Long targetUserId;
    private Map<String, Object> params = new LinkedHashMap<>();
    private String originalQuery;
    private String intentCapability;
    private List<String> expectedColumns;

    public String getSql() {
        return rawSql;
    }

    public void setSql(String sql) {
        this.rawSql = sql;
    }

    public String getCapability() {
        return intentCapability;
    }

    public void setCapability(String capability) {
        this.intentCapability = capability;
    }
}
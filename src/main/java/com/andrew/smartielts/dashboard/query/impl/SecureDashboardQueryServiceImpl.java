package com.andrew.smartielts.dashboard.query.impl;

import com.andrew.smartielts.dashboard.query.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SecureDashboardQueryServiceImpl implements SecureDashboardQueryService {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final DashboardSqlTemplateRegistry sqlTemplateRegistry;
    private final DashboardQueryPermissionGuard permissionGuard;
    private final ReadOnlySqlGuard readOnlySqlGuard;
    private final DashboardAiSqlPolicyGuard dashboardAiSqlPolicyGuard;
    private final DashboardSqlRewriter dashboardSqlRewriter;

    @Override
    public List<Map<String, Object>> execute(SecureDashboardQueryRequest request) {
        permissionGuard.validate(request);

        if (request.isAiGenerated()) {
            return executeAiSql(request);
        }

        String sql = sqlTemplateRegistry.resolveSql(request);
        Map<String, Object> params = sqlTemplateRegistry.resolveParams(request);
        return jdbcTemplate.queryForList(sql, params);
    }

    private List<Map<String, Object>> executeAiSql(SecureDashboardQueryRequest request) {
        String sql = request.getRawSql();
        readOnlySqlGuard.validate(sql);
        dashboardAiSqlPolicyGuard.validate(sql, request);

        String rewrittenSql = dashboardSqlRewriter.rewrite(sql, request);
        Map<String, Object> params = buildSafeParams(request);

        return jdbcTemplate.queryForList(rewrittenSql, params);
    }

    private Map<String, Object> buildSafeParams(SecureDashboardQueryRequest request) {
        Map<String, Object> params = new HashMap<>();
        if (request.getParams() != null) {
            params.putAll(request.getParams());
        }

        params.put("operatorUserId", request.getOperatorUserId());
        params.put("targetUserId", request.getTargetUserId());

        Object limit = params.get("limit");
        int safeLimit = 20;
        if (limit instanceof Number number) {
            safeLimit = Math.min(Math.max(number.intValue(), 1), 100);
        }
        params.put("limit", safeLimit);
        return params;
    }
}
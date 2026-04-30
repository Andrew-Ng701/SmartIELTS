package com.andrew.smartielts.dashboard.query.impl;

import com.andrew.smartielts.dashboard.query.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecureDashboardQueryServiceImpl implements SecureDashboardQueryService {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final DashboardSqlTemplateRegistry sqlTemplateRegistry;
    private final DashboardQueryPermissionGuard permissionGuard;
    private final ReadOnlySqlGuard readOnlySqlGuard;
    private final DashboardAiSqlPolicyGuard dashboardAiSqlPolicyGuard;
    private final DashboardSqlRewriter dashboardSqlRewriter;

    @Override
    public List<Map<String, Object>> execute(SecureDashboardQueryRequest request) {
        long startedAt = System.currentTimeMillis();

        log.info("dashboard.secure.query.start role={} operatorUserId={} targetUserId={} aiGenerated={} intentCapability={}",
                request.getRole(),
                request.getOperatorUserId(),
                request.getTargetUserId(),
                request.isAiGenerated(),
                request.getIntentCapability());

        permissionGuard.validate(request);
        log.info("dashboard.secure.query.permission.validated role={} operatorUserId={} targetUserId={} elapsedMs={}",
                request.getRole(),
                request.getOperatorUserId(),
                request.getTargetUserId(),
                System.currentTimeMillis() - startedAt);

        if (request.isAiGenerated()) {
            return executeAiSql(request);
        }

        if (request.getTemplateCode() == null) {
            throw new IllegalArgumentException("templateCode is required when aiGenerated=false");
        }

        long sqlStartedAt = System.currentTimeMillis();
        String sql = sqlTemplateRegistry.resolveSql(request);
        Map<String, Object> params = sqlTemplateRegistry.resolveParams(request);

        log.info("dashboard.secure.query.template.start role={} operatorUserId={} targetUserId={} sql={} params={}",
                request.getRole(),
                request.getOperatorUserId(),
                request.getTargetUserId(),
                safeSql(sql),
                params);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params);

        log.info("dashboard.secure.query.template.done role={} operatorUserId={} targetUserId={} elapsedMs={} rowCount={}",
                request.getRole(),
                request.getOperatorUserId(),
                request.getTargetUserId(),
                System.currentTimeMillis() - sqlStartedAt,
                rows == null ? 0 : rows.size());

        return rows;
    }

    private List<Map<String, Object>> executeAiSql(SecureDashboardQueryRequest request) {
        long startedAt = System.currentTimeMillis();
        String sql = request.getRawSql();

        readOnlySqlGuard.validate(sql);

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

        params.put("operator_user_id", request.getOperatorUserId());
        params.put("target_user_id", request.getTargetUserId());

        Object limit = params.get("limit");
        if (limit instanceof Number number) {
            params.put("limit", Math.min(Math.max(number.intValue(), 1), 100));
        } else if (!params.containsKey("limit")) {
            params.put("limit", 20);
        }

        return params;
    }

    private String safeSql(String sql) {
        if (sql == null) {
            return "";
        }
        return sql.replaceAll("\\s+", " ").trim();
    }
}
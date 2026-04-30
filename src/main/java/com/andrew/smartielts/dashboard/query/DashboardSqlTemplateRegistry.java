package com.andrew.smartielts.dashboard.query;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DashboardSqlTemplateRegistry {

    public String resolveSql(SecureDashboardQueryRequest request) {
        return switch (request.getTemplateCode()) {
            case ADMIN_COUNT_USERS -> """
                    SELECT
                        COUNT(*) AS total_users,
                        SUM(CASE WHEN is_deleted = 0 THEN 1 ELSE 0 END) AS active_users,
                        SUM(CASE WHEN is_deleted = 1 THEN 1 ELSE 0 END) AS deleted_users
                    FROM sys_user
                    """;

            case ADMIN_MODULE_STATS -> """
                    SELECT 'listening' AS module,
                           SUM(CASE WHEN is_deleted = 0 THEN 1 ELSE 0 END) AS active_count,
                           SUM(CASE WHEN is_deleted = 1 THEN 1 ELSE 0 END) AS deleted_count
                    FROM listening_record
                    UNION ALL
                    SELECT 'reading' AS module,
                           SUM(CASE WHEN is_deleted = 0 THEN 1 ELSE 0 END) AS active_count,
                           SUM(CASE WHEN is_deleted = 1 THEN 1 ELSE 0 END) AS deleted_count
                    FROM reading_record
                    UNION ALL
                    SELECT 'writing' AS module,
                           SUM(CASE WHEN is_deleted = 0 THEN 1 ELSE 0 END) AS active_count,
                           SUM(CASE WHEN is_deleted = 1 THEN 1 ELSE 0 END) AS deleted_count
                    FROM writing_record
                    UNION ALL
                    SELECT 'speaking' AS module,
                           SUM(CASE WHEN is_deleted = 0 THEN 1 ELSE 0 END) AS active_count,
                           SUM(CASE WHEN is_deleted = 1 THEN 1 ELSE 0 END) AS deleted_count
                    FROM speaking_record
                    """;

            case ADMIN_AI_FAILURES -> """
                    SELECT 'writing' AS module, COUNT(*) AS failure_count
                    FROM writing_record
                    WHERE ai_status = 'FAILED'
                    UNION ALL
                    SELECT 'speaking' AS module, COUNT(*) AS failure_count
                    FROM speaking_record
                    WHERE ai_status = 'FAILED'
                    """;

            case ADMIN_USER_RECORDS -> """
                    SELECT
                        :targetUserId AS user_id,
                        (SELECT COUNT(*) FROM listening_record WHERE user_id = :targetUserId AND is_deleted = 0) AS listening_active,
                        (SELECT COUNT(*) FROM listening_record WHERE user_id = :targetUserId AND is_deleted = 1) AS listening_deleted,
                        (SELECT COUNT(*) FROM reading_record WHERE user_id = :targetUserId AND is_deleted = 0) AS reading_active,
                        (SELECT COUNT(*) FROM reading_record WHERE user_id = :targetUserId AND is_deleted = 1) AS reading_deleted,
                        (SELECT COUNT(*) FROM writing_record WHERE user_id = :targetUserId AND is_deleted = 0) AS writing_active,
                        (SELECT COUNT(*) FROM writing_record WHERE user_id = :targetUserId AND is_deleted = 1) AS writing_deleted,
                        (SELECT COUNT(*) FROM speaking_record WHERE user_id = :targetUserId AND is_deleted = 0) AS speaking_active,
                        (SELECT COUNT(*) FROM speaking_record WHERE user_id = :targetUserId AND is_deleted = 1) AS speaking_deleted
                    """;

            case ADMIN_RECENT_ISSUES -> """
                    SELECT 'writing' AS module, 'AI_FAILURE_SUMMARY' AS issue_type, COUNT(*) AS issue_count
                    FROM writing_record
                    WHERE ai_status = 'FAILED'
                    UNION ALL
                    SELECT 'speaking' AS module, 'AI_FAILURE_SUMMARY' AS issue_type, COUNT(*) AS issue_count
                    FROM speaking_record
                    WHERE ai_status = 'FAILED'
                    """;

            case ADMIN_TOP_ACTIVE_USERS -> """
                    SELECT
                        t.user_id,
                        COUNT(*) AS answer_attempts
                    FROM (
                        SELECT lr.user_id, lr.created_time
                        FROM listening_record lr
                        WHERE lr.is_deleted = 0
                          AND (:timeRange IS NULL
                               OR (:timeRange = 'last7days' AND lr.created_time >= DATE_SUB(NOW(), INTERVAL 7 DAY))
                               OR (:timeRange = 'last30days' AND lr.created_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)))
            
                        UNION ALL
            
                        SELECT rr.user_id, rr.created_time
                        FROM reading_record rr
                        WHERE rr.is_deleted = 0
                          AND (:timeRange IS NULL
                               OR (:timeRange = 'last7days' AND rr.created_time >= DATE_SUB(NOW(), INTERVAL 7 DAY))
                               OR (:timeRange = 'last30days' AND rr.created_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)))
            
                        UNION ALL
            
                        SELECT wr.user_id, wr.created_time
                        FROM writing_record wr
                        WHERE wr.is_deleted = 0
                          AND (:timeRange IS NULL
                               OR (:timeRange = 'last7days' AND wr.created_time >= DATE_SUB(NOW(), INTERVAL 7 DAY))
                               OR (:timeRange = 'last30days' AND wr.created_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)))
            
                        UNION ALL
            
                        SELECT sr.user_id, sr.created_time
                        FROM speaking_record sr
                        WHERE sr.is_deleted = 0
                          AND (:timeRange IS NULL
                               OR (:timeRange = 'last7days' AND sr.created_time >= DATE_SUB(NOW(), INTERVAL 7 DAY))
                               OR (:timeRange = 'last30days' AND sr.created_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)))
                    ) t
                    GROUP BY t.user_id
                    ORDER BY answer_attempts DESC, t.user_id ASC
                    LIMIT :limit
                    """;
            case USER_SELF_MODULE_STATS -> """
                    SELECT 'listening' AS module,
                           SUM(CASE WHEN is_deleted = 0 THEN 1 ELSE 0 END) AS active_count,
                           SUM(CASE WHEN is_deleted = 1 THEN 1 ELSE 0 END) AS deleted_count
                    FROM listening_record
                    WHERE user_id = :targetUserId
                    UNION ALL
                    SELECT 'reading' AS module,
                           SUM(CASE WHEN is_deleted = 0 THEN 1 ELSE 0 END) AS active_count,
                           SUM(CASE WHEN is_deleted = 1 THEN 1 ELSE 0 END) AS deleted_count
                    FROM reading_record
                    WHERE user_id = :targetUserId
                    UNION ALL
                    SELECT 'writing' AS module,
                           SUM(CASE WHEN is_deleted = 0 THEN 1 ELSE 0 END) AS active_count,
                           SUM(CASE WHEN is_deleted = 1 THEN 1 ELSE 0 END) AS deleted_count
                    FROM writing_record
                    WHERE user_id = :targetUserId
                    UNION ALL
                    SELECT 'speaking' AS module,
                           SUM(CASE WHEN is_deleted = 0 THEN 1 ELSE 0 END) AS active_count,
                           SUM(CASE WHEN is_deleted = 1 THEN 1 ELSE 0 END) AS deleted_count
                    FROM speaking_record
                    WHERE user_id = :targetUserId
                    """;

            case USER_SELF_RECENT_RECORDS -> """
                    SELECT * FROM (
                        SELECT 'listening' AS module, id, created_time
                        FROM listening_record
                        WHERE user_id = :targetUserId AND is_deleted = 0
                        UNION ALL
                        SELECT 'reading' AS module, id, created_time
                        FROM reading_record
                        WHERE user_id = :targetUserId AND is_deleted = 0
                        UNION ALL
                        SELECT 'writing' AS module, id, created_time
                        FROM writing_record
                        WHERE user_id = :targetUserId AND is_deleted = 0
                        UNION ALL
                        SELECT 'speaking' AS module, id, created_time
                        FROM speaking_record
                        WHERE user_id = :targetUserId AND is_deleted = 0
                    ) t
                    ORDER BY t.created_time DESC
                    LIMIT :limit
                    """;

            case USER_SELF_PROGRESS -> """
                    SELECT
                        NULL AS listening_average,
                        NULL AS reading_average,
                        NULL AS writing_average,
                        NULL AS speaking_average
                    """;

            case USER_SELF_DELETED_SUMMARY -> """
                    SELECT 'listening' AS module, COUNT(*) AS deleted_count
                    FROM listening_record
                    WHERE user_id = :targetUserId AND is_deleted = 1
                    UNION ALL
                    SELECT 'reading' AS module, COUNT(*) AS deleted_count
                    FROM reading_record
                    WHERE user_id = :targetUserId AND is_deleted = 1
                    UNION ALL
                    SELECT 'writing' AS module, COUNT(*) AS deleted_count
                    FROM writing_record
                    WHERE user_id = :targetUserId AND is_deleted = 1
                    UNION ALL
                    SELECT 'speaking' AS module, COUNT(*) AS deleted_count
                    FROM speaking_record
                    WHERE user_id = :targetUserId AND is_deleted = 1
                    """;
        };
    }

    public Map<String, Object> resolveParams(SecureDashboardQueryRequest request) {
        Map<String, Object> params = new HashMap<>();
        params.put("operator_user_id", request.getOperatorUserId());
        params.put("target_user_id", request.getTargetUserId());
        params.put("limit", request.getParams() == null ? 10 : request.getParams().getOrDefault("limit", 10));
        return params;
    }
}
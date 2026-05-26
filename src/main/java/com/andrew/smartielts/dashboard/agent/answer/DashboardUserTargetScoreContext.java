package com.andrew.smartielts.dashboard.agent.answer;

import com.andrew.smartielts.dashboard.controller.dto.DashboardAskPreloadedPayload;
import com.andrew.smartielts.dashboard.domain.vo.UserOverviewVO;

import java.util.LinkedHashMap;
import java.util.Map;

public final class DashboardUserTargetScoreContext {

    public static final String KEY_USER_TARGET_SCORES = "userTargetScores";
    public static final String KEY_LISTENING_TARGET_SCORE = "listeningTargetScore";
    public static final String KEY_READING_TARGET_SCORE = "readingTargetScore";
    public static final String KEY_WRITING_TARGET_SCORE = "writingTargetScore";
    public static final String KEY_SPEAKING_TARGET_SCORE = "speakingTargetScore";

    private DashboardUserTargetScoreContext() {
    }

    public static Map<String, Object> fromOverview(Object overview) {
        if (overview instanceof UserOverviewVO vo) {
            return build(
                    vo.getListeningTargetScore(),
                    vo.getReadingTargetScore(),
                    vo.getWritingTargetScore(),
                    vo.getSpeakingTargetScore()
            );
        }

        if (overview instanceof Map<?, ?> map) {
            return build(
                    firstValue(map, KEY_LISTENING_TARGET_SCORE, "listening_target_score"),
                    firstValue(map, KEY_READING_TARGET_SCORE, "reading_target_score"),
                    firstValue(map, KEY_WRITING_TARGET_SCORE, "writing_target_score"),
                    firstValue(map, KEY_SPEAKING_TARGET_SCORE, "speaking_target_score")
            );
        }

        return new LinkedHashMap<>();
    }

    public static Map<String, Object> fromPreloadedPayload(DashboardAskPreloadedPayload payload) {
        return payload == null ? new LinkedHashMap<>() : fromOverview(payload.getOverview());
    }

    public static Map<String, Object> fromContext(Map<String, Object> context) {
        if (context == null || context.isEmpty()) {
            return new LinkedHashMap<>();
        }

        Map<String, Object> direct = fromTargetScoreObject(context.get(KEY_USER_TARGET_SCORES));
        if (hasAnyScore(direct)) {
            return direct;
        }

        Map<String, Object> overview = fromOverview(context.get("overview"));
        if (hasAnyScore(overview)) {
            return overview;
        }

        Object preloadedPayload = firstValue(context, "preloadedPayload", "preloaded_payload");
        return fromTargetScoreObject(preloadedPayload);
    }

    public static Map<String, Object> fromData(Object data) {
        return fromTargetScoreObject(data);
    }

    public static boolean hasAnyScore(Map<String, Object> targetScores) {
        if (targetScores == null || targetScores.isEmpty()) {
            return false;
        }
        return hasValue(targetScores.get(KEY_LISTENING_TARGET_SCORE))
                || hasValue(targetScores.get(KEY_READING_TARGET_SCORE))
                || hasValue(targetScores.get(KEY_WRITING_TARGET_SCORE))
                || hasValue(targetScores.get(KEY_SPEAKING_TARGET_SCORE));
    }

    private static Map<String, Object> fromTargetScoreObject(Object source) {
        if (source instanceof DashboardAskPreloadedPayload payload) {
            return fromPreloadedPayload(payload);
        }
        if (!(source instanceof Map<?, ?> map)) {
            return fromOverview(source);
        }

        Map<String, Object> direct = build(
                firstValue(map, KEY_LISTENING_TARGET_SCORE, "listening_target_score"),
                firstValue(map, KEY_READING_TARGET_SCORE, "reading_target_score"),
                firstValue(map, KEY_WRITING_TARGET_SCORE, "writing_target_score"),
                firstValue(map, KEY_SPEAKING_TARGET_SCORE, "speaking_target_score")
        );
        if (hasAnyScore(direct)) {
            return direct;
        }

        Object nestedTargetScores = map.get(KEY_USER_TARGET_SCORES);
        if (nestedTargetScores != source) {
            Map<String, Object> nested = fromTargetScoreObject(nestedTargetScores);
            if (hasAnyScore(nested)) {
                return nested;
            }
        }

        Object overview = map.get("overview");
        if (overview != source) {
            Map<String, Object> overviewScores = fromOverview(overview);
            if (hasAnyScore(overviewScores)) {
                return overviewScores;
            }
        }

        Object preloadedPayload = firstValue(map, "preloadedPayload", "preloaded_payload");
        if (preloadedPayload != source) {
            return fromTargetScoreObject(preloadedPayload);
        }

        return new LinkedHashMap<>();
    }

    private static Map<String, Object> build(Object listening, Object reading, Object writing, Object speaking) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put(KEY_LISTENING_TARGET_SCORE, listening);
        result.put(KEY_READING_TARGET_SCORE, reading);
        result.put(KEY_WRITING_TARGET_SCORE, writing);
        result.put(KEY_SPEAKING_TARGET_SCORE, speaking);
        return result;
    }

    private static Object firstValue(Map<?, ?> map, String camelKey, String snakeKey) {
        Object camelValue = map.get(camelKey);
        return camelValue != null ? camelValue : map.get(snakeKey);
    }

    private static boolean hasValue(Object value) {
        return value != null && (!(value instanceof String text) || !text.isBlank());
    }
}

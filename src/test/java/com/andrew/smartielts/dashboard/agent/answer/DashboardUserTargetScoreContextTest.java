package com.andrew.smartielts.dashboard.agent.answer;

import com.andrew.smartielts.dashboard.controller.dto.DashboardAskPreloadedPayload;
import com.andrew.smartielts.dashboard.domain.vo.UserOverviewVO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DashboardUserTargetScoreContextTest {

    @Test
    void extractsTargetScoresFromPreloadedUserOverview() {
        UserOverviewVO overview = new UserOverviewVO();
        overview.setListeningTargetScore(new BigDecimal("7.0"));
        overview.setReadingTargetScore(new BigDecimal("7.5"));
        overview.setWritingTargetScore(new BigDecimal("6.5"));
        overview.setSpeakingTargetScore(new BigDecimal("7.0"));

        DashboardAskPreloadedPayload payload = new DashboardAskPreloadedPayload();
        payload.setOverview(overview);

        Map<String, Object> result = DashboardUserTargetScoreContext.fromPreloadedPayload(payload);

        assertEquals(new BigDecimal("7.0"), result.get("listeningTargetScore"));
        assertEquals(new BigDecimal("7.5"), result.get("readingTargetScore"));
        assertEquals(new BigDecimal("6.5"), result.get("writingTargetScore"));
        assertEquals(new BigDecimal("7.0"), result.get("speakingTargetScore"));
        assertTrue(DashboardUserTargetScoreContext.hasAnyScore(result));
    }

    @Test
    void extractsTargetScoresFromStructuredQueryContext() {
        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("listeningTargetScore", "7.0");
        overview.put("readingTargetScore", "7.5");
        overview.put("writingTargetScore", "6.5");
        overview.put("speakingTargetScore", "7.0");

        DashboardAskPreloadedPayload payload = new DashboardAskPreloadedPayload();
        payload.setOverview(overview);

        Map<String, Object> context = new LinkedHashMap<>();
        context.put("preloadedPayload", payload);

        Map<String, Object> result = DashboardUserTargetScoreContext.fromContext(context);

        assertEquals("7.0", result.get("listeningTargetScore"));
        assertEquals("7.5", result.get("readingTargetScore"));
        assertEquals("6.5", result.get("writingTargetScore"));
        assertEquals("7.0", result.get("speakingTargetScore"));
    }
}

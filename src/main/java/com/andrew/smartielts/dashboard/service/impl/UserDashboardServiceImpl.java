package com.andrew.smartielts.dashboard.service.impl;

import com.andrew.smartielts.dashboard.domain.vo.UserModuleStatVO;
import com.andrew.smartielts.dashboard.domain.vo.UserOverviewVO;
import com.andrew.smartielts.dashboard.domain.vo.UserProgressSummaryVO;
import com.andrew.smartielts.dashboard.domain.vo.UserRecentRecordVO;
import com.andrew.smartielts.dashboard.service.UserDashboardService;
import com.andrew.smartielts.listening.domain.pojo.ListeningRecord;
import com.andrew.smartielts.listening.domain.query.user.UserListeningDeletedRecordPageQuery;
import com.andrew.smartielts.listening.domain.query.user.UserListeningRecordPageQuery;
import com.andrew.smartielts.listening.mapper.ListeningRecordMapper;
import com.andrew.smartielts.reading.domain.pojo.ReadingRecord;
import com.andrew.smartielts.reading.domain.query.user.UserReadingDeletedRecordPageQuery;
import com.andrew.smartielts.reading.domain.query.user.UserReadingRecordPageQuery;
import com.andrew.smartielts.reading.mapper.ReadingRecordMapper;
import com.andrew.smartielts.speaking.domain.pojo.SpeakingRecord;
import com.andrew.smartielts.speaking.domain.query.user.UserSpeakingDeletedRecordPageQuery;
import com.andrew.smartielts.speaking.domain.query.user.UserSpeakingRecordPageQuery;
import com.andrew.smartielts.speaking.mapper.SpeakingRecordMapper;
import com.andrew.smartielts.writing.domain.pojo.WritingRecord;
import com.andrew.smartielts.writing.domain.query.user.UserWritingDeletedRecordPageQuery;
import com.andrew.smartielts.writing.domain.query.user.UserWritingRecordPageQuery;
import com.andrew.smartielts.writing.mapper.WritingRecordMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.andrew.smartielts.dashboard.agent.DashboardIntentExecutionFacade;
import com.andrew.smartielts.dashboard.constants.DashboardExecutiveSummaryQueryConstants;
import com.andrew.smartielts.dashboard.constants.DashboardOverviewConstants;
import com.andrew.smartielts.dashboard.controller.dto.DashboardAskClientContext;
import com.andrew.smartielts.dashboard.controller.dto.DashboardAskPreloadedPayload;
import com.andrew.smartielts.dashboard.controller.dto.DashboardAskRequest;
import com.andrew.smartielts.dashboard.controller.dto.DashboardAssistantResponse;
import com.andrew.smartielts.dashboard.domain.vo.UserDashboardOverviewVisualVO;
import com.andrew.smartielts.dashboard.domain.vo.UserExecutiveSummaryVO;
import com.andrew.smartielts.dashboard.domain.vo.UserProgressSummaryVO;
import com.andrew.smartielts.dashboard.preload.DashboardPreloadService;
import org.springframework.beans.factory.ObjectProvider;
import com.andrew.smartielts.dashboard.agent.answer.DashboardAnswerComposeService;
import com.andrew.smartielts.dashboard.agent.answer.dto.DashboardAnswerComposeRequest;
import com.andrew.smartielts.dashboard.agent.answer.dto.DashboardAnswerComposeResult;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDashboardServiceImpl implements UserDashboardService {

    private final ListeningRecordMapper listeningRecordMapper;
    private final ReadingRecordMapper readingRecordMapper;
    private final WritingRecordMapper writingRecordMapper;
    private final SpeakingRecordMapper speakingRecordMapper;
    private final ObjectProvider<DashboardPreloadService> dashboardPreloadServiceProvider;
    private final ObjectProvider<DashboardIntentExecutionFacade> executionFacadeProvider;
    private final ObjectMapper objectMapper;
    private final DashboardAnswerComposeService dashboardAnswerComposeService;

    @Override
    public UserOverviewVO overview(Long userId) {
        long listeningActive = safeLong(
                listeningRecordMapper.countUserActive(userId, new UserListeningRecordPageQuery())
        );
        long listeningDeleted = safeLong(
                listeningRecordMapper.countUserDeleted(userId, new UserListeningDeletedRecordPageQuery())
        );

        long readingActive = safeLong(
                readingRecordMapper.countUserActive(userId, new UserReadingRecordPageQuery())
        );
        long readingDeleted = safeLong(
                readingRecordMapper.countUserDeleted(userId, new UserReadingDeletedRecordPageQuery())
        );

        long writingActive = safeLong(
                writingRecordMapper.countUserActive(userId, new UserWritingRecordPageQuery())
        );
        long writingDeleted = safeLong(
                writingRecordMapper.countUserDeleted(userId, new UserWritingDeletedRecordPageQuery())
        );

        long speakingActive = safeLong(
                speakingRecordMapper.countUserActive(userId, new UserSpeakingRecordPageQuery())
        );
        long speakingDeleted = safeLong(
                speakingRecordMapper.countUserDeleted(userId, new UserSpeakingDeletedRecordPageQuery())
        );

        UserOverviewVO vo = new UserOverviewVO();
        vo.setListeningActiveRecords(listeningActive);
        vo.setListeningDeletedRecords(listeningDeleted);
        vo.setReadingActiveRecords(readingActive);
        vo.setReadingDeletedRecords(readingDeleted);
        vo.setWritingActiveRecords(writingActive);
        vo.setWritingDeletedRecords(writingDeleted);
        vo.setSpeakingActiveRecords(speakingActive);
        vo.setSpeakingDeletedRecords(speakingDeleted);
        vo.setTotalActiveRecords(listeningActive + readingActive + writingActive + speakingActive);
        vo.setTotalDeletedRecords(listeningDeleted + readingDeleted + writingDeleted + speakingDeleted);
        vo.setGeneratedAt(LocalDateTime.now());
        return vo;
    }

    @Override
    public List<UserModuleStatVO> deletedSummary(Long userId) {
        return List.of(
                moduleStat("listening", 0L,
                        safeLong(listeningRecordMapper.countUserDeleted(userId, new UserListeningDeletedRecordPageQuery()))),
                moduleStat("reading", 0L,
                        safeLong(readingRecordMapper.countUserDeleted(userId, new UserReadingDeletedRecordPageQuery()))),
                moduleStat("writing", 0L,
                        safeLong(writingRecordMapper.countUserDeleted(userId, new UserWritingDeletedRecordPageQuery()))),
                moduleStat("speaking", 0L,
                        safeLong(speakingRecordMapper.countUserDeleted(userId, new UserSpeakingDeletedRecordPageQuery())))
        );
    }

    @Override
    public List<UserModuleStatVO> userStats(Long userId) {
        return List.of(
                moduleStat("listening",
                        safeLong(listeningRecordMapper.countUserActive(userId, new UserListeningRecordPageQuery())),
                        safeLong(listeningRecordMapper.countUserDeleted(userId, new UserListeningDeletedRecordPageQuery()))),
                moduleStat("reading",
                        safeLong(readingRecordMapper.countUserActive(userId, new UserReadingRecordPageQuery())),
                        safeLong(readingRecordMapper.countUserDeleted(userId, new UserReadingDeletedRecordPageQuery()))),
                moduleStat("writing",
                        safeLong(writingRecordMapper.countUserActive(userId, new UserWritingRecordPageQuery())),
                        safeLong(writingRecordMapper.countUserDeleted(userId, new UserWritingDeletedRecordPageQuery()))),
                moduleStat("speaking",
                        safeLong(speakingRecordMapper.countUserActive(userId, new UserSpeakingRecordPageQuery())),
                        safeLong(speakingRecordMapper.countUserDeleted(userId, new UserSpeakingDeletedRecordPageQuery())))
        );
    }

    @Override
    public List<UserRecentRecordVO> recentRecords(Long userId) {
        List<UserRecentRecordVO> result = new ArrayList<>();

        result.addAll(toListeningRecentRecords(
                listeningRecordMapper.findRecentActiveByUserId(userId, 5)
        ));
        result.addAll(toReadingRecentRecords(
                readingRecordMapper.findRecentActiveByUserId(userId, 5)
        ));
        result.addAll(toWritingRecentRecords(
                writingRecordMapper.findRecentActiveByUserId(userId, 5)
        ));
        result.addAll(toSpeakingRecentRecords(
                speakingRecordMapper.findRecentActiveByUserId(userId, 5)
        ));

        return result.stream()
                .sorted((a, b) -> {
                    if (a.getCreatedTime() == null && b.getCreatedTime() == null) {
                        return 0;
                    }
                    if (a.getCreatedTime() == null) {
                        return 1;
                    }
                    if (b.getCreatedTime() == null) {
                        return -1;
                    }
                    return b.getCreatedTime().compareTo(a.getCreatedTime());
                })
                .limit(10)
                .toList();
    }

    @Override
    public UserProgressSummaryVO progressSummary(Long userId) {
        BigDecimal listeningAvg = averageListeningScore(userId);
        BigDecimal readingAvg = averageReadingScore(userId);
        BigDecimal writingAvg = averageWritingScore(userId);
        BigDecimal speakingAvg = averageSpeakingScore(userId);

        UserProgressSummaryVO vo = new UserProgressSummaryVO();
        vo.setListeningAverageScore(defaultDecimal(listeningAvg));
        vo.setReadingAverageScore(defaultDecimal(readingAvg));
        vo.setWritingAverageScore(defaultDecimal(writingAvg));
        vo.setSpeakingAverageScore(defaultDecimal(speakingAvg));
        vo.setOverallAverageScore(
                buildAverageScore(listeningAvg, readingAvg, writingAvg, speakingAvg)
        );
        vo.setGeneratedAt(LocalDateTime.now());
        return vo;
    }

    @Override
    public UserDashboardOverviewVisualVO userOverviewVisual(Long userId, String timeRange) {
        DashboardAskPreloadedPayload payload = loadUserOverviewPayload(userId, timeRange);

        return UserDashboardOverviewVisualVO.builder()
                .snapshotId(payload.getSnapshotId())
                .snapshotTime(payload.getSnapshotTime())
                .overview(payload.getOverview())
                .progressSummary(payload.getProgressSummary())
                .recentRecords(payload.getRecentRecords())
                .moduleStats(payload.getModuleStats())
                .aggregates(payload.getAggregates())
                .scoreRadarChart(buildUserScoreRadarChart(payload))
                .scoreTrendChart(buildUserScoreTrendChart(payload))
                .build();
    }

    @Override
    public UserExecutiveSummaryVO userExecutiveSummary(Long userId, String timeRange) {
        DashboardAskPreloadedPayload payload = loadUserOverviewPayload(userId, timeRange);

        String query = DashboardExecutiveSummaryQueryConstants.USER_EXECUTIVE_SUMMARY_DEFAULT_QUERY;
        String summaryText = composeExecutiveSummary(
                DashboardOverviewConstants.ROLE_USER,
                userId,
                userId,
                query,
                "user_executive_summary",
                timeRange,
                payload
        );

        if (!hasText(summaryText)) {
            summaryText = buildUserExecutiveSummaryText(payload, timeRange);
        }

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("summary_source", "preloaded_payload_compose");
        meta.put("time_range", timeRange);
        meta.put("has_progress_summary", payload.getProgressSummary() != null);
        meta.put("module_stat_count", payload.getModuleStats() == null ? 0 : payload.getModuleStats().size());
        meta.put("recent_record_count", payload.getRecentRecords() == null ? 0 : payload.getRecentRecords().size());

        return UserExecutiveSummaryVO.builder()
                .snapshotId(payload.getSnapshotId())
                .snapshotTime(payload.getSnapshotTime())
                .summaryType(DashboardOverviewConstants.SUMMARY_TYPE_AI)
                .summaryText(summaryText)
                .summarySentences(splitSummarySentences(summaryText))
                .queryUsed(query)
                .meta(meta)
                .build();
    }

    private DashboardAskPreloadedPayload loadUserOverviewPayload(Long userId, String timeRange) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put(DashboardOverviewConstants.CONTEXT_KEY_TIME_RANGE, timeRange);

        return dashboardPreloadServiceProvider.getObject().preload(
                DashboardOverviewConstants.ROLE_USER,
                userId,
                userId,
                DashboardOverviewConstants.PAGE_NAME_USER_OVERVIEW,
                null,
                context
        );
    }

    private String composeExecutiveSummary(
            String role,
            Long operatorUserId,
            Long targetUserId,
            String query,
            String pageName,
            String timeRange,
            DashboardAskPreloadedPayload payload) {

        Map<String, Object> data = new LinkedHashMap<>();
        putIfPresent(data, "query", query);
        putIfPresent(data, "askScene", DashboardOverviewConstants.ASK_SCENE_CHAT);
        putIfPresent(data, "responseMode", DashboardOverviewConstants.RESPONSE_MODE_DEFAULT);
        putIfPresent(data, "preloadedPayload", payload);

        if (payload != null) {
            putIfPresent(data, "overview", payload.getOverview());
            putIfPresent(data, "progressSummary", payload.getProgressSummary());
            putIfPresent(data, "recentRecords", payload.getRecentRecords());
            putIfPresent(data, "moduleStats", payload.getModuleStats());
            putIfPresent(data, "recentQuestions", payload.getRecentQuestions());
            putIfPresent(data, "recentPassages", payload.getRecentPassages());
            putIfPresent(data, "aggregates", payload.getAggregates());
        }

        Map<String, Object> filters = new LinkedHashMap<>();
        filters.put("pageName", pageName);
        filters.put("summaryType", "executive_summary");
        filters.put("tone", "warm_teacher");
        filters.put("timeRange", normalizeTimeRange(timeRange));

        DashboardAnswerComposeResult result = dashboardAnswerComposeService.compose(
                DashboardAnswerComposeRequest.builder()
                        .role(role)
                        .operatorUserId(operatorUserId)
                        .targetUserId(targetUserId)
                        .originalQuery(query)
                        .capability("PRELOADED_DIRECT")
                        .filters(filters)
                        .data(data)
                        .responseLanguage(DashboardOverviewConstants.RESPONSE_LANGUAGE_ZH_HANT)
                        .build()
        );

        return result == null || result.getAnswer() == null ? null : result.getAnswer().trim();
    }

    private void putIfPresent(Map<String, Object> target, String key, Object value) {
        if (target == null || key == null || key.isBlank() || value == null) {
            return;
        }
        if (value instanceof String text) {
            if (!text.isBlank()) {
                target.put(key, text.trim());
            }
            return;
        }
        if (value instanceof List<?> list && list.isEmpty()) {
            return;
        }
        if (value instanceof Map<?, ?> map && map.isEmpty()) {
            return;
        }
        target.put(key, value);
    }

    private DashboardAskRequest buildUserExecutiveSummaryRequest(Long userId,
                                                                 String timeRange,
                                                                 DashboardAskPreloadedPayload payload) {
        DashboardAskRequest request = new DashboardAskRequest();
        request.setTargetUserId(userId);
        request.setQuery(DashboardExecutiveSummaryQueryConstants.USER_EXECUTIVE_SUMMARY_DEFAULT_QUERY);
        request.setAskScene(DashboardOverviewConstants.ASK_SCENE_CHAT);
        request.setResponseMode(DashboardOverviewConstants.RESPONSE_MODE_DEFAULT);
        request.setPreloadedPayload(payload);

        Map<String, Object> context = new LinkedHashMap<>();
        context.put(DashboardOverviewConstants.CONTEXT_KEY_TIME_RANGE, timeRange);
        request.setContext(context);

        DashboardAskClientContext clientContext = new DashboardAskClientContext();
        clientContext.setPageName(DashboardOverviewConstants.PAGE_NAME_USER_OVERVIEW);
        clientContext.setRoute("/smartielts/dashboard/user/overview_visual");
        clientContext.setTab("overview");
        clientContext.setLocale(DashboardOverviewConstants.RESPONSE_LANGUAGE_ZH_HANT);
        request.setClientContext(clientContext);

        return request;
    }

    private Map<String, Object> buildUserScoreRadarChart(DashboardAskPreloadedPayload payload) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("chart_type", "radar");

        if (payload.getProgressSummary() instanceof UserProgressSummaryVO vo) {
            map.put("indicators", java.util.List.of("listening", "reading", "writing", "speaking"));
            map.put("values", java.util.List.of(
                    vo.getListeningAverageScore(),
                    vo.getReadingAverageScore(),
                    vo.getWritingAverageScore(),
                    vo.getSpeakingAverageScore()
            ));
        } else {
            map.put("indicators", java.util.List.of());
            map.put("values", java.util.List.of());
        }
        return map;
    }

    private Map<String, Object> buildUserScoreTrendChart(DashboardAskPreloadedPayload payload) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("chart_type", "line");
        map.put("x_key", "createdTime");
        map.put("y_key", "score");
        map.put("rows", payload.getRecentRecords() == null ? List.of() : payload.getRecentRecords());
        return map;
    }

    private Map<String, Object> buildSummaryMeta(DashboardAssistantResponse response) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("summarySource", DashboardOverviewConstants.SUMMARY_SOURCE_PRELOAD_PLUS_ASK);
        meta.put("suggestions", response == null ? List.of() : response.getSuggestions());
        meta.put("answerMeta", response == null ? Map.of() : response.getMeta());
        return meta;
    }

    private BigDecimal averageListeningScore(Long userId) {
        return listeningRecordMapper.selectUserAverageScore(userId);
    }

    private BigDecimal averageReadingScore(Long userId) {
        return readingRecordMapper.selectUserAverageScore(userId);
    }

    private BigDecimal averageWritingScore(Long userId) {
        return writingRecordMapper.selectUserAverageScore(userId);
    }

    private BigDecimal averageSpeakingScore(Long userId) {
        return speakingRecordMapper.selectUserAverageScore(userId);
    }

    private BigDecimal buildAverageScore(BigDecimal listening,
                                         BigDecimal reading,
                                         BigDecimal writing,
                                         BigDecimal speaking) {
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;

        if (listening != null) {
            sum = sum.add(listening);
            count++;
        }
        if (reading != null) {
            sum = sum.add(reading);
            count++;
        }
        if (writing != null) {
            sum = sum.add(writing);
            count++;
        }
        if (speaking != null) {
            sum = sum.add(speaking);
            count++;
        }

        if (count == 0) {
            return BigDecimal.ZERO;
        }

        return sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private UserModuleStatVO moduleStat(String module, long active, long deleted) {
        UserModuleStatVO vo = new UserModuleStatVO();
        vo.setModule(module);
        vo.setActiveCount(active);
        vo.setDeletedCount(deleted);
        return vo;
    }

    private List<UserRecentRecordVO> toRecentRecordVOList(String module, List<?> records) {
        List<UserRecentRecordVO> result = new ArrayList<>();
        if (records == null) {
            return result;
        }

        for (Object record : records) {
            UserRecentRecordVO vo = new UserRecentRecordVO();
            vo.setModule(module);

            if (record instanceof ListeningRecord r) {
                vo.setRecordId(r.getId());
                vo.setCreatedTime(r.getCreatedTime());
            } else if (record instanceof ReadingRecord r) {
                vo.setRecordId(r.getId());
                vo.setCreatedTime(r.getCreatedTime());
            } else if (record instanceof WritingRecord r) {
                vo.setRecordId(r.getId());
                vo.setCreatedTime(r.getCreatedTime());
            } else if (record instanceof SpeakingRecord r) {
                vo.setRecordId(r.getId());
                vo.setCreatedTime(r.getCreatedTime());
            }

            result.add(vo);
        }

        return result;
    }

    private List<UserRecentRecordVO> toListeningRecentRecords(
            List<com.andrew.smartielts.listening.domain.pojo.ListeningRecord> records) {

        List<UserRecentRecordVO> list = new ArrayList<>();
        if (records == null) {
            return list;
        }

        for (com.andrew.smartielts.listening.domain.pojo.ListeningRecord record : records) {
            UserRecentRecordVO vo = new UserRecentRecordVO();
            vo.setModule("listening");
            vo.setRecordId(record.getId());
            vo.setCreatedTime(record.getCreatedTime());
            vo.setTitle("Listening Test #" + record.getTestId());
            vo.setStatus("ACTIVE");
            vo.setSummary(record.getTotalScore() == null ? "No score" : "Score: " + record.getTotalScore());
            list.add(vo);
        }
        return list;
    }

    private List<UserRecentRecordVO> toReadingRecentRecords(
            List<com.andrew.smartielts.reading.domain.pojo.ReadingRecord> records) {

        List<UserRecentRecordVO> list = new ArrayList<>();
        if (records == null) {
            return list;
        }

        for (com.andrew.smartielts.reading.domain.pojo.ReadingRecord record : records) {
            UserRecentRecordVO vo = new UserRecentRecordVO();
            vo.setModule("reading");
            vo.setRecordId(record.getId());
            vo.setCreatedTime(record.getCreatedTime());
            vo.setTitle("Reading Test #" + record.getTestId());
            vo.setStatus("ACTIVE");
            vo.setSummary(record.getTotalScore() == null ? "No score" : "Score: " + record.getTotalScore());
            list.add(vo);
        }
        return list;
    }

    private List<UserRecentRecordVO> toWritingRecentRecords(
            List<com.andrew.smartielts.writing.domain.pojo.WritingRecord> records) {

        List<UserRecentRecordVO> list = new ArrayList<>();
        if (records == null) {
            return list;
        }

        for (com.andrew.smartielts.writing.domain.pojo.WritingRecord record : records) {
            UserRecentRecordVO vo = new UserRecentRecordVO();
            vo.setModule("writing");
            vo.setRecordId(record.getId());
            vo.setCreatedTime(record.getCreatedTime());
            vo.setTitle("Writing Question #" + record.getQuestionId());
            vo.setStatus(record.getAiStatus());
            vo.setSummary(record.getAiScore() == null ? "No AI score" : "AI score: " + record.getAiScore());
            list.add(vo);
        }
        return list;
    }

    private List<UserRecentRecordVO> toSpeakingRecentRecords(
            List<com.andrew.smartielts.speaking.domain.pojo.SpeakingRecord> records) {

        List<UserRecentRecordVO> list = new ArrayList<>();
        if (records == null) {
            return list;
        }

        for (com.andrew.smartielts.speaking.domain.pojo.SpeakingRecord record : records) {
            UserRecentRecordVO vo = new UserRecentRecordVO();
            vo.setModule("speaking");
            vo.setRecordId(record.getId());
            vo.setCreatedTime(record.getCreatedTime());
            vo.setTitle("Speaking Question #" + record.getQuestionId());
            vo.setStatus(record.getAiStatus());
            vo.setSummary(record.getOverallScore() == null ? "No overall score" : "Overall score: " + record.getOverallScore());
            list.add(vo);
        }
        return list;
    }

    private String buildUserExecutiveSummaryText(DashboardAskPreloadedPayload payload, String timeRange) {
        Map<String, Object> progress = toMap(payload.getProgressSummary());

        String overallAvg = firstNonBlank(
                getString(progress, "overallAverageScore"),
                getString(progress, "overallAverage"),
                getString(progress, "averageScore"),
                getString(progress, "avgScore"),
                getString(progress, "overall_score")
        );

        WeakModule weak = findWeakestScoreModule(progress);

        List<String> parts = new ArrayList<>();
        String rangeLabel = toZhTimeRange(timeRange);

        if (hasText(overallAvg)) {
            parts.add(rangeLabel + " 的整體平均分是 " + overallAvg + "。");
        } else {
            parts.add(rangeLabel + " 目前可用分數資料不足，建議先完成一次四科練習建立基準。");
        }

        if (weak != null && hasText(weak.moduleName)) {
            parts.add("目前較需要關注的是 " + weak.moduleName + "，平均分約 " + weak.score + "。");
            parts.add("建議下一輪練習先安排 " + weak.moduleName + "，再回到 dashboard 比較趨勢。");
        } else {
            parts.add("目前各科分數還不足以判斷弱項。");
            parts.add("建議前端顯示最近紀錄與四科雷達圖，引導使用者補齊缺少的練習資料。");
        }

        return String.join("", parts);
    }

    private WeakModule findWeakestScoreModule(Map<String, Object> progress) {
        List<WeakModule> modules = List.of(
                new WeakModule("listening", toDouble(progress.get("listeningAverageScore"))),
                new WeakModule("reading", toDouble(progress.get("readingAverageScore"))),
                new WeakModule("writing", toDouble(progress.get("writingAverageScore"))),
                new WeakModule("speaking", toDouble(progress.get("speakingAverageScore")))
        );

        WeakModule result = null;
        for (WeakModule module : modules) {
            if (module.score == null || module.score <= 0) {
                continue;
            }
            if (result == null || module.score < result.score) {
                result = module;
            }
        }
        return result;
    }

    private Map<String, Object> toMap(Object value) {
        if (value == null) {
            return Map.of();
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            map.forEach((k, v) -> result.put(String.valueOf(k), v));
            return result;
        }
        return objectMapper.convertValue(value, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
    }

    private List<Map<String, Object>> toListOfMap(Object value) {
        if (value == null) {
            return List.of();
        }
        if (value instanceof List<?> list) {
            List<Map<String, Object>> result = new ArrayList<>();
            for (Object item : list) {
                result.add(toMap(item));
            }
            return result;
        }
        return List.of();
    }

    private String getString(Map<String, Object> map, String key) {
        if (map == null || map.isEmpty() || key == null) {
            return null;
        }
        Object value = map.get(key);
        return value == null ? null : String.valueOf(value).trim();
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private Double firstNumber(Object... values) {
        if (values == null) {
            return null;
        }
        for (Object value : values) {
            Double parsed = toDouble(value);
            if (parsed != null) {
                return parsed;
            }
        }
        return null;
    }

    private Double toDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value).trim());
        } catch (Exception e) {
            return null;
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalizeTimeRange(String timeRange) {
        return hasText(timeRange) ? timeRange.trim() : DashboardOverviewConstants.DEFAULT_TIME_RANGE;
    }

    private String toZhTimeRange(String timeRange) {
        return switch (normalizeTimeRange(timeRange).toLowerCase(java.util.Locale.ROOT)) {
            case "last7days" -> "近 7 天";
            case "last90days" -> "近 90 天";
            case "all" -> "全部時間";
            default -> "近 30 天";
        };
    }

    private List<String> splitSummarySentences(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return Arrays.stream(text.split("[。！？!?\\n]+"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    private static class WeakModule {
        private final String moduleName;
        private final Double score;

        private WeakModule(String moduleName, Double score) {
            this.moduleName = moduleName;
            this.score = score;
        }
    }
}

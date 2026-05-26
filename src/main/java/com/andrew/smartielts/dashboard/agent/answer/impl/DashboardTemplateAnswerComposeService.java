package com.andrew.smartielts.dashboard.agent.answer.impl;

import com.andrew.smartielts.dashboard.agent.answer.DashboardAnswerComposeService;
import com.andrew.smartielts.dashboard.agent.answer.DashboardSuggestionService;
import com.andrew.smartielts.dashboard.agent.answer.dto.DashboardAnswerComposeRequest;
import com.andrew.smartielts.dashboard.agent.answer.dto.DashboardAnswerComposeResult;
import com.andrew.smartielts.dashboard.domain.vo.AdminAiFailureVO;
import com.andrew.smartielts.dashboard.domain.vo.AdminModuleStatVO;
import com.andrew.smartielts.dashboard.domain.vo.AdminOverviewVO;
import com.andrew.smartielts.dashboard.domain.vo.AdminRecentIssueVO;
import com.andrew.smartielts.dashboard.domain.vo.AdminUserCountVO;
import com.andrew.smartielts.dashboard.domain.vo.AdminUserRecordSummaryVO;
import com.andrew.smartielts.dashboard.domain.vo.UserModuleStatVO;
import com.andrew.smartielts.dashboard.domain.vo.UserOverviewVO;
import com.andrew.smartielts.dashboard.domain.vo.UserProgressSummaryVO;
import com.andrew.smartielts.dashboard.domain.vo.UserRecentRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DashboardTemplateAnswerComposeService implements DashboardAnswerComposeService {

    private final DashboardSuggestionService dashboardSuggestionService;

    @Override
    public DashboardAnswerComposeResult compose(DashboardAnswerComposeRequest request) {
        Object data = request.getData();

        if (data instanceof UserOverviewVO vo) {
            return buildUserOverview(request, vo);
        }

        if (data instanceof UserProgressSummaryVO vo) {
            return buildUserProgressSummary(request, vo);
        }

        if (data instanceof AdminOverviewVO vo) {
            return buildAdminOverview(request, vo);
        }

        if (data instanceof AdminUserCountVO vo) {
            return buildAdminUserCount(request, vo);
        }

        if (data instanceof AdminUserRecordSummaryVO vo) {
            return buildAdminUserRecordSummary(request, vo);
        }

        if (data instanceof List<?> list) {
            return buildListAnswer(request, list);
        }

        if (data instanceof Map<?, ?> map) {
            return buildDashboardPayloadSummary(request, map);
        }

        String answer = "我已完成查詢，下面是相關資料結果。";
        return DashboardAnswerComposeResult.builder()
                .answer(answer)
                .suggestions(smartSuggestions(request, answer, data))
                .build();
    }

    private DashboardAnswerComposeResult buildUserOverview(DashboardAnswerComposeRequest request, UserOverviewVO vo) {
        String answer = "你目前共有 " + vo.getTotalActiveRecords()
                + " 筆有效練習紀錄，已刪除紀錄 " + vo.getTotalDeletedRecords()
                + " 筆。四科之中，活躍紀錄可再配合下方資料查看各科分佈。";

        return DashboardAnswerComposeResult.builder()
                .answer(answer)
                .suggestions(smartSuggestions(request, answer, vo))
                .build();
    }

    private DashboardAnswerComposeResult buildUserProgressSummary(DashboardAnswerComposeRequest request,
                                                                  UserProgressSummaryVO vo) {
        String bestModule = maxScoreModule(
                vo.getListeningAverageScore(),
                vo.getReadingAverageScore(),
                vo.getWritingAverageScore(),
                vo.getSpeakingAverageScore()
        );
        String weakestModule = minScoreModule(
                vo.getListeningAverageScore(),
                vo.getReadingAverageScore(),
                vo.getWritingAverageScore(),
                vo.getSpeakingAverageScore()
        );

        StringBuilder answer = new StringBuilder();
        answer.append("你目前整體平均分為 ").append(formatDecimal(vo.getOverallAverageScore()))
                .append("。")
                .append("各科平均分為：聆聽 ").append(formatDecimal(vo.getListeningAverageScore()))
                .append("、閱讀 ").append(formatDecimal(vo.getReadingAverageScore()))
                .append("、寫作 ").append(formatDecimal(vo.getWritingAverageScore()))
                .append("、口說 ").append(formatDecimal(vo.getSpeakingAverageScore()))
                .append("。");

        if (bestModule != null && weakestModule != null) {
            answer.append("目前表現相對較好的是").append(bestModule)
                    .append("，相對較弱的是").append(weakestModule).append("。");
        }

        String query = request.getOriginalQuery() == null ? "" : request.getOriginalQuery();
        if (containsComparisonIntent(query)) {
            answer.append("不過目前這份資料是整體平均摘要，未包含今週與上週的分段比較，因此暫時無法準確判斷是否較上週進步。");
        }

        return DashboardAnswerComposeResult.builder()
                .answer(answer.toString())
                .suggestions(smartSuggestions(request, answer.toString(), vo))
                .build();
    }

    private DashboardAnswerComposeResult buildAdminOverview(DashboardAnswerComposeRequest request, AdminOverviewVO vo) {
        String answer = "目前平台共有 " + vo.getTotalUsers()
                + " 位用戶，其中活躍用戶 " + vo.getActiveUsers()
                + " 位、已刪除用戶 " + vo.getDeletedUsers()
                + " 位。全平台共有 " + vo.getTotalActiveRecords()
                + " 筆有效紀錄、" + vo.getTotalDeletedRecords()
                + " 筆已刪除紀錄，近期 AI 相關問題數為 " + vo.getRecentAiFailureCount() + "。";

        return DashboardAnswerComposeResult.builder()
                .answer(answer)
                .suggestions(smartSuggestions(request, answer, vo))
                .build();
    }

    private DashboardAnswerComposeResult buildAdminUserCount(DashboardAnswerComposeRequest request, AdminUserCountVO vo) {
        String answer = "目前平台總用戶數為 " + vo.getTotalUsers()
                + "，其中活躍用戶 " + vo.getActiveUsers()
                + "，已刪除用戶 " + vo.getDeletedUsers() + "。";

        return DashboardAnswerComposeResult.builder()
                .answer(answer)
                .suggestions(smartSuggestions(request, answer, vo))
                .build();
    }

    private DashboardAnswerComposeResult buildAdminUserRecordSummary(DashboardAnswerComposeRequest request,
                                                                     AdminUserRecordSummaryVO vo) {
        String answer = "該用戶目前共有 " + vo.getTotalActiveRecords()
                + " 筆有效紀錄，已刪除紀錄 " + vo.getTotalDeletedRecords()
                + " 筆，整體平均分為 " + formatDecimal(vo.getAverageScore()) + "。"
                + "四科平均分為：聆聽 " + formatDecimal(vo.getListeningAverageScore())
                + "、閱讀 " + formatDecimal(vo.getReadingAverageScore())
                + "、寫作 " + formatDecimal(vo.getWritingAverageScore())
                + "、口說 " + formatDecimal(vo.getSpeakingAverageScore()) + "。";

        return DashboardAnswerComposeResult.builder()
                .answer(answer)
                .suggestions(smartSuggestions(request, answer, vo))
                .build();
    }

    private DashboardAnswerComposeResult buildListAnswer(DashboardAnswerComposeRequest request, List<?> list) {
        if (list.isEmpty()) {
            String answer = "目前沒有查到相關資料。";
            return DashboardAnswerComposeResult.builder()
                    .answer(answer)
                    .suggestions(smartSuggestions(request, answer, list))
                    .build();
        }

        Object first = list.get(0);

        if (first instanceof UserRecentRecordVO) {
            return buildUserRecentRecords(request, castList(list, UserRecentRecordVO.class));
        }

        if (first instanceof UserModuleStatVO) {
            return buildUserModuleStats(castList(list, UserModuleStatVO.class), request);
        }

        if (first instanceof AdminModuleStatVO) {
            return buildAdminModuleStats(request, castList(list, AdminModuleStatVO.class));
        }

        if (first instanceof AdminAiFailureVO) {
            return buildAdminAiFailures(request, castList(list, AdminAiFailureVO.class));
        }

        if (first instanceof AdminRecentIssueVO) {
            return buildAdminRecentIssues(request, castList(list, AdminRecentIssueVO.class));
        }

        String answer = "我已整理出一組列表資料，請查看下方明細。";
        return DashboardAnswerComposeResult.builder()
                .answer(answer)
                .suggestions(smartSuggestions(request, answer, list))
                .build();
    }

    private DashboardAnswerComposeResult buildUserRecentRecords(DashboardAnswerComposeRequest request,
                                                                List<UserRecentRecordVO> list) {
        UserRecentRecordVO latest = list.stream()
                .filter(Objects::nonNull)
                .filter(it -> it.getCreatedTime() != null)
                .max(Comparator.comparing(UserRecentRecordVO::getCreatedTime))
                .orElse(list.get(0));

        String answer = "我已整理你最近的練習紀錄，共 " + list.size()
                + " 筆，並按時間由新到舊展示。"
                + "最近一筆是 " + safeText(latest.getModule())
                + " 模組，題目為「" + safeText(latest.getTitle()) + "」。";

        return DashboardAnswerComposeResult.builder()
                .answer(answer)
                .suggestions(smartSuggestions(request, answer, list))
                .build();
    }

    private DashboardAnswerComposeResult buildUserModuleStats(List<UserModuleStatVO> list,
                                                              DashboardAnswerComposeRequest request) {
        long totalActive = list.stream().mapToLong(UserModuleStatVO::getActiveCount).sum();
        long totalDeleted = list.stream().mapToLong(UserModuleStatVO::getDeletedCount).sum();

        boolean deletedOnlyQuery =
                (request.getOriginalQuery() != null
                        && request.getOriginalQuery().toLowerCase(Locale.ROOT).contains("刪"))
                        || "USER_SELF_DELETED_SUMMARY".equalsIgnoreCase(request.getCapability());

        String answer;
        if (deletedOnlyQuery) {
            answer = "我已整理你各科已刪除紀錄，共 " + totalDeleted + " 筆。"
                    + "你可以在下方查看聆聽、閱讀、寫作與口說的分佈。";
        } else {
            answer = "我已整理你各科紀錄統計，目前共有 " + totalActive
                    + " 筆有效紀錄，已刪除紀錄 " + totalDeleted + " 筆。";
        }

        return DashboardAnswerComposeResult.builder()
                .answer(answer)
                .suggestions(smartSuggestions(request, answer, list))
                .build();
    }

    private DashboardAnswerComposeResult buildAdminModuleStats(DashboardAnswerComposeRequest request,
                                                               List<AdminModuleStatVO> list) {
        long totalActive = list.stream().mapToLong(AdminModuleStatVO::getActiveCount).sum();
        long totalDeleted = list.stream().mapToLong(AdminModuleStatVO::getDeletedCount).sum();

        String answer = "我已整理平台各模組統計，目前共有 " + totalActive
                + " 筆有效紀錄，已刪除紀錄 " + totalDeleted
                + " 筆。你可以在下方查看四個模組的分佈。";

        return DashboardAnswerComposeResult.builder()
                .answer(answer)
                .suggestions(smartSuggestions(request, answer, list))
                .build();
    }

    private DashboardAnswerComposeResult buildAdminAiFailures(DashboardAnswerComposeRequest request,
                                                              List<AdminAiFailureVO> list) {
        long total = list.stream().mapToLong(AdminAiFailureVO::getFailureCount).sum();

        String answer = "目前平台 AI 失敗記錄共 " + total
                + " 筆，主要分佈可在下方查看各模組統計。";

        return DashboardAnswerComposeResult.builder()
                .answer(answer)
                .suggestions(smartSuggestions(request, answer, list))
                .build();
    }

    private DashboardAnswerComposeResult buildAdminRecentIssues(DashboardAnswerComposeRequest request,
                                                                List<AdminRecentIssueVO> list) {
        String answer = "我已整理近期平台問題，共 " + list.size()
                + " 類異常或風險項目，請查看下方明細以判斷是否需要優先處理。";

        return DashboardAnswerComposeResult.builder()
                .answer(answer)
                .suggestions(smartSuggestions(request, answer, list))
                .build();
    }

    private DashboardAnswerComposeResult buildDashboardPayloadSummary(DashboardAnswerComposeRequest request,
                                                                      Map<?, ?> data) {
        Object overview = data.get("overview");
        Object progressSummary = data.get("progressSummary");
        Object moduleStats = data.get("moduleStats");
        Object recentRecords = data.get("recentRecords");

        String answer = isEnglishResponse(request)
                ? buildEnglishPayloadSummary(overview, progressSummary, moduleStats, recentRecords)
                : buildChinesePayloadSummary(overview, progressSummary, moduleStats, recentRecords);

        return DashboardAnswerComposeResult.builder()
                .answer(answer)
                .suggestions(smartSuggestions(request, answer, data))
                .build();
    }

    private String buildEnglishPayloadSummary(Object overview,
                                              Object progressSummary,
                                              Object moduleStats,
                                              Object recentRecords) {
        long activeRecords = longValue(overview, "totalActiveRecords");
        long deletedRecords = longValue(overview, "totalDeletedRecords");
        BigDecimal overallAverage = decimalValue(progressSummary, "overallAverageScore");
        int moduleCount = listSize(moduleStats);
        int recentCount = listSize(recentRecords);

        StringBuilder answer = new StringBuilder();
        answer.append("Your dashboard shows ")
                .append(activeRecords)
                .append(" active practice records");
        if (deletedRecords > 0) {
            answer.append(" and ").append(deletedRecords).append(" recoverable records");
        }
        answer.append(". The current overall average is ")
                .append(formatDecimal(overallAverage))
                .append(" across ")
                .append(moduleCount == 0 ? "the IELTS modules" : moduleCount + " tracked modules")
                .append(".");
        if (recentCount > 0) {
            answer.append(" I found ").append(recentCount)
                    .append(" recent records to use for next-step analysis.");
        } else {
            answer.append(" There are no recent scored records in this snapshot yet.");
        }
        return answer.toString();
    }

    private String buildChinesePayloadSummary(Object overview,
                                              Object progressSummary,
                                              Object moduleStats,
                                              Object recentRecords) {
        long activeRecords = longValue(overview, "totalActiveRecords");
        long deletedRecords = longValue(overview, "totalDeletedRecords");
        BigDecimal overallAverage = decimalValue(progressSummary, "overallAverageScore");
        int moduleCount = listSize(moduleStats);
        int recentCount = listSize(recentRecords);

        StringBuilder answer = new StringBuilder();
        answer.append("你的 dashboard 目前有 ")
                .append(activeRecords)
                .append(" 筆有效練習紀錄");
        if (deletedRecords > 0) {
            answer.append("，另有 ").append(deletedRecords).append(" 筆可復原紀錄");
        }
        answer.append("。目前整體平均分是 ")
                .append(formatDecimal(overallAverage))
                .append("，統計範圍涵蓋 ")
                .append(moduleCount == 0 ? "IELTS 各模組" : moduleCount + " 個模組")
                .append("。");
        if (recentCount > 0) {
            answer.append("我也找到 ").append(recentCount).append(" 筆近期紀錄，可用來判斷下一步練習重點。");
        } else {
            answer.append("這份快照暫時沒有近期已評分紀錄。");
        }
        return answer.toString();
    }

    private boolean isEnglishResponse(DashboardAnswerComposeRequest request) {
        return request != null
                && request.getResponseLanguage() != null
                && request.getResponseLanguage().trim().toLowerCase(Locale.ROOT).startsWith("en");
    }

    private int listSize(Object value) {
        return value instanceof List<?> list ? list.size() : 0;
    }

    private long longValue(Object source, String key) {
        Object value = propertyValue(source, key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Long.parseLong(text.trim());
            } catch (NumberFormatException ignored) {
                return 0L;
            }
        }
        return 0L;
    }

    private BigDecimal decimalValue(Object source, String key) {
        Object value = propertyValue(source, key);
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return new BigDecimal(text.trim());
            } catch (NumberFormatException ignored) {
                return BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }

    private Object propertyValue(Object source, String key) {
        if (source instanceof Map<?, ?> map) {
            return map.get(key);
        }
        if (source instanceof UserOverviewVO vo) {
            return switch (key) {
                case "totalActiveRecords" -> vo.getTotalActiveRecords();
                case "totalDeletedRecords" -> vo.getTotalDeletedRecords();
                default -> null;
            };
        }
        if (source instanceof AdminOverviewVO vo) {
            return switch (key) {
                case "totalActiveRecords" -> vo.getTotalActiveRecords();
                case "totalDeletedRecords" -> vo.getTotalDeletedRecords();
                default -> null;
            };
        }
        if (source instanceof UserProgressSummaryVO vo) {
            return "overallAverageScore".equals(key) ? vo.getOverallAverageScore() : null;
        }
        return null;
    }

    private List<String> smartSuggestions(DashboardAnswerComposeRequest request, String answer, Object data) {
        return dashboardSuggestionService.buildSuggestions(
                request.getRole(),
                request.getOriginalQuery(),
                answer,
                request.getCapability(),
                null,
                request.getFilters(),
                data
        );
    }

    private boolean containsComparisonIntent(String query) {
        String text = query == null ? "" : query.toLowerCase(Locale.ROOT);
        return text.contains("對比")
                || text.contains("比較")
                || text.contains("相比")
                || text.contains("進步")
                || text.contains("上週")
                || text.contains("上个星期")
                || text.contains("上個星期")
                || text.contains("this week")
                || text.contains("last week");
    }

    private String maxScoreModule(BigDecimal listening, BigDecimal reading, BigDecimal writing, BigDecimal speaking) {
        return List.of(
                        scoreEntry("聆聽", listening),
                        scoreEntry("閱讀", reading),
                        scoreEntry("寫作", writing),
                        scoreEntry("口說", speaking)
                ).stream()
                .max(Comparator.comparing(ScoreEntry::score))
                .map(ScoreEntry::label)
                .orElse(null);
    }

    private String minScoreModule(BigDecimal listening, BigDecimal reading, BigDecimal writing, BigDecimal speaking) {
        return List.of(
                        scoreEntry("聆聽", listening),
                        scoreEntry("閱讀", reading),
                        scoreEntry("寫作", writing),
                        scoreEntry("口說", speaking)
                ).stream()
                .min(Comparator.comparing(ScoreEntry::score))
                .map(ScoreEntry::label)
                .orElse(null);
    }

    private ScoreEntry scoreEntry(String label, BigDecimal score) {
        return new ScoreEntry(label, score == null ? BigDecimal.ZERO : score);
    }

    private String formatDecimal(BigDecimal value) {
        return value == null ? "0" : value.stripTrailingZeros().toPlainString();
    }

    private String safeText(String text) {
        return text == null || text.isBlank() ? "未命名" : text;
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> castList(List<?> list, Class<T> type) {
        return (List<T>) list;
    }

    private record ScoreEntry(String label, BigDecimal score) {
    }
}

package com.andrew.smartielts.dashboard.service.impl;

import com.andrew.smartielts.dashboard.domain.vo.AdminAiFailureVO;
import com.andrew.smartielts.dashboard.domain.vo.AdminModuleStatVO;
import com.andrew.smartielts.dashboard.domain.vo.AdminOverviewVO;
import com.andrew.smartielts.dashboard.domain.vo.AdminRecentIssueVO;
import com.andrew.smartielts.dashboard.domain.vo.AdminUserCountVO;
import com.andrew.smartielts.dashboard.domain.vo.AdminUserRecordSummaryVO;
import com.andrew.smartielts.dashboard.service.AdminDashboardService;
import com.andrew.smartielts.listening.domain.query.admin.AdminListeningDeletedRecordPageQuery;
import com.andrew.smartielts.listening.domain.query.admin.AdminListeningRecordPageQuery;
import com.andrew.smartielts.listening.domain.query.user.UserListeningDeletedRecordPageQuery;
import com.andrew.smartielts.listening.domain.query.user.UserListeningRecordPageQuery;
import com.andrew.smartielts.listening.mapper.ListeningRecordMapper;
import com.andrew.smartielts.reading.domain.query.admin.AdminReadingDeletedRecordPageQuery;
import com.andrew.smartielts.reading.domain.query.admin.AdminReadingRecordPageQuery;
import com.andrew.smartielts.reading.domain.query.user.UserReadingDeletedRecordPageQuery;
import com.andrew.smartielts.reading.domain.query.user.UserReadingRecordPageQuery;
import com.andrew.smartielts.reading.mapper.ReadingRecordMapper;
import com.andrew.smartielts.speaking.domain.query.admin.AdminSpeakingDeletedRecordPageQuery;
import com.andrew.smartielts.speaking.domain.query.admin.AdminSpeakingRecordPageQuery;
import com.andrew.smartielts.speaking.domain.query.user.UserSpeakingDeletedRecordPageQuery;
import com.andrew.smartielts.speaking.domain.query.user.UserSpeakingRecordPageQuery;
import com.andrew.smartielts.speaking.mapper.SpeakingRecordMapper;
import com.andrew.smartielts.user.mapper.UserMapper;
import com.andrew.smartielts.writing.domain.query.admin.AdminWritingDeletedRecordPageQuery;
import com.andrew.smartielts.writing.domain.query.admin.AdminWritingRecordPageQuery;
import com.andrew.smartielts.writing.domain.query.user.UserWritingDeletedRecordPageQuery;
import com.andrew.smartielts.writing.domain.query.user.UserWritingRecordPageQuery;
import com.andrew.smartielts.writing.mapper.WritingRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserMapper userMapper;
    private final ListeningRecordMapper listeningRecordMapper;
    private final ReadingRecordMapper readingRecordMapper;
    private final WritingRecordMapper writingRecordMapper;
    private final SpeakingRecordMapper speakingRecordMapper;

    @Override
    public AdminOverviewVO overview() {
        long totalUsers = safeLong(userMapper.countAllUsers());
        long activeUsers = safeLong(userMapper.countActiveUsers());
        long deletedUsers = safeLong(userMapper.countDeletedUsers());

        long listeningActive = safeLong(
                listeningRecordMapper.countAdminActive(new AdminListeningRecordPageQuery())
        );
        long listeningDeleted = safeLong(
                listeningRecordMapper.countAdminDeleted(new AdminListeningDeletedRecordPageQuery())
        );

        long readingActive = safeLong(
                readingRecordMapper.countAdminActive(new AdminReadingRecordPageQuery())
        );
        long readingDeleted = safeLong(
                readingRecordMapper.countAdminDeleted(new AdminReadingDeletedRecordPageQuery())
        );

        long writingActive = safeLong(
                writingRecordMapper.countAdminActive(new AdminWritingRecordPageQuery())
        );
        long writingDeleted = safeLong(
                writingRecordMapper.countAdminDeleted(new AdminWritingDeletedRecordPageQuery())
        );

        long speakingActive = safeLong(
                speakingRecordMapper.countAdminActive(new AdminSpeakingRecordPageQuery())
        );
        long speakingDeleted = safeLong(
                speakingRecordMapper.countAdminDeleted(new AdminSpeakingDeletedRecordPageQuery())
        );

        List<AdminRecentIssueVO> recentIssues = recentIssues();
        long recentAiFailureCount = aiFailureSummary().stream()
                .mapToLong(AdminAiFailureVO::getFailureCount)
                .sum();

        AdminOverviewVO vo = new AdminOverviewVO();
        vo.setTotalUsers(totalUsers);
        vo.setActiveUsers(activeUsers);
        vo.setDeletedUsers(deletedUsers);

        vo.setListeningActiveRecords(listeningActive);
        vo.setListeningDeletedRecords(listeningDeleted);

        vo.setReadingActiveRecords(readingActive);
        vo.setReadingDeletedRecords(readingDeleted);

        vo.setWritingActiveRecords(writingActive);
        vo.setWritingDeletedRecords(writingDeleted);

        vo.setSpeakingActiveRecords(speakingActive);
        vo.setSpeakingDeletedRecords(speakingDeleted);

        vo.setTotalActiveRecords(
                listeningActive + readingActive + writingActive + speakingActive
        );
        vo.setTotalDeletedRecords(
                listeningDeleted + readingDeleted + writingDeleted + speakingDeleted
        );

        vo.setRecentAiFailureCount((int) recentAiFailureCount);
        vo.setRecentIssues(recentIssues == null ? List.of() : recentIssues);
        vo.setGeneratedAt(LocalDateTime.now());
        return vo;
    }

    @Override
    public AdminUserCountVO userCount() {
        AdminUserCountVO vo = new AdminUserCountVO();
        vo.setTotalUsers(safeLong(userMapper.countAllUsers()));
        vo.setActiveUsers(safeLong(userMapper.countActiveUsers()));
        vo.setDeletedUsers(safeLong(userMapper.countDeletedUsers()));
        vo.setGeneratedAt(LocalDateTime.now());
        return vo;
    }

    @Override
    public List<AdminModuleStatVO> moduleStats() {
        return List.of(
                moduleStat(
                        "listening",
                        safeLong(listeningRecordMapper.countAdminActive(new AdminListeningRecordPageQuery())),
                        safeLong(listeningRecordMapper.countAdminDeleted(new AdminListeningDeletedRecordPageQuery()))
                ),
                moduleStat(
                        "reading",
                        safeLong(readingRecordMapper.countAdminActive(new AdminReadingRecordPageQuery())),
                        safeLong(readingRecordMapper.countAdminDeleted(new AdminReadingDeletedRecordPageQuery()))
                ),
                moduleStat(
                        "writing",
                        safeLong(writingRecordMapper.countAdminActive(new AdminWritingRecordPageQuery())),
                        safeLong(writingRecordMapper.countAdminDeleted(new AdminWritingDeletedRecordPageQuery()))
                ),
                moduleStat(
                        "speaking",
                        safeLong(speakingRecordMapper.countAdminActive(new AdminSpeakingRecordPageQuery())),
                        safeLong(speakingRecordMapper.countAdminDeleted(new AdminSpeakingDeletedRecordPageQuery()))
                )
        );
    }

    @Override
    public List<AdminAiFailureVO> aiFailureSummary() {
        List<AdminAiFailureVO> list = new ArrayList<>();

        AdminAiFailureVO writing = new AdminAiFailureVO();
        writing.setModule("writing");
        writing.setFailureCount(safeLong(writingRecordMapper.countAdminAiFailed()));
        list.add(writing);

        AdminAiFailureVO speaking = new AdminAiFailureVO();
        speaking.setModule("speaking");
        speaking.setFailureCount(safeLong(speakingRecordMapper.countAdminAiFailed()));
        list.add(speaking);

        return list;
    }

    @Override
    public AdminUserRecordSummaryVO userRecordSummary(Long targetUserId) {
        long listeningActive = safeLong(
                listeningRecordMapper.countUserActive(targetUserId, new UserListeningRecordPageQuery())
        );
        long listeningDeleted = safeLong(
                listeningRecordMapper.countUserDeleted(targetUserId, new UserListeningDeletedRecordPageQuery())
        );

        long readingActive = safeLong(
                readingRecordMapper.countUserActive(targetUserId, new UserReadingRecordPageQuery())
        );
        long readingDeleted = safeLong(
                readingRecordMapper.countUserDeleted(targetUserId, new UserReadingDeletedRecordPageQuery())
        );

        long writingActive = safeLong(
                writingRecordMapper.countUserActive(targetUserId, new UserWritingRecordPageQuery())
        );
        long writingDeleted = safeLong(
                writingRecordMapper.countUserDeleted(targetUserId, new UserWritingDeletedRecordPageQuery())
        );

        long speakingActive = safeLong(
                speakingRecordMapper.countUserActive(targetUserId, new UserSpeakingRecordPageQuery())
        );
        long speakingDeleted = safeLong(
                speakingRecordMapper.countUserDeleted(targetUserId, new UserSpeakingDeletedRecordPageQuery())
        );

        BigDecimal listeningAvg = averageListeningScore(targetUserId);
        BigDecimal readingAvg = averageReadingScore(targetUserId);
        BigDecimal writingAvg = averageWritingScore(targetUserId);
        BigDecimal speakingAvg = averageSpeakingScore(targetUserId);

        AdminUserRecordSummaryVO vo = new AdminUserRecordSummaryVO();
        vo.setUserId(targetUserId);

        vo.setListeningActiveRecords(listeningActive);
        vo.setListeningDeletedRecords(listeningDeleted);

        vo.setReadingActiveRecords(readingActive);
        vo.setReadingDeletedRecords(readingDeleted);

        vo.setWritingActiveRecords(writingActive);
        vo.setWritingDeletedRecords(writingDeleted);

        vo.setSpeakingActiveRecords(speakingActive);
        vo.setSpeakingDeletedRecords(speakingDeleted);

        vo.setTotalActiveRecords(
                listeningActive + readingActive + writingActive + speakingActive
        );
        vo.setTotalDeletedRecords(
                listeningDeleted + readingDeleted + writingDeleted + speakingDeleted
        );

        vo.setListeningAverageScore(defaultDecimal(listeningAvg));
        vo.setReadingAverageScore(defaultDecimal(readingAvg));
        vo.setWritingAverageScore(defaultDecimal(writingAvg));
        vo.setSpeakingAverageScore(defaultDecimal(speakingAvg));
        vo.setAverageScore(buildAverageScore(
                listeningAvg, readingAvg, writingAvg, speakingAvg
        ));
        vo.setGeneratedAt(LocalDateTime.now());
        return vo;
    }

    @Override
    public List<AdminRecentIssueVO> recentIssues() {
        List<AdminRecentIssueVO> list = new ArrayList<>();

        long writingFailed = safeLong(writingRecordMapper.countAdminAiFailed());
        if (writingFailed > 0) {
            AdminRecentIssueVO writingIssue = new AdminRecentIssueVO();
            writingIssue.setModule("writing");
            writingIssue.setIssueType("AI_FAILURE_SUMMARY");
            writingIssue.setIssueCount(writingFailed);
            writingIssue.setGeneratedAt(LocalDateTime.now());
            list.add(writingIssue);
        }

        long speakingFailed = safeLong(speakingRecordMapper.countAdminAiFailed());
        if (speakingFailed > 0) {
            AdminRecentIssueVO speakingIssue = new AdminRecentIssueVO();
            speakingIssue.setModule("speaking");
            speakingIssue.setIssueType("AI_FAILURE_SUMMARY");
            speakingIssue.setIssueCount(speakingFailed);
            speakingIssue.setGeneratedAt(LocalDateTime.now());
            list.add(speakingIssue);
        }

        return list;
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

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private AdminModuleStatVO moduleStat(String module, long active, long deleted) {
        AdminModuleStatVO vo = new AdminModuleStatVO();
        vo.setModule(module);
        vo.setActiveCount(active);
        vo.setDeletedCount(deleted);
        return vo;
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
}
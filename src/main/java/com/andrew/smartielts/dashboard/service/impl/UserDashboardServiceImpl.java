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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
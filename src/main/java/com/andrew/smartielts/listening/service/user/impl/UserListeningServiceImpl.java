package com.andrew.smartielts.listening.service.user.impl;

import com.andrew.smartielts.common.constants.RecordQueryValidator;
import com.andrew.smartielts.common.domain.pojo.QuestionAnswerRule;
import com.andrew.smartielts.common.domain.pojo.TestPartGroup;
import com.andrew.smartielts.common.domain.pojo.TestTimerConfig;
import com.andrew.smartielts.common.page.PageResult;
import com.andrew.smartielts.listening.domain.dto.ListeningAnswerDTO;
import com.andrew.smartielts.listening.domain.dto.ListeningSessionActionDTO;
import com.andrew.smartielts.listening.domain.dto.ListeningSubmitDTO;
import com.andrew.smartielts.listening.domain.pojo.ListeningAnswerRecord;
import com.andrew.smartielts.listening.domain.pojo.ListeningMaterial;
import com.andrew.smartielts.listening.domain.pojo.ListeningQuestion;
import com.andrew.smartielts.listening.domain.pojo.ListeningRecord;
import com.andrew.smartielts.listening.domain.pojo.ListeningTest;
import com.andrew.smartielts.listening.domain.query.user.UserListeningDeletedRecordPageQuery;
import com.andrew.smartielts.listening.domain.query.user.UserListeningRecordPageQuery;
import com.andrew.smartielts.listening.domain.vo.ListeningAnswerResultVO;
import com.andrew.smartielts.listening.domain.vo.ListeningQuestionVO;
import com.andrew.smartielts.listening.domain.vo.ListeningRecordDetailVO;
import com.andrew.smartielts.listening.domain.vo.ListeningRecordVO;
import com.andrew.smartielts.listening.domain.vo.ListeningSessionVO;
import com.andrew.smartielts.listening.domain.vo.ListeningTestDetailVO;
import com.andrew.smartielts.listening.mapper.ListeningAnswerRecordMapper;
import com.andrew.smartielts.listening.mapper.ListeningQuestionAnswerRuleMapper;
import com.andrew.smartielts.listening.mapper.ListeningQuestionMapper;
import com.andrew.smartielts.listening.mapper.ListeningRecordMapper;
import com.andrew.smartielts.listening.mapper.ListeningTestMapper;
import com.andrew.smartielts.listening.service.admin.ListeningMaterialService;
import com.andrew.smartielts.listening.service.admin.ListeningPartGroupService;
import com.andrew.smartielts.listening.service.admin.ListeningTestTimerService;
import com.andrew.smartielts.listening.service.user.UserListeningService;
import com.andrew.smartielts.listening.support.ListeningAnswerRecordBuilder;
import com.andrew.smartielts.utils.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserListeningServiceImpl implements UserListeningService {

    private static final String TIMER_MODE_NONE = "NONE";
    private static final String TIMER_MODE_TEST_LEVEL = "TEST_LEVEL";
    private static final String TIMER_MODE_PART_LEVEL = "PART_LEVEL";

    private static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String STATUS_PAUSED = "PAUSED";
    private static final String STATUS_SUBMITTED = "SUBMITTED";
    private static final String STATUS_AUTO_SUBMITTED = "AUTO_SUBMITTED";

    private final ListeningTestMapper listeningTestMapper;
    private final ListeningQuestionMapper listeningQuestionMapper;
    private final ListeningRecordMapper listeningRecordMapper;
    private final ListeningAnswerRecordMapper listeningAnswerRecordMapper;
    private final ListeningQuestionAnswerRuleMapper listeningQuestionAnswerRuleMapper;
    private final ListeningTestTimerService listeningTestTimerService;
    private final ListeningPartGroupService listeningPartGroupService;
    private final ListeningMaterialService listeningMaterialService;
    private final ListeningAnswerRecordBuilder listeningAnswerRecordBuilder;

    public UserListeningServiceImpl(
            ListeningTestMapper listeningTestMapper,
            ListeningQuestionMapper listeningQuestionMapper,
            ListeningRecordMapper listeningRecordMapper,
            ListeningAnswerRecordMapper listeningAnswerRecordMapper,
            ListeningQuestionAnswerRuleMapper listeningQuestionAnswerRuleMapper,
            ListeningTestTimerService listeningTestTimerService,
            ListeningPartGroupService listeningPartGroupService,
            ListeningMaterialService listeningMaterialService,
            ListeningAnswerRecordBuilder listeningAnswerRecordBuilder
    ) {
        this.listeningTestMapper = listeningTestMapper;
        this.listeningQuestionMapper = listeningQuestionMapper;
        this.listeningRecordMapper = listeningRecordMapper;
        this.listeningAnswerRecordMapper = listeningAnswerRecordMapper;
        this.listeningQuestionAnswerRuleMapper = listeningQuestionAnswerRuleMapper;
        this.listeningTestTimerService = listeningTestTimerService;
        this.listeningPartGroupService = listeningPartGroupService;
        this.listeningMaterialService = listeningMaterialService;
        this.listeningAnswerRecordBuilder = listeningAnswerRecordBuilder;
    }

    @Override
    public List<ListeningTest> listTests() {
        return listeningTestMapper.findAllActive();
    }

    @Override
    public ListeningTestDetailVO getTestDetail(Long testId) {
        return buildActiveTestDetailVO(testId);
    }

    @Override
    @Transactional
    public ListeningSessionVO start(Long testId) {
        Long userId = SecurityUtils.getCurrentUserId();
        ListeningTest test = listeningTestMapper.findActiveById(testId);
        if (test == null) {
            throw new RuntimeException("Listening test not found");
        }

        ListeningRecord existing = listeningRecordMapper.findInProgressByTestIdForUser(testId, userId);
        if (existing != null) {
            TestTimerConfig timerConfig = listeningTestTimerService.getByTestId(testId);
            return toSessionVO(existing, timerConfig);
        }

        TestTimerConfig timerConfig = listeningTestTimerService.getByTestId(testId);
        LocalDateTime now = LocalDateTime.now();

        ListeningRecord record = new ListeningRecord();
        record.setUserId(userId);
        record.setTestId(testId);
        record.setSessionId(generateSessionId());
        record.setStartedTime(now);
        record.setSubmittedTime(null);
        record.setTimeLimitSeconds(resolveListeningTimeLimitSeconds(testId, timerConfig));
        record.setTimeSpentSeconds(0);
        record.setTotalScore(0);
        record.setRecordStatus(STATUS_IN_PROGRESS);
        record.setCreatedTime(now);
        record.setIsDeleted(0);

        listeningRecordMapper.insert(record);
        return toSessionVO(record, timerConfig);
    }

    @Override
    public ListeningSessionVO getSession(String sessionId, Long userId) {
        ListeningRecord record = getListeningSessionRecord(sessionId, userId);
        TestTimerConfig timerConfig = listeningTestTimerService.getByTestId(record.getTestId());
        return toSessionVO(record, timerConfig);
    }

    @Override
    @Transactional
    public ListeningSessionVO pause(String sessionId, Long userId, ListeningSessionActionDTO dto) {
        ListeningRecord record = getListeningSessionRecord(sessionId, userId);
        if (!STATUS_IN_PROGRESS.equalsIgnoreCase(trimToNull(record.getRecordStatus()))) {
            throw new RuntimeException("Listening session is not in progress");
        }

        TestTimerConfig timerConfig = listeningTestTimerService.getByTestId(record.getTestId());
        if (timerConfig == null || !enabled(timerConfig.getAllowPause())) {
            throw new RuntimeException("Pause is not allowed for this listening test");
        }

        int timeSpentSeconds = calculateCurrentTimeSpent(
                record,
                dto == null ? null : dto.getClientTimeSpentSeconds()
        );
        record.setTimeSpentSeconds(timeSpentSeconds);
        record.setRecordStatus(STATUS_PAUSED);
        listeningRecordMapper.updateSessionState(record);

        return toSessionVO(record, timerConfig);
    }

    @Override
    @Transactional
    public ListeningSessionVO resume(String sessionId, Long userId) {
        ListeningRecord record = getListeningSessionRecord(sessionId, userId);
        if (!STATUS_PAUSED.equalsIgnoreCase(trimToNull(record.getRecordStatus()))) {
            throw new RuntimeException("Listening session is not paused");
        }

        TestTimerConfig timerConfig = listeningTestTimerService.getByTestId(record.getTestId());
        if (timerConfig == null || !enabled(timerConfig.getAllowPause())) {
            throw new RuntimeException("Pause is not allowed for this listening test");
        }

        LocalDateTime now = LocalDateTime.now();
        int timeSpent = record.getTimeSpentSeconds() == null ? 0 : record.getTimeSpentSeconds();
        record.setStartedTime(now.minusSeconds(timeSpent));
        record.setRecordStatus(STATUS_IN_PROGRESS);
        listeningRecordMapper.updateSessionState(record);

        return toSessionVO(record, timerConfig);
    }

    @Override
    @Transactional
    public ListeningRecordDetailVO submit(Long testId, ListeningSubmitDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        ListeningTest test = listeningTestMapper.findActiveById(testId);
        if (test == null) {
            throw new RuntimeException("Listening test not found");
        }

        List<ListeningQuestion> questions = listeningQuestionMapper.findActiveByTestId(testId);
        if (questions == null) {
            questions = new ArrayList<>();
        }

        TestTimerConfig timerConfig = listeningTestTimerService.getByTestId(testId);
        LocalDateTime now = LocalDateTime.now();

        ListeningRecord record;
        String sessionId = dto == null ? null : trimToNull(dto.getSessionId());
        if (sessionId != null) {
            record = getListeningSessionRecord(sessionId, userId);
            if (!Objects.equals(record.getTestId(), testId)) {
                throw new RuntimeException("Listening session does not belong to test");
            }
            if (isSubmittedStatus(record.getRecordStatus())) {
                throw new RuntimeException("Listening session already submitted");
            }
        } else {
            record = new ListeningRecord();
            record.setUserId(userId);
            record.setTestId(testId);
            record.setSessionId(generateSessionId());
            record.setStartedTime(now);
            record.setSubmittedTime(null);
            record.setTimeLimitSeconds(resolveListeningTimeLimitSeconds(testId, timerConfig));
            record.setTimeSpentSeconds(0);
            record.setTotalScore(0);
            record.setRecordStatus(STATUS_IN_PROGRESS);
            record.setCreatedTime(now);
            record.setIsDeleted(0);
            listeningRecordMapper.insert(record);
        }

        int timeSpentSeconds = calculateCurrentTimeSpent(record, dto == null ? null : dto.getTimeSpentSeconds());
        if (dto != null && dto.getTimeSpentSeconds() != null && dto.getTimeSpentSeconds() >= 0) {
            timeSpentSeconds = Math.max(timeSpentSeconds, dto.getTimeSpentSeconds());
        }

        Integer timeLimitSeconds = record.getTimeLimitSeconds();
        if (timeLimitSeconds == null) {
            timeLimitSeconds = resolveListeningTimeLimitSeconds(testId, timerConfig);
        }

        LocalDateTime startedTime = dto != null && dto.getStartedTime() != null
                ? dto.getStartedTime()
                : resolveStartedTime(record.getStartedTime(), now, timeSpentSeconds);

        boolean timeout = isTimeout(timeLimitSeconds, timeSpentSeconds);
        boolean autoSubmitted = dto != null
                && dto.getAutoSubmitted() != null
                && dto.getAutoSubmitted() == 1;
        boolean finalAutoSubmitted = autoSubmitted || (timeout && isAutoSubmitEnabled(timerConfig));

        Map<Long, List<String>> answerMap = new LinkedHashMap<>();
        if (dto != null && dto.getAnswers() != null) {
            for (ListeningAnswerDTO answerDTO : dto.getAnswers()) {
                if (answerDTO == null || answerDTO.getQuestionId() == null) {
                    continue;
                }
                List<String> rawAnswers = new ArrayList<>();
                if (answerDTO.getAnswers() != null && !answerDTO.getAnswers().isEmpty()) {
                    rawAnswers.addAll(answerDTO.getAnswers());
                } else if (answerDTO.getAnswer() != null) {
                    rawAnswers.add(answerDTO.getAnswer());
                }
                answerMap.put(answerDTO.getQuestionId(), rawAnswers);
            }
        }

        ListeningAnswerRecordBuilder.BuildResult buildResult =
                listeningAnswerRecordBuilder.persist(record.getId(), testId, answerMap);

        int totalScore = buildResult.getTotalScore();

        Map<Long, ListeningAnswerRecordBuilder.QuestionResult> resultMap =
                buildResult.getQuestionResults()
                        .stream()
                        .collect(Collectors.toMap(
                                ListeningAnswerRecordBuilder.QuestionResult::getQuestionId,
                                item -> item,
                                (a, b) -> a,
                                LinkedHashMap::new
                        ));

        List<ListeningQuestionVO> questionVOList = new ArrayList<>();
        List<ListeningAnswerResultVO> answerVOList = new ArrayList<>();

        for (ListeningQuestion question : questions) {
            if (question == null) {
                continue;
            }

            questionVOList.add(toQuestionVO(question));

            ListeningAnswerRecordBuilder.QuestionResult result = resultMap.get(question.getId());

            ListeningAnswerResultVO answerVO = new ListeningAnswerResultVO();
            answerVO.setQuestionId(question.getId());
            answerVO.setQuestionNumber(question.getQuestionNumber());
            answerVO.setQuestionType(question.getQuestionType());
            answerVO.setAnswerMode(question.getAnswerMode());
            answerVO.setQuestionText(question.getQuestionText());
            answerVO.setOptionsJson(question.getOptionsJson());

            if (result != null) {
                answerVO.setUserAnswer(result.getUserAnswer());
                answerVO.setCorrectAnswer(result.getCorrectAnswerDisplay());
                answerVO.setIsCorrect(result.isCorrect() ? 1 : 0);
                answerVO.setScore(result.getEarnedScore());
            } else {
                answerVO.setUserAnswer(null);
                answerVO.setCorrectAnswer(buildDisplayCorrectAnswer(question));
                answerVO.setIsCorrect(0);
                answerVO.setScore(0);
            }

            answerVOList.add(answerVO);
        }

        questionVOList.sort(
                Comparator.comparing(ListeningQuestionVO::getDisplayOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(ListeningQuestionVO::getQuestionNumber, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(ListeningQuestionVO::getId, Comparator.nullsLast(Long::compareTo))
        );

        answerVOList.sort(
                Comparator.comparing(ListeningAnswerResultVO::getQuestionNumber, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(ListeningAnswerResultVO::getQuestionId, Comparator.nullsLast(Long::compareTo))
        );

        record.setStartedTime(startedTime);
        record.setSubmittedTime(now);
        record.setTimeLimitSeconds(timeLimitSeconds);
        record.setTimeSpentSeconds(resolveSubmittedTimeSpentSeconds(
                dto == null ? null : dto.getTimeSpentSeconds(),
                startedTime,
                now
        ));
        record.setTotalScore(totalScore);
        record.setRecordStatus(finalAutoSubmitted ? STATUS_AUTO_SUBMITTED : STATUS_SUBMITTED);

        listeningRecordMapper.updateSessionState(record);
        listeningRecordMapper.updateTotalScore(record.getId(), totalScore);

        ListeningRecordDetailVO detailVO = new ListeningRecordDetailVO();
        detailVO.setRecordId(record.getId());
        detailVO.setTestId(test.getId());
        detailVO.setTestTitle(test.getTitle());
        detailVO.setAudioUrl(test.getAudioUrl());
        detailVO.setTranscriptText(test.getTranscriptText());
        detailVO.setTotalScore(totalScore);
        detailVO.setCreatedTime(record.getCreatedTime());
        detailVO.setQuestions(questionVOList);
        detailVO.setAnswers(answerVOList);
        return detailVO;
    }

    @Override
    public PageResult<ListeningRecordVO> pageActiveRecords(Long userId, UserListeningRecordPageQuery query) {
        UserListeningRecordPageQuery safeQuery = query == null ? new UserListeningRecordPageQuery() : query;

        RecordQueryValidator.validate(
                safeQuery.getPageNum(),
                safeQuery.getPageSize(),
                userId,
                safeQuery.getTestId(),
                safeQuery.getMinScore(),
                safeQuery.getMaxScore(),
                safeQuery.getStartTime(),
                safeQuery.getEndTime()
        );

        int pageNum = normalizePageNum(safeQuery.getPageNum());
        int pageSize = normalizePageSize(safeQuery.getPageSize());
        int offset = (pageNum - 1) * pageSize;

        Long total = listeningRecordMapper.countUserActive(userId, safeQuery);
        if (total == null || total == 0L) {
            return new PageResult<>(new ArrayList<>(), 0L, pageNum, pageSize);
        }

        List<ListeningRecord> records = listeningRecordMapper.pageUserActive(userId, safeQuery, offset, pageSize);
        List<ListeningRecordVO> voList = new ArrayList<>();
        if (records != null) {
            for (ListeningRecord record : records) {
                voList.add(toRecordVO(record));
            }
        }

        return new PageResult<>(voList, total, pageNum, pageSize);
    }

    @Override
    public PageResult<ListeningRecordVO> pageDeletedRecords(Long userId, UserListeningDeletedRecordPageQuery query) {
        UserListeningDeletedRecordPageQuery safeQuery = query == null ? new UserListeningDeletedRecordPageQuery() : query;

        int pageNum = normalizePageNum(safeQuery.getPageNum());
        int pageSize = normalizePageSize(safeQuery.getPageSize());
        int offset = (pageNum - 1) * pageSize;

        Long total = listeningRecordMapper.countUserDeleted(userId, safeQuery);
        if (total == null || total == 0L) {
            return new PageResult<>(new ArrayList<>(), 0L, pageNum, pageSize);
        }

        List<ListeningRecord> records = listeningRecordMapper.pageUserDeleted(userId, safeQuery, offset, pageSize);
        List<ListeningRecordVO> voList = new ArrayList<>();
        if (records != null) {
            for (ListeningRecord record : records) {
                voList.add(toRecordVO(record));
            }
        }

        return new PageResult<>(voList, total, pageNum, pageSize);
    }

    @Override
    public ListeningRecordDetailVO getRecord(Long recordId, Long userId) {
        ListeningRecord record = listeningRecordMapper.findAnyByIdForUser(recordId, userId);
        if (record == null) {
            throw new RuntimeException("Listening record not found");
        }

        ListeningTest test = listeningTestMapper.findAnyById(record.getTestId());
        if (test == null) {
            throw new RuntimeException("Listening test not found");
        }

        List<ListeningQuestion> questions = listeningQuestionMapper.findAnyByTestId(test.getId());
        List<ListeningAnswerRecord> answerRecords = listeningAnswerRecordMapper.findByRecordId(recordId);

        if (questions == null) {
            questions = new ArrayList<>();
        }
        if (answerRecords == null) {
            answerRecords = new ArrayList<>();
        }

        List<ListeningQuestionVO> questionVOList = new ArrayList<>();
        List<ListeningAnswerResultVO> answerVOList = new ArrayList<>();

        for (ListeningQuestion question : questions) {
            if (question == null) {
                continue;
            }

            questionVOList.add(toQuestionVO(question));

            ListeningAnswerRecord matched = answerRecords.stream()
                    .filter(answer -> Objects.equals(answer.getQuestionId(), question.getId()))
                    .findFirst()
                    .orElse(null);

            ListeningAnswerResultVO answerVO = new ListeningAnswerResultVO();
            answerVO.setQuestionId(question.getId());
            answerVO.setQuestionNumber(question.getQuestionNumber());
            answerVO.setQuestionType(question.getQuestionType());
            answerVO.setAnswerMode(question.getAnswerMode());
            answerVO.setQuestionText(question.getQuestionText());
            answerVO.setOptionsJson(question.getOptionsJson());
            answerVO.setCorrectAnswer(buildDisplayCorrectAnswer(question));

            if (matched != null) {
                answerVO.setUserAnswer(matched.getUserAnswer());
                answerVO.setIsCorrect(matched.getIsCorrect());
                answerVO.setScore(matched.getScore());
            } else {
                answerVO.setUserAnswer(null);
                answerVO.setIsCorrect(0);
                answerVO.setScore(0);
            }

            answerVOList.add(answerVO);
        }

        questionVOList.sort(
                Comparator.comparing(ListeningQuestionVO::getDisplayOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(ListeningQuestionVO::getQuestionNumber, Comparator.nullsLast(Integer::compareTo))
        );

        answerVOList.sort(
                Comparator.comparing(ListeningAnswerResultVO::getQuestionNumber, Comparator.nullsLast(Integer::compareTo))
        );

        ListeningRecordDetailVO detailVO = new ListeningRecordDetailVO();
        detailVO.setRecordId(record.getId());
        detailVO.setTestId(test.getId());
        detailVO.setTestTitle(test.getTitle());
        detailVO.setAudioUrl(test.getAudioUrl());
        detailVO.setTranscriptText(test.getTranscriptText());
        detailVO.setTotalScore(record.getTotalScore());
        detailVO.setCreatedTime(record.getCreatedTime());
        detailVO.setQuestions(questionVOList);
        detailVO.setAnswers(answerVOList);
        return detailVO;
    }

    @Override
    @Transactional
    public void deleteRecord(Long recordId, Long userId) {
        ListeningRecord record = listeningRecordMapper.findAnyByIdForUser(recordId, userId);
        if (record == null) {
            throw new RuntimeException("Listening record not found");
        }
        listeningRecordMapper.softDeleteByIdForUser(recordId, userId);
    }

    @Override
    @Transactional
    public void restoreRecord(Long recordId, Long userId) {
        ListeningRecord record = listeningRecordMapper.findAnyByIdForUser(recordId, userId);
        if (record == null) {
            throw new RuntimeException("Listening record not found");
        }
        listeningRecordMapper.restoreByIdForUser(recordId, userId);
    }

    private ListeningTestDetailVO buildActiveTestDetailVO(Long testId) {
        ListeningTest test = listeningTestMapper.findActiveById(testId);
        if (test == null) {
            throw new RuntimeException("Listening test not found");
        }

        List<ListeningQuestion> questions = listeningQuestionMapper.findActiveByTestId(testId);
        if (questions == null) {
            questions = new ArrayList<>();
        }

        List<ListeningQuestionVO> questionVOList = new ArrayList<>();
        for (ListeningQuestion question : questions) {
            if (question == null) {
                continue;
            }
            questionVOList.add(toQuestionVO(question));
        }

        questionVOList.sort(
                Comparator.comparing(ListeningQuestionVO::getDisplayOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(ListeningQuestionVO::getQuestionNumber, Comparator.nullsLast(Integer::compareTo))
        );

        List<TestPartGroup> partGroups = listeningPartGroupService.listActiveByTestId(testId);
        if (partGroups != null) {
            partGroups.sort(
                    Comparator.comparing(TestPartGroup::getDisplayOrder, Comparator.nullsLast(Integer::compareTo))
                            .thenComparing(TestPartGroup::getPartNumber, Comparator.nullsLast(Integer::compareTo))
                            .thenComparing(TestPartGroup::getGroupNumber, Comparator.nullsLast(Integer::compareTo))
            );
        }

        List<ListeningMaterial> materials = listeningMaterialService.listActiveByTestId(testId);
        if (materials != null) {
            materials.sort(
                    Comparator.comparing(ListeningMaterial::getDisplayOrder, Comparator.nullsLast(Integer::compareTo))
                            .thenComparing(ListeningMaterial::getId, Comparator.nullsLast(Long::compareTo))
            );
        }

        ListeningTestDetailVO detailVO = new ListeningTestDetailVO();
        detailVO.setId(test.getId());
        detailVO.setTitle(test.getTitle());
        detailVO.setAudioUrl(test.getAudioUrl());
        detailVO.setTranscriptText(test.getTranscriptText());
        detailVO.setTotalScore(test.getTotalScore());
        detailVO.setTimerConfig(listeningTestTimerService.getByTestId(testId));
        detailVO.setPartGroups(partGroups);
        detailVO.setMaterials(materials);
        detailVO.setQuestions(questionVOList);
        return detailVO;
    }

    private boolean isSubmittedStatus(String status) {
        String value = trimToNull(status);
        return STATUS_SUBMITTED.equalsIgnoreCase(value) || STATUS_AUTO_SUBMITTED.equalsIgnoreCase(value);
    }

    private Integer resolveListeningTimeLimitSeconds(Long testId, TestTimerConfig timerConfig) {
        if (timerConfig == null) {
            return null;
        }

        String timerMode = trimToNull(timerConfig.getTimerMode());
        if (timerMode == null || TIMER_MODE_NONE.equalsIgnoreCase(timerMode)) {
            return null;
        }

        if (TIMER_MODE_TEST_LEVEL.equalsIgnoreCase(timerMode)) {
            return timerConfig.getTotalSeconds();
        }

        if (TIMER_MODE_PART_LEVEL.equalsIgnoreCase(timerMode)) {
            List<TestPartGroup> partGroups = listeningPartGroupService.listActiveByTestId(testId);
            if (partGroups == null || partGroups.isEmpty()) {
                return null;
            }

            int total = partGroups.stream()
                    .filter(Objects::nonNull)
                    .map(TestPartGroup::getTimeLimitSeconds)
                    .filter(Objects::nonNull)
                    .filter(v -> v > 0)
                    .reduce(0, Integer::sum);

            return total > 0 ? total : null;
        }

        return null;
    }

    private Integer resolveSubmittedTimeSpentSeconds(
            Integer providedTimeSpent,
            LocalDateTime startedTime,
            LocalDateTime now
    ) {
        if (providedTimeSpent != null && providedTimeSpent >= 0) {
            return providedTimeSpent;
        }
        if (startedTime != null) {
            long seconds = Duration.between(startedTime, now).getSeconds();
            return (int) Math.max(seconds, 0);
        }
        return 0;
    }

    private LocalDateTime resolveStartedTime(
            LocalDateTime startedTime,
            LocalDateTime now,
            Integer timeSpentSeconds
    ) {
        if (startedTime != null) {
            return startedTime;
        }
        int safeSeconds = timeSpentSeconds == null || timeSpentSeconds < 0 ? 0 : timeSpentSeconds;
        return now.minusSeconds(safeSeconds);
    }

    private boolean isTimeout(Integer timeLimitSeconds, Integer timeSpentSeconds) {
        return timeLimitSeconds != null
                && timeLimitSeconds > 0
                && timeSpentSeconds != null
                && timeSpentSeconds >= timeLimitSeconds;
    }

    private boolean isAutoSubmitEnabled(TestTimerConfig timerConfig) {
        return timerConfig != null && enabled(timerConfig.getAutoSubmit());
    }

    private ListeningRecord getListeningSessionRecord(String sessionId, Long userId) {
        String value = trimToNull(sessionId);
        if (value == null) {
            throw new RuntimeException("sessionId is required");
        }

        ListeningRecord record = listeningRecordMapper.findBySessionIdForUser(value, userId);
        if (record == null) {
            throw new RuntimeException("Listening session not found");
        }
        if (record.getIsDeleted() != null && record.getIsDeleted() == 1) {
            throw new RuntimeException("Listening session is deleted");
        }
        return record;
    }

    private ListeningSessionVO toSessionVO(ListeningRecord record, TestTimerConfig timerConfig) {
        ListeningSessionVO vo = new ListeningSessionVO();
        vo.setRecordId(record.getId());
        vo.setTestId(record.getTestId());
        vo.setSessionId(record.getSessionId());
        vo.setRecordStatus(record.getRecordStatus());
        vo.setStartedTime(record.getStartedTime());
        vo.setSubmittedTime(record.getSubmittedTime());
        vo.setTimeLimitSeconds(record.getTimeLimitSeconds());

        int timeSpent = calculateCurrentTimeSpent(record, null);
        vo.setTimeSpentSeconds(timeSpent);
        vo.setRemainingSeconds(calculateRemainingSeconds(record.getTimeLimitSeconds(), timeSpent));
        vo.setAllowPause(timerConfig == null ? 0 : timerConfig.getAllowPause());
        vo.setAutoSubmit(timerConfig == null ? 1 : timerConfig.getAutoSubmit());
        return vo;
    }

    private int calculateCurrentTimeSpent(ListeningRecord record, Integer clientTimeSpentSeconds) {
        int stored = record.getTimeSpentSeconds() == null ? 0 : record.getTimeSpentSeconds();

        String status = trimToNull(record.getRecordStatus());
        if (STATUS_PAUSED.equalsIgnoreCase(status)
                || STATUS_SUBMITTED.equalsIgnoreCase(status)
                || STATUS_AUTO_SUBMITTED.equalsIgnoreCase(status)) {
            return mergeClientTimeSpent(stored, clientTimeSpentSeconds);
        }

        if (record.getStartedTime() == null) {
            return mergeClientTimeSpent(stored, clientTimeSpentSeconds);
        }

        long elapsed = Duration.between(record.getStartedTime(), LocalDateTime.now()).getSeconds();
        int serverSpent = (int) Math.max(elapsed, 0);
        return Math.max(serverSpent, mergeClientTimeSpent(stored, clientTimeSpentSeconds));
    }

    private int mergeClientTimeSpent(int stored, Integer clientTimeSpentSeconds) {
        if (clientTimeSpentSeconds == null || clientTimeSpentSeconds < 0) {
            return stored;
        }
        return Math.max(stored, clientTimeSpentSeconds);
    }

    private Integer calculateRemainingSeconds(Integer limit, Integer spent) {
        if (limit == null || limit <= 0) {
            return null;
        }
        int safeSpent = spent == null ? 0 : spent;
        return Math.max(limit - safeSpent, 0);
    }

    private String generateSessionId() {
        return "lst-" + UUID.randomUUID().toString().replace("-", "");
    }

    private ListeningQuestionVO toQuestionVO(ListeningQuestion question) {
        ListeningQuestionVO vo = new ListeningQuestionVO();
        vo.setId(question.getId());
        vo.setPartGroupId(question.getPartGroupId());
        vo.setMaterialId(question.getMaterialId());
        vo.setSectionNumber(question.getSectionNumber());
        vo.setQuestionNumber(question.getQuestionNumber());
        vo.setQuestionType(question.getQuestionType());
        vo.setAnswerMode(question.getAnswerMode());
        vo.setQuestionText(question.getQuestionText());
        vo.setCorrectAnswer(question.getCorrectAnswer());
        vo.setOptionsJson(question.getOptionsJson());
        vo.setAcceptedAnswersJson(question.getAcceptedAnswersJson());
        vo.setCaseInsensitive(question.getCaseInsensitive());
        vo.setIgnoreWhitespace(question.getIgnoreWhitespace());
        vo.setIgnorePunctuation(question.getIgnorePunctuation());
        vo.setDisplayOrder(question.getDisplayOrder());
        vo.setScore(question.getScore());
        vo.setAnswerRules(listeningQuestionAnswerRuleMapper.findByQuestionId(question.getId()));
        return vo;
    }

    private String buildDisplayCorrectAnswer(ListeningQuestion question) {
        if (question == null) {
            return null;
        }

        List<QuestionAnswerRule> rules = listeningQuestionAnswerRuleMapper.findByQuestionId(question.getId());
        if (rules != null && !rules.isEmpty()) {
            Map<Integer, List<QuestionAnswerRule>> blankMap = rules.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.groupingBy(
                            rule -> rule.getBlankNo() == null ? 1 : rule.getBlankNo(),
                            TreeMap::new,
                            Collectors.toList()
                    ));

            List<String> parts = new ArrayList<>();
            for (List<QuestionAnswerRule> ruleList : blankMap.values()) {
                QuestionAnswerRule primary = ruleList.stream()
                        .filter(rule -> rule.getIsPrimary() != null && rule.getIsPrimary() == 1)
                        .findFirst()
                        .orElse(ruleList.get(0));

                String text = trimToNull(firstNonBlank(primary.getAnswerText(), primary.getNormalizedAnswer()));
                if (text != null) {
                    parts.add(text);
                }
            }

            if (!parts.isEmpty()) {
                return String.join(", ", parts);
            }
        }

        String correctAnswer = trimToNull(question.getCorrectAnswer());
        if (correctAnswer != null) {
            return correctAnswer;
        }

        return trimToNull(question.getAcceptedAnswersJson());
    }

    private ListeningRecordVO toRecordVO(ListeningRecord record) {
        ListeningRecordVO vo = new ListeningRecordVO();
        vo.setId(record.getId());
        vo.setUserId(record.getUserId());
        vo.setTestId(record.getTestId());
        vo.setTotalScore(record.getTotalScore());
        vo.setCreatedTime(record.getCreatedTime());
        vo.setIsDeleted(record.getIsDeleted());

        ListeningTest test = listeningTestMapper.findAnyById(record.getTestId());
        vo.setTestTitle(test == null ? null : test.getTitle());
        return vo;
    }

    private int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? 10 : pageSize;
    }

    private boolean enabled(Integer flag) {
        return flag != null && flag == 1;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String firstNonBlank(String first, String second) {
        String firstValue = trimToNull(first);
        if (firstValue != null) {
            return firstValue;
        }
        return trimToNull(second);
    }
}
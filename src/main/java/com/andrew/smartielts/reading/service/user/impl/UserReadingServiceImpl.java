package com.andrew.smartielts.reading.service.user.impl;

import com.andrew.smartielts.common.constants.RecordQueryValidator;
import com.andrew.smartielts.common.domain.pojo.QuestionAnswerRule;
import com.andrew.smartielts.common.domain.pojo.TestPartGroup;
import com.andrew.smartielts.common.domain.pojo.TestTimerConfig;
import com.andrew.smartielts.common.page.PageResult;
import com.andrew.smartielts.common.support.QuestionAnswerRuleJudgeSupport;
import com.andrew.smartielts.reading.domain.dto.ReadingAnswerDTO;
import com.andrew.smartielts.reading.domain.dto.ReadingSessionActionDTO;
import com.andrew.smartielts.reading.domain.dto.ReadingSubmitDTO;
import com.andrew.smartielts.reading.domain.pojo.ReadingAnswerRecord;
import com.andrew.smartielts.reading.domain.pojo.ReadingPassage;
import com.andrew.smartielts.reading.domain.pojo.ReadingQuestion;
import com.andrew.smartielts.reading.domain.pojo.ReadingRecord;
import com.andrew.smartielts.reading.domain.pojo.ReadingTest;
import com.andrew.smartielts.reading.domain.query.user.UserReadingDeletedRecordPageQuery;
import com.andrew.smartielts.reading.domain.query.user.UserReadingRecordPageQuery;
import com.andrew.smartielts.reading.domain.vo.ReadingAnswerResultVO;
import com.andrew.smartielts.reading.domain.vo.ReadingPassageVO;
import com.andrew.smartielts.reading.domain.vo.ReadingQuestionVO;
import com.andrew.smartielts.reading.domain.vo.ReadingRecordDetailVO;
import com.andrew.smartielts.reading.domain.vo.ReadingRecordVO;
import com.andrew.smartielts.reading.domain.vo.ReadingSessionVO;
import com.andrew.smartielts.reading.domain.vo.ReadingTestDetailVO;
import com.andrew.smartielts.reading.mapper.ReadingAnswerRecordMapper;
import com.andrew.smartielts.reading.mapper.ReadingPassageMapper;
import com.andrew.smartielts.reading.mapper.ReadingQuestionAnswerRuleMapper;
import com.andrew.smartielts.reading.mapper.ReadingQuestionMapper;
import com.andrew.smartielts.reading.mapper.ReadingRecordMapper;
import com.andrew.smartielts.reading.mapper.ReadingTestMapper;
import com.andrew.smartielts.reading.service.admin.ReadingPartGroupService;
import com.andrew.smartielts.reading.service.admin.ReadingTestTimerService;
import com.andrew.smartielts.reading.service.user.UserReadingService;
import com.andrew.smartielts.utils.SecurityUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class UserReadingServiceImpl implements UserReadingService {

    private static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String STATUS_PAUSED = "PAUSED";
    private static final String STATUS_SUBMITTED = "SUBMITTED";
    private static final String STATUS_AUTO_SUBMITTED = "AUTO_SUBMITTED";

    private static final String TIMER_MODE_NONE = "NONE";
    private static final String TIMER_MODE_TEST_LEVEL = "TEST_LEVEL";
    private static final String TIMER_MODE_PART_LEVEL = "PART_LEVEL";

    private final ReadingTestMapper reading_test_mapper;
    private final ReadingPassageMapper reading_passage_mapper;
    private final ReadingQuestionMapper reading_question_mapper;
    private final ReadingRecordMapper reading_record_mapper;
    private final ReadingAnswerRecordMapper reading_answer_record_mapper;
    private final ReadingQuestionAnswerRuleMapper reading_question_answer_rule_mapper;
    private final ReadingTestTimerService reading_test_timer_service;
    private final ReadingPartGroupService reading_part_group_service;
    private final QuestionAnswerRuleJudgeSupport judge_support;
    private final ObjectMapper object_mapper = new ObjectMapper();

    public UserReadingServiceImpl(ReadingTestMapper reading_test_mapper,
                                  ReadingPassageMapper reading_passage_mapper,
                                  ReadingQuestionMapper reading_question_mapper,
                                  ReadingRecordMapper reading_record_mapper,
                                  ReadingAnswerRecordMapper reading_answer_record_mapper,
                                  ReadingQuestionAnswerRuleMapper reading_question_answer_rule_mapper,
                                  ReadingTestTimerService reading_test_timer_service,
                                  ReadingPartGroupService reading_part_group_service,
                                  QuestionAnswerRuleJudgeSupport judge_support) {
        this.reading_test_mapper = reading_test_mapper;
        this.reading_passage_mapper = reading_passage_mapper;
        this.reading_question_mapper = reading_question_mapper;
        this.reading_record_mapper = reading_record_mapper;
        this.reading_answer_record_mapper = reading_answer_record_mapper;
        this.reading_question_answer_rule_mapper = reading_question_answer_rule_mapper;
        this.reading_test_timer_service = reading_test_timer_service;
        this.reading_part_group_service = reading_part_group_service;
        this.judge_support = judge_support;
    }

    @Override
    public List<ReadingTest> listTests() {
        return reading_test_mapper.findAllActive();
    }

    @Override
    public ReadingTestDetailVO getTestDetail(Long test_id) {
        ReadingTest test = reading_test_mapper.findActiveById(test_id);
        if (test == null) {
            throw new RuntimeException("Reading test not found");
        }

        List<ReadingPassage> passages = reading_passage_mapper.findActiveByTestId(test_id);

        ReadingTestDetailVO detail_vo = new ReadingTestDetailVO();
        detail_vo.setId(test.getId());
        detail_vo.setTitle(test.getTitle());
        detail_vo.setTotalScore(test.getTotalScore());
        detail_vo.setTimerConfig(reading_test_timer_service.getByTestId(test_id));
        detail_vo.setPartGroups(reading_part_group_service.listActiveByTestId(test_id));
        detail_vo.setPassages(build_passage_vo_list(passages, true));
        return detail_vo;
    }

    @Override
    @Transactional
    public ReadingSessionVO start(Long test_id) {
        Long user_id = SecurityUtils.getCurrentUserId();

        ReadingTest test = reading_test_mapper.findActiveById(test_id);
        if (test == null) {
            throw new RuntimeException("Reading test not found");
        }

        ReadingRecord existing_record = reading_record_mapper.findInProgressByTestIdForUser(test_id, user_id);
        TestTimerConfig timer_config = reading_test_timer_service.getByTestId(test_id);

        if (existing_record != null) {
            return to_session_vo(existing_record, timer_config);
        }

        LocalDateTime now = LocalDateTime.now();

        ReadingRecord record = new ReadingRecord();
        record.setUserId(user_id);
        record.setTestId(test_id);
        record.setSessionId(generate_session_id());
        record.setStartedTime(now);
        record.setSubmittedTime(null);
        record.setTimeLimitSeconds(resolve_reading_time_limit_seconds(test_id, timer_config));
        record.setTimeSpentSeconds(0);
        record.setTotalScore(0);
        record.setRecordStatus(STATUS_IN_PROGRESS);
        record.setCreatedTime(now);
        record.setIsDeleted(0);

        reading_record_mapper.insertReadingRecord(record);
        return to_session_vo(record, timer_config);
    }

    @Override
    public ReadingSessionVO getSession(String session_id, Long user_id) {
        ReadingRecord record = get_reading_session_record(session_id, user_id);
        TestTimerConfig timer_config = reading_test_timer_service.getByTestId(record.getTestId());
        return to_session_vo(record, timer_config);
    }

    @Override
    @Transactional
    public ReadingSessionVO pause(String session_id, Long user_id, ReadingSessionActionDTO dto) {
        ReadingRecord record = get_reading_session_record(session_id, user_id);

        if (!is_status_in_progress(record.getRecordStatus())) {
            throw new RuntimeException("Reading session is not in progress");
        }

        TestTimerConfig timer_config = reading_test_timer_service.getByTestId(record.getTestId());
        if (timer_config == null || !enabled(timer_config.getAllowPause())) {
            throw new RuntimeException("Pause is not allowed for this reading test");
        }

        int time_spent_seconds = calculate_current_time_spent(
                record,
                dto == null ? null : dto.getClientTimeSpentSeconds()
        );

        record.setTimeSpentSeconds(time_spent_seconds);
        record.setRecordStatus(STATUS_PAUSED);
        reading_record_mapper.updateSessionState(record);

        return to_session_vo(record, timer_config);
    }

    @Override
    @Transactional
    public ReadingSessionVO resume(String session_id, Long user_id) {
        ReadingRecord record = get_reading_session_record(session_id, user_id);

        if (!is_status_paused(record.getRecordStatus())) {
            throw new RuntimeException("Reading session is not paused");
        }

        TestTimerConfig timer_config = reading_test_timer_service.getByTestId(record.getTestId());
        if (timer_config == null || !enabled(timer_config.getAllowPause())) {
            throw new RuntimeException("Pause is not allowed for this reading test");
        }

        LocalDateTime now = LocalDateTime.now();
        int time_spent_seconds = record.getTimeSpentSeconds() == null ? 0 : record.getTimeSpentSeconds();

        record.setStartedTime(now.minusSeconds(time_spent_seconds));
        record.setRecordStatus(STATUS_IN_PROGRESS);
        reading_record_mapper.updateSessionState(record);

        return to_session_vo(record, timer_config);
    }

    @Override
    @Transactional
    public ReadingRecordDetailVO submit(Long test_id, ReadingSubmitDTO dto) {
        Long user_id = SecurityUtils.getCurrentUserId();

        ReadingTest test = reading_test_mapper.findActiveById(test_id);
        if (test == null) {
            throw new RuntimeException("Reading test not found");
        }

        List<ReadingPassage> passages = reading_passage_mapper.findActiveByTestId(test_id);
        List<ReadingQuestion> all_questions = new ArrayList<>();
        if (passages != null) {
            for (ReadingPassage passage : passages) {
                if (passage == null || passage.getId() == null) {
                    continue;
                }
                List<ReadingQuestion> question_list = reading_question_mapper.findActiveByPassageId(passage.getId());
                if (question_list != null) {
                    all_questions.addAll(question_list);
                }
            }
        }

        TestTimerConfig timer_config = reading_test_timer_service.getByTestId(test_id);
        LocalDateTime now = LocalDateTime.now();

        ReadingRecord record;
        String session_id = dto == null ? null : trim_to_null(dto.getSessionId());

        if (session_id != null) {
            record = get_reading_session_record(session_id, user_id);
            if (!Objects.equals(record.getTestId(), test_id)) {
                throw new RuntimeException("Reading session does not belong to test");
            }
            if (is_status_submitted(record.getRecordStatus()) || is_status_auto_submitted(record.getRecordStatus())) {
                throw new RuntimeException("Reading session already submitted");
            }
        } else {
            record = new ReadingRecord();
            record.setUserId(user_id);
            record.setTestId(test_id);
            record.setSessionId(generate_session_id());
            record.setCreatedTime(now);
            record.setIsDeleted(0);
            record.setTotalScore(0);
            record.setRecordStatus(STATUS_IN_PROGRESS);
            record.setTimeLimitSeconds(resolve_reading_time_limit_seconds(test_id, timer_config));
            record.setTimeSpentSeconds(0);
            record.setStartedTime(now);
            record.setSubmittedTime(null);
            reading_record_mapper.insertReadingRecord(record);
        }

        int time_spent_seconds = calculate_current_time_spent(
                record,
                dto == null ? null : dto.getTimeSpentSeconds()
        );
        if (dto != null && dto.getTimeSpentSeconds() != null && dto.getTimeSpentSeconds() >= 0) {
            time_spent_seconds = Math.max(time_spent_seconds, dto.getTimeSpentSeconds());
        }

        Integer time_limit_seconds = record.getTimeLimitSeconds();
        if (time_limit_seconds == null) {
            time_limit_seconds = resolve_reading_time_limit_seconds(test_id, timer_config);
        }

        LocalDateTime started_time = dto != null && dto.getStartedTime() != null
                ? dto.getStartedTime()
                : resolve_started_time(record.getStartedTime(), now, time_spent_seconds);

        boolean timeout = is_timeout(time_limit_seconds, time_spent_seconds);
        boolean auto_submitted = dto != null
                && dto.getAutoSubmitted() != null
                && dto.getAutoSubmitted() == 1;
        boolean final_auto_submitted = auto_submitted || (timeout && is_auto_submit_enabled(timer_config));

        Map<Long, ReadingAnswerDTO> answer_map = build_answer_input_map(dto == null ? null : dto.getAnswers());

        int total_score = 0;
        List<ReadingAnswerResultVO> answer_result_list = new ArrayList<>();

        for (ReadingQuestion question : all_questions) {
            if (question == null || question.getId() == null) {
                continue;
            }

            ReadingAnswerDTO answer_dto = answer_map.get(question.getId());
            List<String> raw_answers = normalize_raw_list(
                    answer_dto == null ? null : answer_dto.getAnswer(),
                    answer_dto == null ? null : answer_dto.getAnswers()
            );

            List<QuestionAnswerRule> rules = reading_question_answer_rule_mapper.findByQuestionId(question.getId());
            var grade_result = judge_support.grade(
                    raw_answers,
                    question.getAnswerMode(),
                    question.getCorrectAnswer(),
                    question.getAcceptedAnswersJson(),
                    question.getCaseInsensitive(),
                    question.getIgnoreWhitespace(),
                    question.getIgnorePunctuation(),
                    rules,
                    question.getScore()
            );

            int score = grade_result.getEarnedScore();
            total_score += score;

            ReadingAnswerRecord answer_record = new ReadingAnswerRecord();
            answer_record.setRecordId(record.getId());
            answer_record.setQuestionId(question.getId());
            answer_record.setPartGroupId(question.getPartGroupId());
            answer_record.setUserAnswer(grade_result.getStoredUserAnswer());
            answer_record.setNormalizedAnswer(grade_result.getNormalizedUserAnswer());
            answer_record.setRawAnswersJson(grade_result.getRawAnswersJson());
            answer_record.setIsCorrect(grade_result.isCorrect() ? 1 : 0);
            answer_record.setScore(score);
            reading_answer_record_mapper.insertReadingAnswerRecord(answer_record);

            ReadingAnswerResultVO result_vo = new ReadingAnswerResultVO();
            result_vo.setQuestionId(question.getId());
            result_vo.setQuestionText(question.getQuestionText());
            result_vo.setQuestionType(question.getQuestionType());
            result_vo.setAnswerMode(question.getAnswerMode());
            result_vo.setOptionsJson(question.getOptionsJson());
            result_vo.setUserAnswer(grade_result.getStoredUserAnswer());
            result_vo.setCorrectAnswer(grade_result.getDisplayCorrectAnswer());
            result_vo.setIsCorrect(grade_result.isCorrect() ? 1 : 0);
            result_vo.setScore(score);
            answer_result_list.add(result_vo);
        }

        record.setStartedTime(started_time);
        record.setSubmittedTime(now);
        record.setTimeLimitSeconds(time_limit_seconds);
        record.setTimeSpentSeconds(resolve_submitted_time_spent_seconds(
                dto == null ? null : dto.getTimeSpentSeconds(),
                started_time,
                now
        ));
        record.setTotalScore(total_score);
        record.setRecordStatus(final_auto_submitted ? STATUS_AUTO_SUBMITTED : STATUS_SUBMITTED);

        reading_record_mapper.updateSessionState(record);
        reading_record_mapper.updateTotalScore(record.getId(), total_score);

        ReadingRecordDetailVO detail_vo = new ReadingRecordDetailVO();
        detail_vo.setRecordId(record.getId());
        detail_vo.setTestId(record.getTestId());
        detail_vo.setTestTitle(test.getTitle());
        detail_vo.setTotalScore(record.getTotalScore());
        detail_vo.setCreatedTime(record.getCreatedTime());
        detail_vo.setPassages(build_passage_vo_list(passages, true));
        detail_vo.setAnswers(answer_result_list);
        return detail_vo;
    }

    @Override
    public PageResult<ReadingRecordVO> pageActiveRecords(Long user_id, UserReadingRecordPageQuery query) {
        UserReadingRecordPageQuery safe_query = query == null ? new UserReadingRecordPageQuery() : query;

        RecordQueryValidator.validate(
                safe_query.getPageNum(),
                safe_query.getPageSize(),
                user_id,
                safe_query.getTestId(),
                safe_query.getMinScore(),
                safe_query.getMaxScore(),
                safe_query.getStartTime(),
                safe_query.getEndTime()
        );

        int page_num = normalize_page_num(safe_query.getPageNum());
        int page_size = normalize_page_size(safe_query.getPageSize());
        int offset = (page_num - 1) * page_size;

        Long total = reading_record_mapper.countUserActive(user_id, safe_query);
        if (total == null || total <= 0L) {
            return new PageResult<>(new ArrayList<>(), 0L, page_num, page_size);
        }

        List<ReadingRecord> records = reading_record_mapper.pageUserActive(user_id, safe_query, offset, page_size);
        List<ReadingRecordVO> vo_list = new ArrayList<>();
        if (records != null) {
            for (ReadingRecord record : records) {
                if (record == null) {
                    continue;
                }
                vo_list.add(to_record_vo(record));
            }
        }
        return new PageResult<>(vo_list, total, page_num, page_size);
    }

    @Override
    public PageResult<ReadingRecordVO> pageDeletedRecords(Long user_id, UserReadingDeletedRecordPageQuery query) {
        UserReadingDeletedRecordPageQuery safe_query = query == null ? new UserReadingDeletedRecordPageQuery() : query;

        int page_num = normalize_page_num(safe_query.getPageNum());
        int page_size = normalize_page_size(safe_query.getPageSize());
        int offset = (page_num - 1) * page_size;

        Long total = reading_record_mapper.countUserDeleted(user_id, safe_query);
        if (total == null || total <= 0L) {
            return new PageResult<>(new ArrayList<>(), 0L, page_num, page_size);
        }

        List<ReadingRecord> records = reading_record_mapper.pageUserDeleted(user_id, safe_query, offset, page_size);
        List<ReadingRecordVO> vo_list = new ArrayList<>();
        if (records != null) {
            for (ReadingRecord record : records) {
                if (record == null) {
                    continue;
                }
                vo_list.add(to_record_vo(record));
            }
        }
        return new PageResult<>(vo_list, total, page_num, page_size);
    }

    @Override
    public ReadingRecordDetailVO getRecord(Long record_id, Long user_id) {
        ReadingRecord record = reading_record_mapper.findAnyByIdForUser(record_id, user_id);
        if (record == null) {
            throw new RuntimeException("Reading record not found");
        }

        ReadingTest test = reading_test_mapper.findAnyById(record.getTestId());
        if (test == null) {
            throw new RuntimeException("Reading test not found");
        }

        List<ReadingPassage> passages = reading_passage_mapper.findAnyByTestId(record.getTestId());
        List<ReadingAnswerRecord> answer_records = reading_answer_record_mapper.findByRecordId(record_id);

        List<ReadingPassageVO> passage_vo_list = new ArrayList<>();
        List<ReadingAnswerResultVO> answer_vo_list = new ArrayList<>();

        if (passages != null) {
            for (ReadingPassage passage : passages) {
                if (passage == null) {
                    continue;
                }

                ReadingPassageVO passage_vo = new ReadingPassageVO();
                passage_vo.setId(passage.getId());
                passage_vo.setPartGroupId(passage.getPartGroupId());
                passage_vo.setPassageNo(passage.getPassageNo());
                passage_vo.setTitle(passage.getTitle());
                passage_vo.setContent(passage.getContent());
                passage_vo.setMaterialType(passage.getMaterialType());
                passage_vo.setDisplayOrder(passage.getDisplayOrder());

                List<ReadingQuestion> question_list = reading_question_mapper.findAnyByPassageId(passage.getId());
                List<ReadingQuestionVO> question_vo_list = new ArrayList<>();

                if (question_list != null) {
                    for (ReadingQuestion question : question_list) {
                        if (question == null) {
                            continue;
                        }

                        question_vo_list.add(to_question_vo(question));

                        ReadingAnswerRecord matched = find_matched_answer(answer_records, question.getId());

                        ReadingAnswerResultVO answer_vo = new ReadingAnswerResultVO();
                        answer_vo.setQuestionId(question.getId());
                        answer_vo.setQuestionText(question.getQuestionText());
                        answer_vo.setQuestionType(question.getQuestionType());
                        answer_vo.setAnswerMode(question.getAnswerMode());
                        answer_vo.setOptionsJson(question.getOptionsJson());
                        answer_vo.setCorrectAnswer(build_display_correct_answer(question));

                        if (matched != null) {
                            answer_vo.setUserAnswer(matched.getUserAnswer());
                            answer_vo.setIsCorrect(matched.getIsCorrect());
                            answer_vo.setScore(matched.getScore());
                        } else {
                            answer_vo.setUserAnswer(null);
                            answer_vo.setIsCorrect(0);
                            answer_vo.setScore(0);
                        }
                        answer_vo_list.add(answer_vo);
                    }
                }

                question_vo_list.sort(Comparator
                        .comparing(ReadingQuestionVO::getDisplayOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(ReadingQuestionVO::getQuestionNumber, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(ReadingQuestionVO::getId, Comparator.nullsLast(Long::compareTo)));

                passage_vo.setQuestions(question_vo_list);
                passage_vo_list.add(passage_vo);
            }
        }

        passage_vo_list.sort(Comparator
                .comparing(ReadingPassageVO::getPassageNo, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(ReadingPassageVO::getDisplayOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(ReadingPassageVO::getId, Comparator.nullsLast(Long::compareTo)));

        ReadingRecordDetailVO detail_vo = new ReadingRecordDetailVO();
        detail_vo.setRecordId(record.getId());
        detail_vo.setTestId(record.getTestId());
        detail_vo.setTestTitle(test.getTitle());
        detail_vo.setTotalScore(record.getTotalScore());
        detail_vo.setCreatedTime(record.getCreatedTime());
        detail_vo.setPassages(passage_vo_list);
        detail_vo.setAnswers(answer_vo_list);
        return detail_vo;
    }

    @Override
    @Transactional
    public void deleteRecord(Long record_id, Long user_id) {
        ReadingRecord record = reading_record_mapper.findAnyByIdForUser(record_id, user_id);
        if (record == null) {
            throw new RuntimeException("Reading record not found");
        }
        reading_record_mapper.softDeleteByIdForUser(record_id, user_id);
    }

    @Override
    @Transactional
    public void restoreRecord(Long record_id, Long user_id) {
        ReadingRecord record = reading_record_mapper.findAnyByIdForUser(record_id, user_id);
        if (record == null) {
            throw new RuntimeException("Reading record not found");
        }
        reading_record_mapper.restoreByIdForUser(record_id, user_id);
    }

    private List<ReadingPassageVO> build_passage_vo_list(List<ReadingPassage> passages, boolean active_only) {
        List<ReadingPassageVO> passage_vo_list = new ArrayList<>();
        if (passages == null) {
            return passage_vo_list;
        }

        for (ReadingPassage passage : passages) {
            if (passage == null) {
                continue;
            }

            ReadingPassageVO passage_vo = new ReadingPassageVO();
            passage_vo.setId(passage.getId());
            passage_vo.setPartGroupId(passage.getPartGroupId());
            passage_vo.setPassageNo(passage.getPassageNo());
            passage_vo.setTitle(passage.getTitle());
            passage_vo.setContent(passage.getContent());
            passage_vo.setMaterialType(passage.getMaterialType());
            passage_vo.setDisplayOrder(passage.getDisplayOrder());

            List<ReadingQuestion> question_list = active_only
                    ? reading_question_mapper.findActiveByPassageId(passage.getId())
                    : reading_question_mapper.findAnyByPassageId(passage.getId());

            List<ReadingQuestionVO> question_vo_list = new ArrayList<>();
            if (question_list != null) {
                for (ReadingQuestion question : question_list) {
                    if (question == null) {
                        continue;
                    }
                    question_vo_list.add(to_question_vo(question));
                }
            }

            question_vo_list.sort(Comparator
                    .comparing(ReadingQuestionVO::getDisplayOrder, Comparator.nullsLast(Integer::compareTo))
                    .thenComparing(ReadingQuestionVO::getQuestionNumber, Comparator.nullsLast(Integer::compareTo))
                    .thenComparing(ReadingQuestionVO::getId, Comparator.nullsLast(Long::compareTo)));

            passage_vo.setQuestions(question_vo_list);
            passage_vo_list.add(passage_vo);
        }

        passage_vo_list.sort(Comparator
                .comparing(ReadingPassageVO::getPassageNo, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(ReadingPassageVO::getDisplayOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(ReadingPassageVO::getId, Comparator.nullsLast(Long::compareTo)));

        return passage_vo_list;
    }

    private Map<Long, ReadingAnswerDTO> build_answer_input_map(List<ReadingAnswerDTO> answer_list) {
        Map<Long, ReadingAnswerDTO> answer_map = new LinkedHashMap<>();
        if (answer_list == null) {
            return answer_map;
        }

        for (ReadingAnswerDTO answer_dto : answer_list) {
            if (answer_dto == null || answer_dto.getQuestionId() == null) {
                continue;
            }
            answer_map.put(answer_dto.getQuestionId(), answer_dto);
        }
        return answer_map;
    }

    private List<String> normalize_raw_list(String answer, List<String> answers) {
        List<String> result = new ArrayList<>();

        if (answers != null) {
            for (String item : answers) {
                String normalized_item = trim_to_null(item);
                if (normalized_item != null) {
                    result.add(normalized_item);
                }
            }
        }

        String single_answer = trim_to_null(answer);
        if (single_answer != null && result.isEmpty()) {
            result.add(single_answer);
        } else if (single_answer != null && !result.contains(single_answer)) {
            result.add(single_answer);
        }

        return result;
    }

    private ReadingAnswerRecord find_matched_answer(List<ReadingAnswerRecord> answer_records, Long question_id) {
        if (answer_records == null || answer_records.isEmpty() || question_id == null) {
            return null;
        }

        for (ReadingAnswerRecord answer_record : answer_records) {
            if (answer_record != null && Objects.equals(answer_record.getQuestionId(), question_id)) {
                return answer_record;
            }
        }
        return null;
    }

    private ReadingQuestionVO to_question_vo(ReadingQuestion question) {
        ReadingQuestionVO vo = new ReadingQuestionVO();
        vo.setId(question.getId());
        vo.setPassageId(question.getPassageId());
        vo.setPartGroupId(question.getPartGroupId());
        vo.setQuestionNumber(question.getQuestionNumber());
        vo.setQuestionType(question.getQuestionType());
        vo.setAnswerMode(question.getAnswerMode());
        vo.setQuestionText(question.getQuestionText());
        vo.setCorrectAnswer(question.getCorrectAnswer());
        vo.setOptionsJson(question.getOptionsJson());
        vo.setAcceptedAnswersJson(question.getAcceptedAnswersJson());
        vo.setGroupLabel(question.getGroupLabel());
        vo.setCaseInsensitive(question.getCaseInsensitive());
        vo.setIgnoreWhitespace(question.getIgnoreWhitespace());
        vo.setIgnorePunctuation(question.getIgnorePunctuation());
        vo.setDisplayOrder(question.getDisplayOrder());
        vo.setScore(question.getScore());
        vo.setAnswerRules(
                question.getId() == null
                        ? new ArrayList<>()
                        : reading_question_answer_rule_mapper.findByQuestionId(question.getId())
        );
        return vo;
    }

    private ReadingRecordVO to_record_vo(ReadingRecord record) {
        ReadingRecordVO vo = new ReadingRecordVO();
        vo.setId(record.getId());
        vo.setUserId(record.getUserId());
        vo.setTestId(record.getTestId());
        vo.setTotalScore(record.getTotalScore());
        vo.setCreatedTime(record.getCreatedTime());
        vo.setIsDeleted(record.getIsDeleted());

        ReadingTest test = reading_test_mapper.findAnyById(record.getTestId());
        vo.setTestTitle(test != null ? test.getTitle() : null);
        return vo;
    }

    private ReadingRecord get_reading_session_record(String session_id, Long user_id) {
        String normalized_session_id = trim_to_null(session_id);
        if (normalized_session_id == null) {
            throw new RuntimeException("sessionId is required");
        }

        ReadingRecord record = reading_record_mapper.findBySessionIdForUser(normalized_session_id, user_id);
        if (record == null) {
            throw new RuntimeException("Reading session not found");
        }
        if (record.getIsDeleted() != null && record.getIsDeleted() == 1) {
            throw new RuntimeException("Reading session is deleted");
        }
        return record;
    }

    private ReadingSessionVO to_session_vo(ReadingRecord record, TestTimerConfig timer_config) {
        ReadingSessionVO vo = new ReadingSessionVO();
        vo.setRecordId(record.getId());
        vo.setTestId(record.getTestId());
        vo.setSessionId(record.getSessionId());
        vo.setRecordStatus(record.getRecordStatus());
        vo.setStartedTime(record.getStartedTime());
        vo.setSubmittedTime(record.getSubmittedTime());
        vo.setTimeLimitSeconds(record.getTimeLimitSeconds());

        int time_spent_seconds = calculate_current_time_spent(record, null);
        vo.setTimeSpentSeconds(time_spent_seconds);
        vo.setRemainingSeconds(calculate_remaining_seconds(record.getTimeLimitSeconds(), time_spent_seconds));
        vo.setAllowPause(timer_config == null ? 0 : timer_config.getAllowPause());
        vo.setAutoSubmit(timer_config == null ? 1 : timer_config.getAutoSubmit());
        return vo;
    }

    private int calculate_current_time_spent(ReadingRecord record, Integer client_time_spent_seconds) {
        if (record == null) {
            return client_time_spent_seconds != null && client_time_spent_seconds >= 0 ? client_time_spent_seconds : 0;
        }

        int stored = record.getTimeSpentSeconds() == null ? 0 : record.getTimeSpentSeconds();
        String status = record.getRecordStatus();

        if (is_status_paused(status) || is_status_submitted(status) || is_status_auto_submitted(status)) {
            return merge_client_time_spent(stored, client_time_spent_seconds);
        }

        if (record.getStartedTime() == null) {
            return merge_client_time_spent(stored, client_time_spent_seconds);
        }

        long elapsed = Duration.between(record.getStartedTime(), LocalDateTime.now()).getSeconds();
        int server_spent = (int) Math.max(elapsed, 0);
        return Math.max(server_spent, merge_client_time_spent(stored, client_time_spent_seconds));
    }

    private int merge_client_time_spent(int stored, Integer client_time_spent_seconds) {
        if (client_time_spent_seconds == null || client_time_spent_seconds < 0) {
            return stored;
        }
        return Math.max(stored, client_time_spent_seconds);
    }

    private Integer calculate_remaining_seconds(Integer limit_seconds, Integer spent_seconds) {
        if (limit_seconds == null || limit_seconds <= 0) {
            return null;
        }
        int safe_spent_seconds = spent_seconds == null ? 0 : spent_seconds;
        return Math.max(limit_seconds - safe_spent_seconds, 0);
    }

    private Integer resolve_reading_time_limit_seconds(Long test_id, TestTimerConfig timer_config) {
        if (timer_config == null) {
            return null;
        }

        String timer_mode = normalize_token(timer_config.getTimerMode());
        if (timer_mode == null || TIMER_MODE_NONE.equals(timer_mode)) {
            return null;
        }

        if (TIMER_MODE_TEST_LEVEL.equals(timer_mode) || "TESTLEVEL".equals(timer_mode)) {
            return timer_config.getTotalSeconds();
        }

        if (TIMER_MODE_PART_LEVEL.equals(timer_mode) || "PARTLEVEL".equals(timer_mode)) {
            List<TestPartGroup> part_groups = reading_part_group_service.listActiveByTestId(test_id);
            if (part_groups == null || part_groups.isEmpty()) {
                return null;
            }

            int total_seconds = part_groups.stream()
                    .filter(Objects::nonNull)
                    .map(TestPartGroup::getTimeLimitSeconds)
                    .filter(Objects::nonNull)
                    .filter(value -> value > 0)
                    .reduce(0, Integer::sum);

            return total_seconds > 0 ? total_seconds : null;
        }

        return null;
    }

    private Integer resolve_submitted_time_spent_seconds(Integer provided_time_spent,
                                                         LocalDateTime started_time,
                                                         LocalDateTime now) {
        if (provided_time_spent != null && provided_time_spent >= 0) {
            return provided_time_spent;
        }
        if (started_time != null) {
            long seconds = Duration.between(started_time, now).getSeconds();
            return (int) Math.max(seconds, 0);
        }
        return 0;
    }

    private LocalDateTime resolve_started_time(LocalDateTime started_time,
                                               LocalDateTime now,
                                               Integer time_spent_seconds) {
        if (started_time != null) {
            return started_time;
        }
        int safe_seconds = time_spent_seconds == null || time_spent_seconds < 0 ? 0 : time_spent_seconds;
        return now.minusSeconds(safe_seconds);
    }

    private boolean is_timeout(Integer time_limit_seconds, Integer time_spent_seconds) {
        return time_limit_seconds != null
                && time_limit_seconds > 0
                && time_spent_seconds != null
                && time_spent_seconds >= time_limit_seconds;
    }

    private boolean is_auto_submit_enabled(TestTimerConfig timer_config) {
        return timer_config != null && enabled(timer_config.getAutoSubmit());
    }

    private boolean enabled(Integer value) {
        return value != null && value == 1;
    }

    private boolean is_status_in_progress(String status) {
        String normalized_status = normalize_token(status);
        return STATUS_IN_PROGRESS.equals(normalized_status) || "INPROGRESS".equals(normalized_status);
    }

    private boolean is_status_paused(String status) {
        String normalized_status = normalize_token(status);
        return STATUS_PAUSED.equals(normalized_status);
    }

    private boolean is_status_submitted(String status) {
        String normalized_status = normalize_token(status);
        return STATUS_SUBMITTED.equals(normalized_status);
    }

    private boolean is_status_auto_submitted(String status) {
        String normalized_status = normalize_token(status);
        return STATUS_AUTO_SUBMITTED.equals(normalized_status) || "AUTOSUBMITTED".equals(normalized_status);
    }

    private String generate_session_id() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String build_display_correct_answer(ReadingQuestion question) {
        if (question == null) {
            return null;
        }

        List<QuestionAnswerRule> rules = question.getId() == null
                ? Collections.emptyList()
                : reading_question_answer_rule_mapper.findByQuestionId(question.getId());

        if (rules != null && !rules.isEmpty()) {
            List<String> values = rules.stream()
                    .filter(Objects::nonNull)
                    .sorted(Comparator
                            .comparing(QuestionAnswerRule::getBlankNo, Comparator.nullsLast(Integer::compareTo))
                            .thenComparing(QuestionAnswerRule::getAnswerGroupNo, Comparator.nullsLast(Integer::compareTo))
                            .thenComparing(QuestionAnswerRule::getDisplayOrder, Comparator.nullsLast(Integer::compareTo))
                            .thenComparing(QuestionAnswerRule::getId, Comparator.nullsLast(Long::compareTo)))
                    .map(QuestionAnswerRule::getAnswerText)
                    .map(this::trim_to_null)
                    .filter(Objects::nonNull)
                    .toList();

            if (!values.isEmpty()) {
                return String.join(" / ", new LinkedHashSet<>(values));
            }
        }

        List<String> accepted_answers = parse_json_string_list(question.getAcceptedAnswersJson());
        if (!accepted_answers.isEmpty()) {
            return String.join(" / ", accepted_answers);
        }

        return trim_to_null(question.getCorrectAnswer());
    }

    private List<String> parse_json_string_list(String json_value) {
        String safe_json_value = trim_to_null(json_value);
        if (safe_json_value == null) {
            return Collections.emptyList();
        }

        try {
            JsonNode root = object_mapper.readTree(safe_json_value);
            List<String> result = new ArrayList<>();

            if (root.isArray()) {
                for (JsonNode item : root) {
                    String value = trim_to_null(item == null ? null : item.asText());
                    if (value != null) {
                        result.add(value);
                    }
                }
                return result;
            }

            if (root.isTextual()) {
                String value = trim_to_null(root.asText());
                return value == null ? Collections.emptyList() : List.of(value);
            }
        } catch (Exception ignored) {
        }

        return List.of(safe_json_value);
    }

    private int normalize_page_num(Integer page_num) {
        return page_num == null || page_num < 1 ? 1 : page_num;
    }

    private int normalize_page_size(Integer page_size) {
        if (page_size == null || page_size < 1) {
            return 10;
        }
        return Math.min(page_size, 100);
    }

    private String normalize_token(String value) {
        String normalized_value = trim_to_null(value);
        if (normalized_value == null) {
            return null;
        }
        return normalized_value
                .replace('-', '_')
                .replace(' ', '_')
                .toUpperCase();
    }

    private String trim_to_null(String value) {
        if (value == null) {
            return null;
        }
        String trimmed_value = value.trim();
        return trimmed_value.isEmpty() ? null : trimmed_value;
    }
}
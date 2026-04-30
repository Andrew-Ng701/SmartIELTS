package com.andrew.smartielts.reading.service.admin.impl;

import com.andrew.smartielts.common.constants.RecordQueryValidator;
import com.andrew.smartielts.common.domain.dto.BizImageResourceDTO;
import com.andrew.smartielts.common.domain.pojo.BizImageResource;
import com.andrew.smartielts.common.domain.pojo.QuestionAnswerRule;
import com.andrew.smartielts.common.domain.pojo.TestPartGroup;
import com.andrew.smartielts.common.domain.pojo.TestTimerConfig;
import com.andrew.smartielts.common.page.PageResult;
import com.andrew.smartielts.common.service.BizImageResourceService;
import com.andrew.smartielts.common.storage.BucketType;
import com.andrew.smartielts.reading.domain.dto.ReadingPassageDTO;
import com.andrew.smartielts.reading.domain.dto.ReadingQuestionDTO;
import com.andrew.smartielts.reading.domain.dto.ReadingTestDTO;
import com.andrew.smartielts.reading.domain.pojo.ReadingAnswerRecord;
import com.andrew.smartielts.reading.domain.pojo.ReadingPassage;
import com.andrew.smartielts.reading.domain.pojo.ReadingQuestion;
import com.andrew.smartielts.reading.domain.pojo.ReadingRecord;
import com.andrew.smartielts.reading.domain.pojo.ReadingTest;
import com.andrew.smartielts.reading.domain.query.admin.AdminReadingDeletedRecordPageQuery;
import com.andrew.smartielts.reading.domain.query.admin.AdminReadingRecordPageQuery;
import com.andrew.smartielts.reading.domain.vo.ReadingAnswerResultVO;
import com.andrew.smartielts.reading.domain.vo.ReadingPassageVO;
import com.andrew.smartielts.reading.domain.vo.ReadingQuestionVO;
import com.andrew.smartielts.reading.domain.vo.ReadingRecordDetailVO;
import com.andrew.smartielts.reading.domain.vo.ReadingRecordVO;
import com.andrew.smartielts.reading.domain.vo.ReadingTestDetailVO;
import com.andrew.smartielts.reading.mapper.ReadingAnswerRecordMapper;
import com.andrew.smartielts.reading.mapper.ReadingPassageMapper;
import com.andrew.smartielts.reading.mapper.ReadingQuestionMapper;
import com.andrew.smartielts.reading.mapper.ReadingRecordMapper;
import com.andrew.smartielts.reading.mapper.ReadingTestMapper;
import com.andrew.smartielts.reading.service.admin.AdminReadingService;
import com.andrew.smartielts.reading.service.admin.ReadingPartGroupService;
import com.andrew.smartielts.reading.service.admin.ReadingQuestionAnswerRuleService;
import com.andrew.smartielts.reading.service.admin.ReadingTestTimerService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.andrew.smartielts.common.constants.StorageBizConstants.*;
import static com.andrew.smartielts.reading.constant.ReadingQuestionConstants.normalize_question_type;
import static com.andrew.smartielts.reading.constant.ReadingQuestionConstants.resolve_answer_mode_by_question_type;

@Service
public class AdminReadingServiceImpl implements AdminReadingService {

    private final ReadingTestMapper reading_test_mapper;
    private final ReadingPassageMapper reading_passage_mapper;
    private final ReadingQuestionMapper reading_question_mapper;
    private final ReadingRecordMapper reading_record_mapper;
    private final ReadingAnswerRecordMapper reading_answer_record_mapper;
    private final ReadingTestTimerService reading_test_timer_service;
    private final ReadingPartGroupService reading_part_group_service;
    private final ReadingQuestionAnswerRuleService reading_question_answer_rule_service;
    private final BizImageResourceService biz_image_resource_service;
    private final ObjectMapper object_mapper = new ObjectMapper();

    public AdminReadingServiceImpl(ReadingTestMapper reading_test_mapper,
                                   ReadingPassageMapper reading_passage_mapper,
                                   ReadingQuestionMapper reading_question_mapper,
                                   ReadingRecordMapper reading_record_mapper,
                                   ReadingAnswerRecordMapper reading_answer_record_mapper,
                                   ReadingTestTimerService reading_test_timer_service,
                                   ReadingPartGroupService reading_part_group_service,
                                   ReadingQuestionAnswerRuleService reading_question_answer_rule_service,
                                   BizImageResourceService biz_image_resource_service) {
        this.reading_test_mapper = reading_test_mapper;
        this.reading_passage_mapper = reading_passage_mapper;
        this.reading_question_mapper = reading_question_mapper;
        this.reading_record_mapper = reading_record_mapper;
        this.reading_answer_record_mapper = reading_answer_record_mapper;
        this.reading_test_timer_service = reading_test_timer_service;
        this.reading_part_group_service = reading_part_group_service;
        this.reading_question_answer_rule_service = reading_question_answer_rule_service;
        this.biz_image_resource_service = biz_image_resource_service;
    }

    @Override
    @Transactional
    public ReadingTest createTest(ReadingTestDTO dto) {
        if (dto == null) {
            throw new RuntimeException("Request body is required");
        }

        ReadingTest test = new ReadingTest();
        test.setTitle(trim_to_null(dto.getTitle()));
        test.setTotalScore(dto.getTotalScore());
        test.setCreatedTime(LocalDateTime.now());
        test.setIsDeleted(0);
        reading_test_mapper.insertReadingTest(test);

        TestTimerConfig timer_config = dto.getTimerConfig() == null ? new TestTimerConfig() : dto.getTimerConfig();
        timer_config.setTestId(test.getId());
        if (trim_to_null(timer_config.getTimerMode()) == null) {
            timer_config.setTimerMode("NONE");
        }
        if (timer_config.getAutoSubmit() == null) {
            timer_config.setAutoSubmit(1);
        }
        if (timer_config.getAllowPause() == null) {
            timer_config.setAllowPause(0);
        }
        reading_test_timer_service.saveOrUpdateTestTimerConfig(timer_config);

        sync_reading_part_groups(test.getId(), dto.getPartGroups());

        test.setTimerConfig(reading_test_timer_service.getByTestId(test.getId()));
        test.setPartGroups(reading_part_group_service.listAnyByTestId(test.getId()));
        attach_group_images(test.getPartGroups());
        return test;
    }

    @Override
    public List<ReadingTest> listTests() {
        return reading_test_mapper.findAllActive();
    }

    @Override
    public ReadingTestDetailVO getTestDetail(Long test_id) {
        ReadingTest test = reading_test_mapper.findAnyById(test_id);
        if (test == null) {
            throw new RuntimeException("Reading test not found");
        }

        List<TestPartGroup> part_groups = reading_part_group_service.listAnyByTestId(test_id);
        attach_group_images(part_groups);

        List<ReadingPassage> passages = reading_passage_mapper.findAnyByTestId(test_id);
        List<ReadingPassageVO> passage_vo_list = build_passage_vo_list(passages, false);

        ReadingTestDetailVO detail_vo = new ReadingTestDetailVO();
        detail_vo.setId(test.getId());
        detail_vo.setTitle(test.getTitle());
        detail_vo.setTotalScore(test.getTotalScore());
        detail_vo.setTimerConfig(reading_test_timer_service.getByTestId(test_id));
        detail_vo.setPartGroups(part_groups);
        detail_vo.setPassages(passage_vo_list);
        return detail_vo;
    }

    @Override
    @Transactional
    public ReadingTest updateTest(Long id, ReadingTestDTO dto) {
        ReadingTest test = reading_test_mapper.findActiveById(id);
        if (test == null) {
            throw new RuntimeException("Reading test not found");
        }
        if (dto == null) {
            throw new RuntimeException("Request body is required");
        }

        test.setTitle(trim_to_null(dto.getTitle()));
        test.setTotalScore(dto.getTotalScore());
        reading_test_mapper.updateReadingTest(test);

        if (dto.getTimerConfig() != null) {
            TestTimerConfig timer_config = dto.getTimerConfig();
            timer_config.setTestId(id);
            if (trim_to_null(timer_config.getTimerMode()) == null) {
                timer_config.setTimerMode("NONE");
            }
            if (timer_config.getAutoSubmit() == null) {
                timer_config.setAutoSubmit(1);
            }
            if (timer_config.getAllowPause() == null) {
                timer_config.setAllowPause(0);
            }
            reading_test_timer_service.saveOrUpdateTestTimerConfig(timer_config);
        }

        sync_reading_part_groups(id, dto.getPartGroups());

        test.setTimerConfig(reading_test_timer_service.getByTestId(id));
        test.setPartGroups(reading_part_group_service.listAnyByTestId(id));
        attach_group_images(test.getPartGroups());
        return test;
    }

    @Override
    @Transactional
    public void deleteTest(Long id) {
        ReadingTest test = reading_test_mapper.findActiveById(id);
        if (test == null) {
            throw new RuntimeException("Reading test not found");
        }

        List<ReadingPassage> passages = reading_passage_mapper.findAnyByTestId(id);
        if (passages != null) {
            for (ReadingPassage passage : passages) {
                if (passage == null || passage.getId() == null) {
                    continue;
                }
                reading_question_mapper.softDeleteByPassageId(passage.getId());
            }
        }

        reading_passage_mapper.softDeleteByTestId(id);
        reading_part_group_service.deleteByTestId(id);
        reading_test_timer_service.deleteByTestId(id);
        reading_test_mapper.softDeleteById(id);
    }

    @Override
    @Transactional
    public void restoreTest(Long id) {
        ReadingTest test = reading_test_mapper.findAnyById(id);
        if (test == null) {
            throw new RuntimeException("Reading test not found");
        }

        reading_test_mapper.restoreById(id);
        reading_part_group_service.restoreByTestId(id);
        reading_passage_mapper.restoreByTestId(id);

        List<ReadingPassage> passages = reading_passage_mapper.findAnyByTestId(id);
        if (passages != null) {
            for (ReadingPassage passage : passages) {
                if (passage == null || passage.getId() == null) {
                    continue;
                }
                reading_question_mapper.restoreByPassageId(passage.getId());
            }
        }
    }

    @Override
    @Transactional
    public void createPassage(Long test_id, ReadingPassageDTO dto) {
        ReadingTest test = reading_test_mapper.findActiveById(test_id);
        if (test == null) {
            throw new RuntimeException("Reading test not found");
        }
        if (dto == null) {
            throw new RuntimeException("Request body is required");
        }

        validate_reading_part_group(test_id, dto.getPartGroupId());

        ReadingPassage passage = new ReadingPassage();
        passage.setTestId(test_id);
        passage.setPartGroupId(dto.getPartGroupId());
        passage.setPassageNo(dto.getPassageNo());
        passage.setTitle(trim_to_null(dto.getTitle()));
        passage.setContent(trim_to_null(dto.getContent()));
        passage.setMaterialType(trim_to_null(dto.getMaterialType()));
        passage.setDisplayOrder(dto.getDisplayOrder() == null ? 0 : dto.getDisplayOrder());
        passage.setIsDeleted(0);
        reading_passage_mapper.insertReadingPassage(passage);
    }

    @Override
    @Transactional
    public void updatePassage(Long passage_id, ReadingPassageDTO dto) {
        ReadingPassage passage = reading_passage_mapper.findActiveById(passage_id);
        if (passage == null) {
            throw new RuntimeException("Reading passage not found");
        }
        if (dto == null) {
            throw new RuntimeException("Request body is required");
        }

        validate_reading_part_group(passage.getTestId(), dto.getPartGroupId());

        passage.setPartGroupId(dto.getPartGroupId());
        passage.setPassageNo(dto.getPassageNo());
        passage.setTitle(trim_to_null(dto.getTitle()));
        passage.setContent(trim_to_null(dto.getContent()));
        passage.setMaterialType(trim_to_null(dto.getMaterialType()));
        passage.setDisplayOrder(dto.getDisplayOrder() == null ? 0 : dto.getDisplayOrder());
        reading_passage_mapper.updateReadingPassage(passage);
    }

    @Override
    @Transactional
    public void deletePassage(Long passage_id) {
        ReadingPassage passage = reading_passage_mapper.findActiveById(passage_id);
        if (passage == null) {
            throw new RuntimeException("Reading passage not found");
        }

        reading_question_mapper.softDeleteByPassageId(passage_id);
        reading_passage_mapper.softDeleteById(passage_id);
    }

    @Override
    @Transactional
    public void restorePassage(Long passage_id) {
        ReadingPassage passage = reading_passage_mapper.findAnyById(passage_id);
        if (passage == null) {
            throw new RuntimeException("Reading passage not found");
        }

        reading_passage_mapper.restoreById(passage_id);
        reading_question_mapper.restoreByPassageId(passage_id);
    }

    @Override
    @Transactional
    public void createQuestion(Long passage_id, ReadingQuestionDTO dto) {
        ReadingPassage passage = reading_passage_mapper.findActiveById(passage_id);
        if (passage == null) {
            throw new RuntimeException("Reading passage not found");
        }
        if (dto == null) {
            throw new RuntimeException("Request body is required");
        }

        Long part_group_id = dto.getPartGroupId() != null ? dto.getPartGroupId() : passage.getPartGroupId();
        validate_reading_part_group(passage.getTestId(), part_group_id);

        String question_type = normalize_question_type(dto.getQuestionType());
        String answer_mode = resolve_answer_mode_by_question_type(question_type, dto.getAnswerMode());

        ReadingQuestion question = new ReadingQuestion();
        question.setPassageId(passage_id);
        question.setPartGroupId(part_group_id);
        question.setQuestionNumber(dto.getQuestionNumber());
        question.setQuestionType(question_type);
        question.setAnswerMode(answer_mode);
        question.setQuestionText(trim_to_null(dto.getQuestionText()));
        question.setCorrectAnswer(trim_to_null(dto.getCorrectAnswer()));
        question.setOptionsJson(trim_to_null(dto.getOptionsJson()));
        question.setAcceptedAnswersJson(trim_to_null(dto.getAcceptedAnswersJson()));
        question.setGroupLabel(trim_to_null(dto.getGroupLabel()));
        question.setCaseInsensitive(default_flag(dto.getCaseInsensitive(), 1));
        question.setIgnoreWhitespace(default_flag(dto.getIgnoreWhitespace(), 1));
        question.setIgnorePunctuation(default_flag(dto.getIgnorePunctuation(), 1));
        question.setDisplayOrder(dto.getDisplayOrder() == null ? 0 : dto.getDisplayOrder());
        question.setScore(dto.getScore() == null ? 1 : dto.getScore());
        question.setIsDeleted(0);
        reading_question_mapper.insertReadingQuestion(question);

        if (dto.getAnswerRules() != null) {
            reading_question_answer_rule_service.replaceByQuestionId(question.getId(), dto.getAnswerRules());
        }
        if (part_group_id != null && has_group_images(dto.getGroupImages())) {
            replace_reading_part_group_images(part_group_id, dto.getGroupImages());
        }
    }

    @Override
    @Transactional
    public void updateQuestion(Long question_id, ReadingQuestionDTO dto) {
        ReadingQuestion question = reading_question_mapper.findActiveById(question_id);
        if (question == null) {
            throw new RuntimeException("Reading question not found");
        }
        if (dto == null) {
            throw new RuntimeException("Request body is required");
        }

        ReadingPassage passage = reading_passage_mapper.findActiveById(question.getPassageId());
        if (passage == null) {
            throw new RuntimeException("Reading passage not found");
        }

        Long part_group_id = dto.getPartGroupId() != null ? dto.getPartGroupId() : passage.getPartGroupId();
        validate_reading_part_group(passage.getTestId(), part_group_id);

        String question_type = normalize_question_type(dto.getQuestionType());
        String answer_mode = resolve_answer_mode_by_question_type(question_type, dto.getAnswerMode());

        question.setPartGroupId(part_group_id);
        question.setQuestionNumber(dto.getQuestionNumber());
        question.setQuestionType(question_type);
        question.setAnswerMode(answer_mode);
        question.setQuestionText(trim_to_null(dto.getQuestionText()));
        question.setCorrectAnswer(trim_to_null(dto.getCorrectAnswer()));
        question.setOptionsJson(trim_to_null(dto.getOptionsJson()));
        question.setAcceptedAnswersJson(trim_to_null(dto.getAcceptedAnswersJson()));
        question.setGroupLabel(trim_to_null(dto.getGroupLabel()));
        question.setCaseInsensitive(default_flag(dto.getCaseInsensitive(), 1));
        question.setIgnoreWhitespace(default_flag(dto.getIgnoreWhitespace(), 1));
        question.setIgnorePunctuation(default_flag(dto.getIgnorePunctuation(), 1));
        question.setDisplayOrder(dto.getDisplayOrder() == null ? 0 : dto.getDisplayOrder());
        question.setScore(dto.getScore() == null ? 1 : dto.getScore());
        reading_question_mapper.updateReadingQuestion(question);

        if (dto.getAnswerRules() != null) {
            reading_question_answer_rule_service.replaceByQuestionId(question_id, dto.getAnswerRules());
        }
        if (part_group_id != null && dto.getGroupImages() != null) {
            replace_reading_part_group_images(part_group_id, dto.getGroupImages());
        }
    }

    @Override
    @Transactional
    public void deleteQuestion(Long question_id) {
        ReadingQuestion question = reading_question_mapper.findActiveById(question_id);
        if (question == null) {
            throw new RuntimeException("Reading question not found");
        }
        reading_question_mapper.softDeleteById(question_id);
    }

    @Override
    @Transactional
    public void restoreQuestion(Long question_id) {
        ReadingQuestion question = reading_question_mapper.findAnyById(question_id);
        if (question == null) {
            throw new RuntimeException("Reading question not found");
        }
        reading_question_mapper.restoreById(question_id);
    }

    @Override
    public PageResult<ReadingRecordVO> pageActiveRecords(AdminReadingRecordPageQuery query) {
        AdminReadingRecordPageQuery safe_query = query == null ? new AdminReadingRecordPageQuery() : query;

        RecordQueryValidator.validate(
                safe_query.getPageNum(),
                safe_query.getPageSize(),
                safe_query.getUserId(),
                safe_query.getTestId(),
                safe_query.getMinScore(),
                safe_query.getMaxScore(),
                safe_query.getStartTime(),
                safe_query.getEndTime()
        );

        int page_num = normalize_page_num(safe_query.getPageNum());
        int page_size = normalize_page_size(safe_query.getPageSize());
        int offset = (page_num - 1) * page_size;

        Long total = reading_record_mapper.countAdminActive(safe_query);
        if (total == null || total <= 0L) {
            return new PageResult<>(new ArrayList<>(), 0L, page_num, page_size);
        }

        List<ReadingRecord> records = reading_record_mapper.pageAdminActive(safe_query, offset, page_size);
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
    public PageResult<ReadingRecordVO> pageDeletedRecords(AdminReadingDeletedRecordPageQuery query) {
        AdminReadingDeletedRecordPageQuery safe_query = query == null ? new AdminReadingDeletedRecordPageQuery() : query;

        int page_num = normalize_page_num(safe_query.getPageNum());
        int page_size = normalize_page_size(safe_query.getPageSize());
        int offset = (page_num - 1) * page_size;

        Long total = reading_record_mapper.countAdminDeleted(safe_query);
        if (total == null || total <= 0L) {
            return new PageResult<>(new ArrayList<>(), 0L, page_num, page_size);
        }

        List<ReadingRecord> records = reading_record_mapper.pageAdminDeleted(safe_query, offset, page_size);
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
    public ReadingRecordDetailVO getRecord(Long record_id) {
        ReadingRecord record = reading_record_mapper.findAnyById(record_id);
        if (record == null) {
            throw new RuntimeException("Reading record not found");
        }

        ReadingTest test = reading_test_mapper.findAnyById(record.getTestId());
        if (test == null) {
            throw new RuntimeException("Reading test not found");
        }

        List<ReadingPassage> passages = reading_passage_mapper.findAnyByTestId(test.getId());
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

                List<ReadingQuestion> questions = reading_question_mapper.findAnyByPassageId(passage.getId());
                List<ReadingQuestionVO> question_vo_list = new ArrayList<>();

                if (questions != null) {
                    for (ReadingQuestion question : questions) {
                        if (question == null) {
                            continue;
                        }

                        question_vo_list.add(to_question_vo(question));

                        ReadingAnswerRecord matched = find_matched_answer(answer_records, question.getId());

                        ReadingAnswerResultVO answer_vo = new ReadingAnswerResultVO();
                        answer_vo.setQuestionId(question.getId());
                        answer_vo.setQuestionType(question.getQuestionType());
                        answer_vo.setAnswerMode(question.getAnswerMode());
                        answer_vo.setQuestionText(question.getQuestionText());
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
        detail_vo.setTestId(test.getId());
        detail_vo.setTestTitle(test.getTitle());
        detail_vo.setTotalScore(record.getTotalScore());
        detail_vo.setCreatedTime(record.getCreatedTime());
        detail_vo.setPassages(passage_vo_list);
        detail_vo.setAnswers(answer_vo_list);
        return detail_vo;
    }

    @Override
    @Transactional
    public void deleteRecord(Long record_id) {
        ReadingRecord record = reading_record_mapper.findAnyById(record_id);
        if (record == null) {
            throw new RuntimeException("Reading record not found");
        }
        if (record.getIsDeleted() != null && record.getIsDeleted() == 1) {
            throw new RuntimeException("Reading record already deleted");
        }
        reading_record_mapper.softDeleteById(record_id);
    }

    @Override
    @Transactional
    public void restoreRecord(Long record_id) {
        ReadingRecord record = reading_record_mapper.findAnyById(record_id);
        if (record == null) {
            throw new RuntimeException("Reading record not found");
        }
        if (record.getIsDeleted() == null || record.getIsDeleted() == 0) {
            throw new RuntimeException("Reading record is not deleted");
        }
        reading_record_mapper.restoreById(record_id);
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

            List<ReadingQuestion> questions = active_only
                    ? reading_question_mapper.findActiveByPassageId(passage.getId())
                    : reading_question_mapper.findAnyByPassageId(passage.getId());

            List<ReadingQuestionVO> question_vo_list = new ArrayList<>();
            if (questions != null) {
                for (ReadingQuestion question : questions) {
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

    private void validate_reading_part_group(Long test_id, Long part_group_id) {
        if (part_group_id == null) {
            return;
        }

        TestPartGroup part_group = reading_part_group_service.getActiveById(part_group_id);
        if (part_group == null) {
            throw new RuntimeException("Reading part group not found");
        }
        if (!Objects.equals(part_group.getTestId(), test_id)) {
            throw new RuntimeException("Reading part group does not belong to test");
        }
    }

    private void sync_reading_part_groups(Long test_id, List<TestPartGroup> incoming_part_groups) {
        List<TestPartGroup> existing_part_groups = reading_part_group_service.listAnyByTestId(test_id);
        Set<Long> incoming_ids = incoming_part_groups == null
                ? Collections.emptySet()
                : incoming_part_groups.stream()
                .filter(Objects::nonNull)
                .map(TestPartGroup::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (existing_part_groups != null) {
            for (TestPartGroup existing_part_group : existing_part_groups) {
                if (existing_part_group == null || existing_part_group.getId() == null) {
                    continue;
                }
                if (!incoming_ids.contains(existing_part_group.getId())) {
                    reading_part_group_service.deleteById(existing_part_group.getId());
                }
            }
        }

        if (incoming_part_groups == null) {
            return;
        }

        for (TestPartGroup incoming_part_group : incoming_part_groups) {
            if (incoming_part_group == null) {
                continue;
            }
            incoming_part_group.setTestId(test_id);

            if (incoming_part_group.getId() == null) {
                reading_part_group_service.createPartGroup(incoming_part_group);
            } else if (reading_part_group_service.getAnyById(incoming_part_group.getId()) == null) {
                reading_part_group_service.createPartGroup(incoming_part_group);
            } else {
                reading_part_group_service.updatePartGroup(incoming_part_group.getId(), incoming_part_group);
                reading_part_group_service.restoreById(incoming_part_group.getId());
            }
        }
    }

    private boolean has_group_images(List<BizImageResourceDTO> group_images) {
        return group_images != null && !group_images.isEmpty();
    }

    private void replace_reading_part_group_images(Long part_group_id, List<BizImageResourceDTO> group_images) {
        if (part_group_id == null) {
            throw new RuntimeException("partGroupId is required");
        }
        if (group_images == null) {
            return;
        }
        biz_image_resource_service.replaceByTarget(
                TARGET_TYPE_READING_PART_GROUP,
                part_group_id,
                BucketType.QUESTION_GROUP_IMAGE.getKey(),
                BIZ_PATH_QUESTION_GROUP_IMAGE,
                group_images
        );
    }

    private void attach_group_images(List<TestPartGroup> part_groups) {
        if (part_groups == null || part_groups.isEmpty()) {
            return;
        }

        List<Long> target_ids = part_groups.stream()
                .filter(Objects::nonNull)
                .map(TestPartGroup::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (target_ids.isEmpty()) {
            return;
        }

        Map<Long, List<BizImageResource>> image_map = biz_image_resource_service.listByTargets(
                TARGET_TYPE_READING_PART_GROUP,
                target_ids
        );
        if (image_map == null) {
            image_map = Collections.emptyMap();
        }

        for (TestPartGroup part_group : part_groups) {
            if (part_group == null || part_group.getId() == null) {
                continue;
            }
            List<BizImageResource> images = sort_images(image_map.get(part_group.getId()));
            try {
                part_group.getClass().getMethod("setImages", List.class).invoke(part_group, images);
            } catch (Exception ignored) {
            }
        }
    }

    private List<BizImageResource> sort_images(List<BizImageResource> images) {
        if (images == null || images.isEmpty()) {
            return new ArrayList<>();
        }
        return images.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator
                        .comparing(BizImageResource::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(BizImageResource::getId, Comparator.nullsLast(Long::compareTo)))
                .collect(Collectors.toList());
    }

    private String build_display_correct_answer(ReadingQuestion question) {
        if (question == null) {
            return null;
        }

        List<QuestionAnswerRule> rules = question.getId() == null
                ? Collections.emptyList()
                : reading_question_answer_rule_service.listByQuestionId(question.getId());

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
                    .collect(Collectors.toList());

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
        vo.setAnswerRules(question.getId() == null
                ? new ArrayList<>()
                : reading_question_answer_rule_service.listByQuestionId(question.getId()));
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

        String fallback_value = trim_to_null(safe_json_value);
        return fallback_value == null ? Collections.emptyList() : List.of(fallback_value);
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

    private Integer default_flag(Integer value, Integer default_value) {
        return value == null ? default_value : value;
    }

    private String trim_to_null(String value) {
        if (value == null) {
            return null;
        }
        String trimmed_value = value.trim();
        return trimmed_value.isEmpty() ? null : trimmed_value;
    }
}
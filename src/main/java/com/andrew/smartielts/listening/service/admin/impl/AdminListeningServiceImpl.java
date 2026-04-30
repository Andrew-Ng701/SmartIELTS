package com.andrew.smartielts.listening.service.admin.impl;

import com.andrew.smartielts.common.constants.RecordQueryValidator;
import com.andrew.smartielts.common.constants.StorageBizConstants;
import com.andrew.smartielts.common.domain.dto.BizImageResourceDTO;
import com.andrew.smartielts.common.domain.pojo.BizImageResource;
import com.andrew.smartielts.common.domain.pojo.QuestionAnswerRule;
import com.andrew.smartielts.common.domain.pojo.TestPartGroup;
import com.andrew.smartielts.common.domain.pojo.TestTimerConfig;
import com.andrew.smartielts.common.page.PageResult;
import com.andrew.smartielts.common.service.BizImageResourceService;
import com.andrew.smartielts.common.storage.BucketType;
import com.andrew.smartielts.common.storage.UploadResult;
import com.andrew.smartielts.common.storage.service.StorageService;
import com.andrew.smartielts.listening.ai.service.ListeningTranscriptService;
import com.andrew.smartielts.listening.constants.ListeningQuestionConstants;
import com.andrew.smartielts.listening.domain.dto.ListeningCreateTestForm;
import com.andrew.smartielts.listening.domain.dto.ListeningQuestionDTO;
import com.andrew.smartielts.listening.domain.dto.ListeningTestDTO;
import com.andrew.smartielts.listening.domain.pojo.ListeningAnswerRecord;
import com.andrew.smartielts.listening.domain.pojo.ListeningMaterial;
import com.andrew.smartielts.listening.domain.pojo.ListeningQuestion;
import com.andrew.smartielts.listening.domain.pojo.ListeningRecord;
import com.andrew.smartielts.listening.domain.pojo.ListeningTest;
import com.andrew.smartielts.listening.domain.query.admin.AdminListeningDeletedRecordPageQuery;
import com.andrew.smartielts.listening.domain.query.admin.AdminListeningRecordPageQuery;
import com.andrew.smartielts.listening.domain.vo.ListeningAnswerResultVO;
import com.andrew.smartielts.listening.domain.vo.ListeningQuestionVO;
import com.andrew.smartielts.listening.domain.vo.ListeningRecordDetailVO;
import com.andrew.smartielts.listening.domain.vo.ListeningRecordVO;
import com.andrew.smartielts.listening.domain.vo.ListeningTestDetailVO;
import com.andrew.smartielts.listening.mapper.ListeningAnswerRecordMapper;
import com.andrew.smartielts.listening.mapper.ListeningQuestionMapper;
import com.andrew.smartielts.listening.mapper.ListeningRecordMapper;
import com.andrew.smartielts.listening.mapper.ListeningTestMapper;
import com.andrew.smartielts.listening.service.admin.AdminListeningService;
import com.andrew.smartielts.listening.service.admin.ListeningMaterialService;
import com.andrew.smartielts.listening.service.admin.ListeningPartGroupService;
import com.andrew.smartielts.listening.service.admin.ListeningQuestionAnswerRuleService;
import com.andrew.smartielts.listening.service.admin.ListeningTestTimerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminListeningServiceImpl implements AdminListeningService {

    private static final String TIMER_MODE_NONE = "NONE";

    private static final BucketType BUCKET_TYPE_LISTENING_AUDIO = BucketType.LISTENING_RECORDING;
    private static final String BUCKET_TYPE_QUESTION_GROUP_IMAGE = BucketType.QUESTION_GROUP_IMAGE.getKey();

    private static final String BIZ_PATH_LISTENING_AUDIO = StorageBizConstants.BIZ_PATH_LISTENING_AUDIO;
    private static final String BIZ_PATH_QUESTION_GROUP_IMAGE = StorageBizConstants.BIZ_PATH_QUESTION_GROUP_IMAGE;
    private static final String TARGET_TYPE_LISTENING_PART_GROUP = StorageBizConstants.TARGET_TYPE_LISTENING_PART_GROUP;

    private final ListeningTestMapper listening_test_mapper;
    private final ListeningQuestionMapper listening_question_mapper;
    private final ListeningRecordMapper listening_record_mapper;
    private final ListeningAnswerRecordMapper listening_answer_record_mapper;
    private final StorageService storage_service;
    private final ListeningTranscriptService listening_transcript_service;
    private final ListeningTestTimerService listening_test_timer_service;
    private final ListeningPartGroupService listening_part_group_service;
    private final ListeningMaterialService listening_material_service;
    private final ListeningQuestionAnswerRuleService listening_question_answer_rule_service;
    private final BizImageResourceService biz_image_resource_service;

    public AdminListeningServiceImpl(ListeningTestMapper listening_test_mapper,
                                     ListeningQuestionMapper listening_question_mapper,
                                     ListeningRecordMapper listening_record_mapper,
                                     ListeningAnswerRecordMapper listening_answer_record_mapper,
                                     StorageService storage_service,
                                     ListeningTranscriptService listening_transcript_service,
                                     ListeningTestTimerService listening_test_timer_service,
                                     ListeningPartGroupService listening_part_group_service,
                                     ListeningMaterialService listening_material_service,
                                     ListeningQuestionAnswerRuleService listening_question_answer_rule_service,
                                     BizImageResourceService biz_image_resource_service) {
        this.listening_test_mapper = listening_test_mapper;
        this.listening_question_mapper = listening_question_mapper;
        this.listening_record_mapper = listening_record_mapper;
        this.listening_answer_record_mapper = listening_answer_record_mapper;
        this.storage_service = storage_service;
        this.listening_transcript_service = listening_transcript_service;
        this.listening_test_timer_service = listening_test_timer_service;
        this.listening_part_group_service = listening_part_group_service;
        this.listening_material_service = listening_material_service;
        this.listening_question_answer_rule_service = listening_question_answer_rule_service;
        this.biz_image_resource_service = biz_image_resource_service;
    }

    @Override
    @Transactional
    public ListeningTestDetailVO createTest(ListeningCreateTestForm form) {
        validate_create_form(form);

        ListeningTest test = new ListeningTest();
        test.setTitle(trim_to_null(form.getTitle()));
        test.setTotalScore(form.getTotalScore());
        test.setCreatedTime(LocalDateTime.now());
        test.setIsDeleted(0);

        String manual_transcript_text = trim_to_null(form.getTranscriptText());

        if (form.getFile() != null && !form.getFile().isEmpty()) {
            UploadResult upload = storage_service.upload(
                    form.getFile(),
                    BUCKET_TYPE_LISTENING_AUDIO,
                    BIZ_PATH_LISTENING_AUDIO
            );
            test.setAudioUrl(upload.getFileUrl());
            test.setAudioObjectKey(upload.getFileKey());

            String asr_transcript_text = listening_transcript_service.generateTranscript(upload.getFileUrl());
            test.setTranscriptText(first_non_blank(asr_transcript_text, manual_transcript_text));
        } else {
            test.setTranscriptText(manual_transcript_text);
        }

        listening_test_mapper.insertListeningTest(test);

        TestTimerConfig timer_config = new TestTimerConfig();
        timer_config.setTestId(test.getId());
        timer_config.setTimerMode(default_timer_mode(form.getTimerMode()));
        timer_config.setTotalSeconds(form.getTotalSeconds());
        timer_config.setAutoSubmit(default_flag(form.getAutoSubmit(), 1));
        timer_config.setAllowPause(default_flag(form.getAllowPause(), 0));
        listening_test_timer_service.saveOrUpdate(timer_config);

        return build_test_detail_vo(test.getId());
    }

    @Override
    @Transactional
    public ListeningTestDetailVO updateTest(Long id, ListeningTestDTO dto) {
        ListeningTest test = listening_test_mapper.findActiveById(id);
        if (test == null) {
            throw new RuntimeException("Listening test not found");
        }
        if (dto == null) {
            throw new RuntimeException("Request body is required");
        }

        test.setTitle(trim_to_null(dto.getTitle()));
        test.setTotalScore(dto.getTotalScore());
        test.setTranscriptText(trim_to_null(dto.getTranscriptText()));
        listening_test_mapper.updateListeningTest(test);

        if (dto.getTimerConfig() != null) {
            TestTimerConfig timer_config = new TestTimerConfig();
            timer_config.setTestId(id);
            timer_config.setTimerMode(default_timer_mode(dto.getTimerConfig().getTimerMode()));
            timer_config.setTotalSeconds(dto.getTimerConfig().getTotalSeconds());
            timer_config.setAutoSubmit(default_flag(dto.getTimerConfig().getAutoSubmit(), 1));
            timer_config.setAllowPause(default_flag(dto.getTimerConfig().getAllowPause(), 0));
            listening_test_timer_service.saveOrUpdate(timer_config);
        }

        if (dto.getPartGroups() != null) {
            sync_listening_part_groups(id, dto.getPartGroups());
        }

        if (dto.getMaterials() != null) {
            sync_listening_materials(id, dto.getMaterials());
        }

        return build_test_detail_vo(id);
    }

    @Override
    @Transactional
    public ListeningTestDetailVO updateTestAudio(Long id,
                                                 MultipartFile file,
                                                 String title,
                                                 Integer totalScore,
                                                 String transcriptText) {
        ListeningTest test = listening_test_mapper.findActiveById(id);
        if (test == null) {
            throw new RuntimeException("Listening test not found");
        }
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Audio file is required");
        }

        UploadResult upload = storage_service.upload(
                file,
                BUCKET_TYPE_LISTENING_AUDIO,
                BIZ_PATH_LISTENING_AUDIO
        );
        test.setAudioUrl(upload.getFileUrl());
        test.setAudioObjectKey(upload.getFileKey());

        if (title != null) {
            test.setTitle(trim_to_null(title));
        }
        if (totalScore != null) {
            test.setTotalScore(totalScore);
        }

        String manual_transcript_text = trim_to_null(transcriptText);
        String asr_transcript_text = listening_transcript_service.generateTranscript(upload.getFileUrl());
        test.setTranscriptText(first_non_blank(asr_transcript_text, manual_transcript_text));

        listening_test_mapper.updateListeningTest(test);
        return build_test_detail_vo(id);
    }

    @Override
    public List<ListeningTestDetailVO> listTests() {
        List<ListeningTest> tests = listening_test_mapper.findAllActive();
        if (tests == null || tests.isEmpty()) {
            return new ArrayList<>();
        }

        tests.sort(
                Comparator.comparing(
                        ListeningTest::getCreatedTime,
                        Comparator.nullsLast(LocalDateTime::compareTo)
                ).reversed().thenComparing(
                        ListeningTest::getId,
                        Comparator.nullsLast(Long::compareTo)
                )
        );

        List<ListeningTestDetailVO> result = new ArrayList<>();
        for (ListeningTest test : tests) {
            if (test == null || test.getId() == null) {
                continue;
            }
            result.add(build_test_detail_vo(test.getId()));
        }
        return result;
    }

    @Override
    public ListeningTestDetailVO getTestDetail(Long testId) {
        return build_test_detail_vo(testId);
    }

    @Override
    @Transactional
    public void deleteTest(Long id) {
        ListeningTest test = listening_test_mapper.findActiveById(id);
        if (test == null) {
            throw new RuntimeException("Listening test not found");
        }

        listening_question_mapper.softDeleteByTestId(id);
        listening_material_service.deleteByTestId(id);
        listening_part_group_service.deleteByTestId(id);
        listening_test_timer_service.deleteByTestId(id);
        listening_test_mapper.softDeleteById(id);
    }

    @Override
    @Transactional
    public void restoreTest(Long id) {
        ListeningTest test = listening_test_mapper.findAnyById(id);
        if (test == null) {
            throw new RuntimeException("Listening test not found");
        }

        listening_test_mapper.restoreById(id);
        listening_part_group_service.restoreByTestId(id);
        listening_material_service.restoreByTestId(id);
        listening_question_mapper.restoreByTestId(id);
    }

    @Override
    @Transactional
    public void createQuestion(Long testId, ListeningQuestionDTO dto) {
        ListeningTest test = listening_test_mapper.findActiveById(testId);
        if (test == null) {
            throw new RuntimeException("Listening test not found");
        }
        if (dto == null) {
            throw new RuntimeException("Request body is required");
        }

        ListeningMaterial material = validate_listening_material(testId, dto.getMaterialId());
        Long part_group_id = resolve_listening_part_group_id(testId, dto.getPartGroupId(), material);

        ListeningQuestion question = new ListeningQuestion();
        question.setTestId(testId);
        question.setPartGroupId(part_group_id);
        question.setMaterialId(material == null ? null : material.getId());
        question.setSectionNumber(dto.getSectionNumber() == null ? 1 : dto.getSectionNumber());
        question.setQuestionNumber(dto.getQuestionNumber());

        String question_type = normalize_question_type_required(dto.getQuestionType());
        String answer_mode = resolve_answer_mode(question_type, dto.getAnswerMode());

        question.setQuestionType(question_type);
        question.setAnswerMode(answer_mode);
        question.setQuestionText(trim_to_null(dto.getQuestionText()));
        question.setCorrectAnswer(trim_to_null(dto.getCorrectAnswer()));
        question.setOptionsJson(trim_to_null(dto.getOptionsJson()));
        question.setAcceptedAnswersJson(trim_to_null(dto.getAcceptedAnswersJson()));
        question.setCaseInsensitive(default_flag(dto.getCaseInsensitive(), 1));
        question.setIgnoreWhitespace(default_flag(dto.getIgnoreWhitespace(), 1));
        question.setIgnorePunctuation(default_flag(dto.getIgnorePunctuation(), 1));
        question.setDisplayOrder(dto.getDisplayOrder() == null ? 0 : dto.getDisplayOrder());
        question.setScore(dto.getScore() == null ? 1 : dto.getScore());
        question.setIsDeleted(0);

        listening_question_mapper.insertListeningQuestion(question);

        if (dto.getAnswerRules() != null) {
            listening_question_answer_rule_service.replaceByQuestionId(question.getId(), dto.getAnswerRules());
        }

        if (part_group_id != null && has_group_images(dto.getGroupImages())) {
            replace_by_target(part_group_id, dto.getGroupImages());
        }
    }

    @Override
    @Transactional
    public void updateQuestion(Long questionId, ListeningQuestionDTO dto) {
        ListeningQuestion question = listening_question_mapper.findActiveById(questionId);
        if (question == null) {
            throw new RuntimeException("Listening question not found");
        }
        if (dto == null) {
            throw new RuntimeException("Request body is required");
        }

        ListeningMaterial material = validate_listening_material(question.getTestId(), dto.getMaterialId());
        Long part_group_id = resolve_listening_part_group_id(question.getTestId(), dto.getPartGroupId(), material);

        question.setPartGroupId(part_group_id);
        question.setMaterialId(material == null ? null : material.getId());
        question.setSectionNumber(dto.getSectionNumber() == null ? 1 : dto.getSectionNumber());
        question.setQuestionNumber(dto.getQuestionNumber());

        String question_type = normalize_question_type_required(dto.getQuestionType());
        String answer_mode = resolve_answer_mode(question_type, dto.getAnswerMode());

        question.setQuestionType(question_type);
        question.setAnswerMode(answer_mode);
        question.setQuestionText(trim_to_null(dto.getQuestionText()));
        question.setCorrectAnswer(trim_to_null(dto.getCorrectAnswer()));
        question.setOptionsJson(trim_to_null(dto.getOptionsJson()));
        question.setAcceptedAnswersJson(trim_to_null(dto.getAcceptedAnswersJson()));
        question.setCaseInsensitive(default_flag(dto.getCaseInsensitive(), 1));
        question.setIgnoreWhitespace(default_flag(dto.getIgnoreWhitespace(), 1));
        question.setIgnorePunctuation(default_flag(dto.getIgnorePunctuation(), 1));
        question.setDisplayOrder(dto.getDisplayOrder() == null ? 0 : dto.getDisplayOrder());
        question.setScore(dto.getScore() == null ? 1 : dto.getScore());

        listening_question_mapper.updateListeningQuestion(question);

        if (dto.getAnswerRules() != null) {
            listening_question_answer_rule_service.replaceByQuestionId(questionId, dto.getAnswerRules());
        }

        if (part_group_id != null && dto.getGroupImages() != null) {
            replace_by_target(part_group_id, dto.getGroupImages());
        }
    }

    @Override
    @Transactional
    public void deleteQuestion(Long questionId) {
        ListeningQuestion question = listening_question_mapper.findActiveById(questionId);
        if (question == null) {
            throw new RuntimeException("Listening question not found");
        }
        listening_question_mapper.softDeleteById(questionId);
    }

    @Override
    @Transactional
    public void restoreQuestion(Long questionId) {
        ListeningQuestion question = listening_question_mapper.findAnyById(questionId);
        if (question == null) {
            throw new RuntimeException("Listening question not found");
        }

        ListeningTest test = listening_test_mapper.findAnyById(question.getTestId());
        if (test == null) {
            throw new RuntimeException("Listening test not found");
        }
        if (test.getIsDeleted() != null && test.getIsDeleted() == 1) {
            throw new RuntimeException("Cannot restore question because parent test is deleted");
        }

        if (question.getPartGroupId() != null) {
            TestPartGroup part_group = listening_part_group_service.getAnyById(question.getPartGroupId());
            if (part_group == null || (part_group.getIsDeleted() != null && part_group.getIsDeleted() == 1)) {
                throw new RuntimeException("Cannot restore question because part group is deleted");
            }
        }

        if (question.getMaterialId() != null) {
            ListeningMaterial material = listening_material_service.getAnyById(question.getMaterialId());
            if (material == null || (material.getIsDeleted() != null && material.getIsDeleted() == 1)) {
                throw new RuntimeException("Cannot restore question because material is deleted");
            }
        }

        listening_question_mapper.restoreById(questionId);
    }

    @Override
    public PageResult<ListeningRecordVO> pageActiveRecords(AdminListeningRecordPageQuery query) {
        AdminListeningRecordPageQuery safe_query = query == null ? new AdminListeningRecordPageQuery() : query;

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

        Long total = listening_record_mapper.countAdminActive(safe_query);
        if (total == null || total <= 0L) {
            return new PageResult<>(new ArrayList<>(), 0L, page_num, page_size);
        }

        List<ListeningRecord> records = listening_record_mapper.pageAdminActive(safe_query, offset, page_size);
        List<ListeningRecordVO> vo_list = new ArrayList<>();
        if (records != null) {
            for (ListeningRecord record : records) {
                vo_list.add(to_record_vo(record));
            }
        }
        return new PageResult<>(vo_list, total, page_num, page_size);
    }

    @Override
    public PageResult<ListeningRecordVO> pageDeletedRecords(AdminListeningDeletedRecordPageQuery query) {
        AdminListeningDeletedRecordPageQuery safe_query = query == null ? new AdminListeningDeletedRecordPageQuery() : query;

        int page_num = normalize_page_num(safe_query.getPageNum());
        int page_size = normalize_page_size(safe_query.getPageSize());
        int offset = (page_num - 1) * page_size;

        Long total = listening_record_mapper.countAdminDeleted(safe_query);
        if (total == null || total <= 0L) {
            return new PageResult<>(new ArrayList<>(), 0L, page_num, page_size);
        }

        List<ListeningRecord> records = listening_record_mapper.pageAdminDeleted(safe_query, offset, page_size);
        List<ListeningRecordVO> vo_list = new ArrayList<>();
        if (records != null) {
            for (ListeningRecord record : records) {
                vo_list.add(to_record_vo(record));
            }
        }
        return new PageResult<>(vo_list, total, page_num, page_size);
    }

    @Override
    public ListeningRecordDetailVO getRecord(Long recordId) {
        ListeningRecord record = listening_record_mapper.findAnyById(recordId);
        if (record == null) {
            throw new RuntimeException("Listening record not found");
        }

        ListeningTest test = listening_test_mapper.findAnyById(record.getTestId());
        if (test == null) {
            throw new RuntimeException("Listening test not found");
        }

        List<ListeningQuestion> questions = listening_question_mapper.findAnyByTestId(test.getId());
        List<ListeningAnswerRecord> answer_records = listening_answer_record_mapper.findByRecordId(recordId);

        if (questions == null) {
            questions = new ArrayList<>();
        }
        if (answer_records == null) {
            answer_records = new ArrayList<>();
        }

        Map<Long, ListeningAnswerRecord> answer_map = new HashMap<>();
        for (ListeningAnswerRecord answer_record : answer_records) {
            if (answer_record != null && answer_record.getQuestionId() != null) {
                answer_map.put(answer_record.getQuestionId(), answer_record);
            }
        }

        List<ListeningQuestionVO> question_vo_list = new ArrayList<>();
        List<ListeningAnswerResultVO> answer_vo_list = new ArrayList<>();

        for (ListeningQuestion question : questions) {
            if (question == null) {
                continue;
            }

            question_vo_list.add(to_question_vo(question));

            ListeningAnswerRecord matched = answer_map.get(question.getId());

            ListeningAnswerResultVO answer_vo = new ListeningAnswerResultVO();
            answer_vo.setQuestionId(question.getId());
            answer_vo.setQuestionNumber(question.getQuestionNumber());
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

        question_vo_list.sort(
                Comparator.comparing(
                        ListeningQuestionVO::getDisplayOrder,
                        Comparator.nullsLast(Integer::compareTo)
                ).thenComparing(
                        ListeningQuestionVO::getQuestionNumber,
                        Comparator.nullsLast(Integer::compareTo)
                )
        );

        answer_vo_list.sort(
                Comparator.comparing(
                        ListeningAnswerResultVO::getQuestionNumber,
                        Comparator.nullsLast(Integer::compareTo)
                )
        );

        ListeningRecordDetailVO detail_vo = new ListeningRecordDetailVO();
        detail_vo.setRecordId(record.getId());
        detail_vo.setTestId(test.getId());
        detail_vo.setTestTitle(test.getTitle());
        detail_vo.setAudioUrl(test.getAudioUrl());
        detail_vo.setTranscriptText(test.getTranscriptText());
        detail_vo.setTotalScore(record.getTotalScore());
        detail_vo.setCreatedTime(record.getCreatedTime());
        detail_vo.setQuestions(question_vo_list);
        detail_vo.setAnswers(answer_vo_list);

        return detail_vo;
    }

    @Override
    @Transactional
    public void deleteRecord(Long recordId) {
        ListeningRecord record = listening_record_mapper.findAnyById(recordId);
        if (record == null) {
            throw new RuntimeException("Listening record not found");
        }
        if (record.getIsDeleted() != null && record.getIsDeleted() == 1) {
            throw new RuntimeException("Listening record already deleted");
        }
        listening_record_mapper.softDeleteById(recordId);
    }

    @Override
    @Transactional
    public void restoreRecord(Long recordId) {
        ListeningRecord record = listening_record_mapper.findAnyById(recordId);
        if (record == null) {
            throw new RuntimeException("Listening record not found");
        }
        if (record.getIsDeleted() == null || record.getIsDeleted() == 0) {
            throw new RuntimeException("Listening record is not deleted");
        }
        listening_record_mapper.restoreById(recordId);
    }

    private ListeningTestDetailVO build_test_detail_vo(Long test_id) {
        ListeningTest test = listening_test_mapper.findAnyById(test_id);
        if (test == null) {
            throw new RuntimeException("Listening test not found");
        }

        List<TestPartGroup> part_groups = listening_part_group_service.listAnyByTestId(test_id);
        if (part_groups == null) {
            part_groups = new ArrayList<>();
        }
        attach_group_images(part_groups);
        part_groups.sort(
                Comparator.comparing(
                        TestPartGroup::getDisplayOrder,
                        Comparator.nullsLast(Integer::compareTo)
                ).thenComparing(
                        TestPartGroup::getPartNumber,
                        Comparator.nullsLast(Integer::compareTo)
                ).thenComparing(
                        TestPartGroup::getGroupNumber,
                        Comparator.nullsLast(Integer::compareTo)
                ).thenComparing(
                        TestPartGroup::getId,
                        Comparator.nullsLast(Long::compareTo)
                )
        );

        List<ListeningMaterial> materials = listening_material_service.listAnyByTestId(test_id);
        if (materials == null) {
            materials = new ArrayList<>();
        }
        materials.sort(
                Comparator.comparing(
                        ListeningMaterial::getDisplayOrder,
                        Comparator.nullsLast(Integer::compareTo)
                ).thenComparing(
                        ListeningMaterial::getId,
                        Comparator.nullsLast(Long::compareTo)
                )
        );

        List<ListeningQuestion> questions = listening_question_mapper.findAnyByTestId(test_id);
        List<ListeningQuestionVO> question_vo_list = new ArrayList<>();
        if (questions != null) {
            for (ListeningQuestion question : questions) {
                if (question == null) {
                    continue;
                }
                question_vo_list.add(to_question_vo(question));
            }
        }
        question_vo_list.sort(
                Comparator.comparing(
                        ListeningQuestionVO::getDisplayOrder,
                        Comparator.nullsLast(Integer::compareTo)
                ).thenComparing(
                        ListeningQuestionVO::getQuestionNumber,
                        Comparator.nullsLast(Integer::compareTo)
                ).thenComparing(
                        ListeningQuestionVO::getId,
                        Comparator.nullsLast(Long::compareTo)
                )
        );

        ListeningTestDetailVO detail_vo = new ListeningTestDetailVO();
        detail_vo.setId(test.getId());
        detail_vo.setTitle(test.getTitle());
        detail_vo.setAudioUrl(test.getAudioUrl());
        detail_vo.setTranscriptText(test.getTranscriptText());
        detail_vo.setTotalScore(test.getTotalScore());
        detail_vo.setTimerConfig(listening_test_timer_service.getByTestId(test_id));
        detail_vo.setPartGroups(part_groups);
        detail_vo.setMaterials(materials);
        detail_vo.setQuestions(question_vo_list);

        return detail_vo;
    }

    private void validate_create_form(ListeningCreateTestForm form) {
        if (form == null) {
            throw new RuntimeException("Request body is required");
        }
    }

    private void sync_listening_part_groups(Long test_id, List<ListeningTestDTO.PartGroupInput> input_list) {
        List<ListeningTestDTO.PartGroupInput> safe_input = input_list == null ? new ArrayList<>() : input_list;

        List<TestPartGroup> existing_list = listening_part_group_service.listAnyByTestId(test_id);
        if (existing_list == null) {
            existing_list = new ArrayList<>();
        }

        Map<Long, TestPartGroup> existing_map = existing_list.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(TestPartGroup::getId, item -> item));

        Set<Long> incoming_ids = safe_input.stream()
                .filter(Objects::nonNull)
                .map(ListeningTestDTO.PartGroupInput::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (TestPartGroup existing : existing_list) {
            if (existing == null || existing.getId() == null) {
                continue;
            }
            if (!incoming_ids.contains(existing.getId())) {
                biz_image_resource_service.deleteByTarget(TARGET_TYPE_LISTENING_PART_GROUP, existing.getId());
                listening_part_group_service.deleteById(existing.getId());
            }
        }

        for (ListeningTestDTO.PartGroupInput input : safe_input) {
            if (input == null) {
                continue;
            }

            TestPartGroup part_group = build_part_group_from_input(test_id, input);
            List<BizImageResourceDTO> image_dto_list = safe_image_dto_list(input.getImages());

            if (input.getId() == null) {
                TestPartGroup created = listening_part_group_service.createPartGroup(part_group);
                if (!image_dto_list.isEmpty()) {
                    replace_listening_part_group_images(created.getId(), image_dto_list);
                }
                continue;
            }

            TestPartGroup existing = existing_map.get(input.getId());
            if (existing == null || !Objects.equals(existing.getTestId(), test_id)) {
                throw new RuntimeException("Listening part group not found");
            }

            if (existing.getIsDeleted() != null && existing.getIsDeleted() == 1) {
                listening_part_group_service.restoreById(existing.getId());
            }

            TestPartGroup updated = listening_part_group_service.updatePartGroup(existing.getId(), part_group);
            if (input.getImages() != null) {
                replace_listening_part_group_images(updated.getId(), image_dto_list);
            }
        }
    }

    private void sync_listening_materials(Long test_id, List<ListeningTestDTO.MaterialInput> input_list) {
        List<ListeningTestDTO.MaterialInput> safe_input = input_list == null ? new ArrayList<>() : input_list;

        List<ListeningMaterial> existing_list = listening_material_service.listAnyByTestId(test_id);
        if (existing_list == null) {
            existing_list = new ArrayList<>();
        }

        Map<Long, ListeningMaterial> existing_map = existing_list.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(ListeningMaterial::getId, item -> item));

        Set<Long> incoming_ids = safe_input.stream()
                .filter(Objects::nonNull)
                .map(ListeningTestDTO.MaterialInput::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (ListeningMaterial existing : existing_list) {
            if (existing != null && existing.getId() != null && !incoming_ids.contains(existing.getId())) {
                listening_material_service.deleteById(existing.getId());
            }
        }

        for (ListeningTestDTO.MaterialInput input : safe_input) {
            if (input == null) {
                continue;
            }

            ListeningMaterial material = build_material_from_input(test_id, input);

            if (material.getPartGroupId() != null) {
                TestPartGroup part_group = listening_part_group_service.getAnyById(material.getPartGroupId());
                if (part_group == null || !Objects.equals(part_group.getTestId(), test_id)) {
                    throw new RuntimeException("Listening material part group does not belong to test");
                }
            }

            if (input.getId() == null) {
                listening_material_service.createMaterial(material);
                continue;
            }

            ListeningMaterial existing = existing_map.get(input.getId());
            if (existing == null || !Objects.equals(existing.getTestId(), test_id)) {
                throw new RuntimeException("Listening material not found");
            }

            if (existing.getIsDeleted() != null && existing.getIsDeleted() == 1) {
                listening_material_service.restoreById(existing.getId());
            }

            listening_material_service.updateMaterial(existing.getId(), material);
        }
    }

    private TestPartGroup build_part_group_from_input(Long test_id, ListeningTestDTO.PartGroupInput input) {
        TestPartGroup part_group = new TestPartGroup();
        part_group.setId(input.getId());
        part_group.setTestId(test_id);
        part_group.setPartNumber(input.getPartNumber() == null ? 1 : input.getPartNumber());
        part_group.setGroupNumber(input.getGroupNumber() == null ? 1 : input.getGroupNumber());
        part_group.setTitle(trim_to_null(input.getTitle()));
        part_group.setInstructionText(trim_to_null(input.getInstructionText()));
        part_group.setGroupGuideText(trim_to_null(input.getGroupGuideText()));
        part_group.setGroupRequirementText(trim_to_null(input.getGroupRequirementText()));
        part_group.setQuestionNoStart(input.getQuestionNoStart());
        part_group.setQuestionNoEnd(input.getQuestionNoEnd());
        part_group.setDisplayOrder(input.getDisplayOrder() == null ? 0 : input.getDisplayOrder());
        part_group.setTimeLimitSeconds(input.getTimeLimitSeconds());
        return part_group;
    }

    private ListeningMaterial build_material_from_input(Long test_id, ListeningTestDTO.MaterialInput input) {
        ListeningMaterial material = new ListeningMaterial();
        material.setId(input.getId());
        material.setTestId(test_id);
        material.setPartGroupId(input.getPartGroupId());
        material.setTitle(trim_to_null(input.getTitle()));
        material.setAudioUrl(trim_to_null(input.getAudioUrl()));
        material.setAudioObjectKey(trim_to_null(input.getAudioObjectKey()));
        material.setTranscriptText(trim_to_null(input.getTranscriptText()));
        material.setDisplayOrder(input.getDisplayOrder() == null ? 0 : input.getDisplayOrder());
        return material;
    }

    private ListeningMaterial validate_listening_material(Long test_id, Long material_id) {
        if (material_id == null) {
            return null;
        }

        ListeningMaterial material = listening_material_service.getActiveById(material_id);
        if (material == null) {
            throw new RuntimeException("Listening material not found");
        }
        if (!Objects.equals(material.getTestId(), test_id)) {
            throw new RuntimeException("Listening material does not belong to test");
        }
        return material;
    }

    private Long resolve_listening_part_group_id(Long test_id, Long part_group_id, ListeningMaterial material) {
        Long resolved_part_group_id = part_group_id;

        if (material != null) {
            if (resolved_part_group_id == null) {
                resolved_part_group_id = material.getPartGroupId();
            } else if (!Objects.equals(resolved_part_group_id, material.getPartGroupId())) {
                throw new RuntimeException("Question part group and material part group do not match");
            }
        }

        if (resolved_part_group_id == null) {
            return null;
        }

        TestPartGroup part_group = listening_part_group_service.getActiveById(resolved_part_group_id);
        if (part_group == null) {
            throw new RuntimeException("Listening part group not found");
        }
        if (!Objects.equals(part_group.getTestId(), test_id)) {
            throw new RuntimeException("Listening part group does not belong to test");
        }

        return resolved_part_group_id;
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
                TARGET_TYPE_LISTENING_PART_GROUP,
                target_ids
        );
        if (image_map == null) {
            image_map = Collections.emptyMap();
        }

        for (TestPartGroup part_group : part_groups) {
            if (part_group == null || part_group.getId() == null) {
                continue;
            }
            part_group.setImages(
                    image_map.getOrDefault(part_group.getId(), new ArrayList<>())
            );
        }
    }

    private void replace_by_target(Long part_group_id, List<BizImageResourceDTO> group_images) {
        replace_listening_part_group_images(part_group_id, group_images);
    }

    private void replace_listening_part_group_images(Long part_group_id, List<BizImageResourceDTO> group_images) {
        if (part_group_id == null) {
            throw new RuntimeException("part_group_id is required");
        }
        if (group_images == null) {
            return;
        }

        biz_image_resource_service.replaceByTarget(
                TARGET_TYPE_LISTENING_PART_GROUP,
                part_group_id,
                BUCKET_TYPE_QUESTION_GROUP_IMAGE,
                BIZ_PATH_QUESTION_GROUP_IMAGE,
                group_images
        );
    }

    private List<BizImageResourceDTO> safe_image_dto_list(List<BizImageResourceDTO> images) {
        if (images == null || images.isEmpty()) {
            return new ArrayList<>();
        }
        return images.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private String normalize_question_type_required(String raw_question_type) {
        String question_type = ListeningQuestionConstants.normalize_question_type(raw_question_type);
        if (!ListeningQuestionConstants.supports_question_type(question_type)) {
            throw new RuntimeException("Unsupported listening question type: " + raw_question_type);
        }
        return question_type;
    }

    private String resolve_answer_mode(String question_type, String raw_answer_mode) {
        String answer_mode = ListeningQuestionConstants.infer_answer_mode(question_type, raw_answer_mode);
        if (!ListeningQuestionConstants.supports_answer_mode(answer_mode)) {
            throw new RuntimeException("Unsupported listening answer mode: " + raw_answer_mode);
        }
        return answer_mode;
    }

    private ListeningQuestionVO to_question_vo(ListeningQuestion question) {
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
        vo.setAnswerRules(listening_question_answer_rule_service.listByQuestionId(question.getId()));
        return vo;
    }

    private ListeningRecordVO to_record_vo(ListeningRecord record) {
        ListeningRecordVO vo = new ListeningRecordVO();
        vo.setId(record.getId());
        vo.setUserId(record.getUserId());
        vo.setTestId(record.getTestId());
        vo.setTotalScore(record.getTotalScore());
        vo.setCreatedTime(record.getCreatedTime());
        vo.setIsDeleted(record.getIsDeleted());

        ListeningTest test = listening_test_mapper.findAnyById(record.getTestId());
        vo.setTestTitle(test == null ? null : test.getTitle());

        return vo;
    }

    private int normalize_page_num(Integer page_num) {
        return page_num == null || page_num < 1 ? 1 : page_num;
    }

    private int normalize_page_size(Integer page_size) {
        return page_size == null || page_size < 1 ? 10 : Math.min(page_size, 100);
    }

    private Integer default_flag(Integer value, Integer default_value) {
        return value == null ? default_value : value;
    }

    private String default_timer_mode(String timer_mode) {
        String normalized_timer_mode = trim_to_null(timer_mode);
        return normalized_timer_mode == null ? TIMER_MODE_NONE : normalized_timer_mode;
    }

    private String trim_to_null(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String first_non_blank(String first, String second) {
        String first_value = trim_to_null(first);
        if (first_value != null) {
            return first_value;
        }
        return trim_to_null(second);
    }

    private boolean has_group_images(List<BizImageResourceDTO> images) {
        return images != null && !images.isEmpty();
    }

    private String build_display_correct_answer(ListeningQuestion question) {
        if (question == null) {
            return null;
        }

        List<QuestionAnswerRule> rules = listening_question_answer_rule_service.listByQuestionId(question.getId());
        String rule_display = build_display_correct_answer_by_rules(rules);
        if (rule_display != null) {
            return rule_display;
        }

        String accepted_answers_json = trim_to_null(question.getAcceptedAnswersJson());
        if (accepted_answers_json != null) {
            return accepted_answers_json;
        }

        return trim_to_null(question.getCorrectAnswer());
    }

    private String build_display_correct_answer_by_rules(List<QuestionAnswerRule> rules) {
        if (rules == null || rules.isEmpty()) {
            return null;
        }

        Map<Integer, Map<Integer, List<QuestionAnswerRule>>> blank_group_map = new LinkedHashMap<>();

        for (QuestionAnswerRule rule : rules) {
            if (rule == null || trim_to_null(rule.getAnswerText()) == null) {
                continue;
            }

            Integer blank_no = rule.getBlankNo() == null ? 1 : rule.getBlankNo();
            Integer group_no = rule.getAnswerGroupNo() == null ? 1 : rule.getAnswerGroupNo();

            blank_group_map
                    .computeIfAbsent(blank_no, k -> new LinkedHashMap<>())
                    .computeIfAbsent(group_no, k -> new ArrayList<>())
                    .add(rule);
        }

        if (blank_group_map.isEmpty()) {
            return null;
        }

        List<String> blank_displays = new ArrayList<>();

        for (Map.Entry<Integer, Map<Integer, List<QuestionAnswerRule>>> blank_entry : blank_group_map.entrySet()) {
            List<String> group_displays = new ArrayList<>();

            for (Map.Entry<Integer, List<QuestionAnswerRule>> group_entry : blank_entry.getValue().entrySet()) {
                List<String> accepted = pick_display_answers(group_entry.getValue());
                if (!accepted.isEmpty()) {
                    group_displays.add(String.join(" / ", accepted));
                }
            }

            if (!group_displays.isEmpty()) {
                blank_displays.add(String.join(" | ", group_displays));
            }
        }

        return blank_displays.isEmpty() ? null : String.join(", ", blank_displays);
    }

    private List<String> pick_display_answers(List<QuestionAnswerRule> rules) {
        if (rules == null || rules.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> primary = rules.stream()
                .filter(Objects::nonNull)
                .filter(rule -> rule.getIsPrimary() != null && rule.getIsPrimary() == 1)
                .map(QuestionAnswerRule::getAnswerText)
                .map(this::trim_to_null)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (!primary.isEmpty()) {
            return primary;
        }

        return rules.stream()
                .filter(Objects::nonNull)
                .map(QuestionAnswerRule::getAnswerText)
                .map(this::trim_to_null)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }
}
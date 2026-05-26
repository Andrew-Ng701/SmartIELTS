package com.andrew.smartielts.listening.service.admin.impl;

import com.andrew.smartielts.common.domain.pojo.TestPartGroup;
import com.andrew.smartielts.common.image.domain.dto.BizImageResourceDTO;
import com.andrew.smartielts.common.image.domain.pojo.BizImageResource;
import com.andrew.smartielts.common.image.service.BizImageResourceService;
import com.andrew.smartielts.common.storage.BucketType;
import com.andrew.smartielts.listening.constants.ListeningAudioConstants;
import com.andrew.smartielts.listening.constants.ListeningConstants;
import com.andrew.smartielts.listening.constants.ListeningQuestionConstants;
import com.andrew.smartielts.listening.domain.dto.AdminListeningTestFullSaveDTO;
import com.andrew.smartielts.listening.domain.dto.ListeningAudioUpsertDTO;
import com.andrew.smartielts.listening.domain.dto.ListeningPartGroupDTO;
import com.andrew.smartielts.listening.domain.dto.ListeningQuestionDTO;
import com.andrew.smartielts.listening.domain.dto.ListeningTestDTO;
import com.andrew.smartielts.listening.domain.pojo.ListeningAnswerRecord;
import com.andrew.smartielts.listening.domain.pojo.ListeningAudio;
import com.andrew.smartielts.listening.domain.pojo.ListeningQuestion;
import com.andrew.smartielts.listening.domain.pojo.ListeningRecord;
import com.andrew.smartielts.listening.domain.pojo.ListeningTest;
import com.andrew.smartielts.listening.domain.vo.ListeningAnswerResultVO;
import com.andrew.smartielts.listening.domain.vo.ListeningPartGroupVO;
import com.andrew.smartielts.listening.domain.vo.ListeningPartVO;
import com.andrew.smartielts.listening.domain.vo.ListeningQuestionVO;
import com.andrew.smartielts.listening.domain.vo.ListeningRecordDetailVO;
import com.andrew.smartielts.listening.domain.vo.ListeningTestDetailVO;
import com.andrew.smartielts.listening.mapper.ListeningAnswerRecordMapper;
import com.andrew.smartielts.listening.mapper.ListeningQuestionMapper;
import com.andrew.smartielts.listening.mapper.ListeningRecordMapper;
import com.andrew.smartielts.listening.mapper.ListeningTestMapper;
import com.andrew.smartielts.listening.service.admin.AdminListeningService;
import com.andrew.smartielts.listening.service.admin.ListeningAudioService;
import com.andrew.smartielts.listening.service.admin.ListeningPartGroupService;
import com.andrew.smartielts.listening.support.ListeningGroupAnswerRuleSupport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminListeningServiceImpl implements AdminListeningService {

    private final ListeningTestMapper listeningTestMapper;
    private final ListeningQuestionMapper listeningQuestionMapper;
    private final ListeningRecordMapper listeningRecordMapper;
    private final ListeningAnswerRecordMapper listeningAnswerRecordMapper;
    private final ListeningAudioService listeningAudioService;
    private final ListeningPartGroupService listeningPartGroupService;
    private final ListeningGroupAnswerRuleSupport listeningGroupAnswerRuleSupport;
    private final BizImageResourceService bizImageResourceService;

    public AdminListeningServiceImpl(
            ListeningTestMapper listeningTestMapper,
            ListeningQuestionMapper listeningQuestionMapper,
            ListeningRecordMapper listeningRecordMapper,
            ListeningAnswerRecordMapper listeningAnswerRecordMapper,
            ListeningAudioService listeningAudioService,
            ListeningPartGroupService listeningPartGroupService,
            ListeningGroupAnswerRuleSupport listeningGroupAnswerRuleSupport,
            BizImageResourceService bizImageResourceService
    ) {
        this.listeningTestMapper = listeningTestMapper;
        this.listeningQuestionMapper = listeningQuestionMapper;
        this.listeningRecordMapper = listeningRecordMapper;
        this.listeningAnswerRecordMapper = listeningAnswerRecordMapper;
        this.listeningAudioService = listeningAudioService;
        this.listeningPartGroupService = listeningPartGroupService;
        this.listeningGroupAnswerRuleSupport = listeningGroupAnswerRuleSupport;
        this.bizImageResourceService = bizImageResourceService;
    }

    @Override
    @Transactional
    public ListeningTestDetailVO createTest(ListeningTestDTO dto) {
        validateListeningTestDto(dto);

        ListeningTest test = new ListeningTest();
        test.setTitle(trimToNull(dto.getTitle()));
        test.setTotalScore(dto.getTotalScore());
        test.setTimerMode(resolveTimerMode(dto.getTimerMode()));
        test.setPrepSeconds(requiredPrepSeconds(dto));
        test.setTotalSeconds(minutesToSeconds(requiredTotalMinutes(dto.getTotalMinutes())));
        test.setAutoSubmit(resolveAutoSubmit(dto.getAutoSubmit()));
        test.setAllowPause(resolveAllowPause(dto.getAllowPause()));
        test.setAllowAudioSeek(resolveAllowAudioSeek(dto.getAllowAudioSeek()));
        test.setCreatedTime(LocalDateTime.now());
        test.setUpdatedTime(LocalDateTime.now());
        test.setIsDeleted(ListeningConstants.NOT_DELETED);

        listeningTestMapper.insertListeningTest(test);
        return buildTestDetailVO(test.getId(), true);
    }

    @Override
    @Transactional
    public ListeningTestDetailVO updateTest(Long id, ListeningTestDTO dto) {
        ListeningTest existing = requireActiveTest(id);
        validateListeningTestDto(dto);

        existing.setTitle(trimToNull(dto.getTitle()));
        existing.setTotalScore(dto.getTotalScore());
        existing.setTimerMode(resolveTimerMode(dto.getTimerMode()));
        existing.setPrepSeconds(requiredPrepSeconds(dto));
        existing.setTotalSeconds(minutesToSeconds(requiredTotalMinutes(dto.getTotalMinutes())));
        existing.setAutoSubmit(resolveAutoSubmit(dto.getAutoSubmit()));
        existing.setAllowPause(resolveAllowPause(dto.getAllowPause()));
        existing.setAllowAudioSeek(resolveAllowAudioSeek(dto.getAllowAudioSeek()));
        existing.setUpdatedTime(LocalDateTime.now());

        listeningTestMapper.updateListeningTest(existing);
        return buildTestDetailVO(id, true);
    }

    @Override
    @Transactional
    public ListeningTestDetailVO saveFullTest(Long testId, AdminListeningTestFullSaveDTO dto) {
        if (dto == null) {
            throw new RuntimeException("listening_full_test_payload_is_required");
        }
        updateTest(testId, dto.getTest());
        syncFullPartGroups(testId, dto.getPartGroups());
        syncFullQuestions(testId, dto.getQuestions());
        syncFullAudioReferences(testId, dto.getAudios());
        return buildTestDetailVO(testId, true);
    }

    @Override
    public List<ListeningTestDetailVO> listTests() {
        List<ListeningTest> tests = listeningTestMapper.findAllActive();
        if (tests == null || tests.isEmpty()) {
            return new ArrayList<>();
        }

        return tests.stream()
                .filter(Objects::nonNull)
                .map(ListeningTest::getId)
                .filter(Objects::nonNull)
                .map(id -> buildTestDetailVO(id, true))
                .collect(Collectors.toList());
    }

    @Override
    public ListeningTestDetailVO getTestDetail(Long testId) {
        return buildTestDetailVO(testId, true);
    }

    @Override
    @Transactional
    public void deleteTest(Long id) {
        requireActiveTest(id);
        deletePartGroupImages(listeningPartGroupService.listActiveByTestId(id));
        deleteListeningQuestionImages(listeningQuestionMapper.findActiveByTestId(id));
        listeningQuestionMapper.softDeleteByTestId(id);
        listeningAudioService.deleteByTestId(id);
        listeningPartGroupService.deleteByTestId(id);
        listeningTestMapper.softDeleteById(id);
    }

    @Override
    @Transactional
    public void restoreTest(Long id) {
        ListeningTest test = listeningTestMapper.findAnyById(id);
        if (test == null) {
            throw new RuntimeException("listening_test_not_found");
        }
        listeningTestMapper.restoreById(id);
        listeningPartGroupService.restoreByTestId(id);
        listeningQuestionMapper.restoreByTestId(id);
    }

    @Override
    @Transactional
    public void createQuestion(Long testId, ListeningQuestionDTO dto) {
        requireActiveTest(testId);
        ListeningQuestion question = buildQuestionForCreate(testId, dto);
        listeningQuestionMapper.insertListeningQuestion(question);
        replacePartGroupImages(question.getPartGroupId(), dto.getGroupImages());
    }

    @Override
    @Transactional
    public void updateQuestion(Long questionId, ListeningQuestionDTO dto) {
        ListeningQuestion existing = requireActiveQuestion(questionId);
        ListeningQuestion payload = buildQuestionForUpdate(existing, dto);
        listeningQuestionMapper.updateListeningQuestion(payload);
        replacePartGroupImages(payload.getPartGroupId(), dto.getGroupImages());
    }

    @Override
    @Transactional
    public void replaceQuestionImages(Long questionId, MultipartFile[] images) {
        requireActiveQuestion(questionId);
        bizImageResourceService.replaceByTargetFromUploads(
                ListeningAudioConstants.TARGET_TYPE_LISTENING_QUESTION,
                questionId,
                BucketType.QUESTION_GROUP_IMAGE,
                ListeningAudioConstants.BIZ_PATH_LISTENING_QUESTION_IMAGE,
                images
        );
    }

    @Override
    @Transactional
    public void deleteQuestion(Long questionId) {
        requireActiveQuestion(questionId);
        listeningQuestionMapper.softDeleteById(questionId);
        bizImageResourceService.deleteByTargetAndObjects(
                ListeningAudioConstants.TARGET_TYPE_LISTENING_QUESTION,
                questionId,
                BucketType.QUESTION_GROUP_IMAGE
        );
    }

    @Override
    @Transactional
    public void restoreQuestion(Long questionId) {
        ListeningQuestion question = listeningQuestionMapper.findAnyById(questionId);
        if (question == null) {
            throw new RuntimeException("listening_question_not_found");
        }
        listeningQuestionMapper.restoreById(questionId);
    }

    @Override
    public ListeningRecordDetailVO getRecord(Long recordId) {
        ListeningRecord record = listeningRecordMapper.findAnyById(recordId);
        if (record == null) {
            throw new RuntimeException("listening_record_not_found");
        }
        return buildRecordDetailVO(record);
    }

    @Override
    @Transactional
    public void deleteRecord(Long recordId) {
        ListeningRecord record = listeningRecordMapper.findAnyById(recordId);
        if (record == null) {
            throw new RuntimeException("listening_record_not_found");
        }
        listeningRecordMapper.softDeleteById(recordId);
    }

    @Override
    @Transactional
    public void restoreRecord(Long recordId) {
        ListeningRecord record = listeningRecordMapper.findAnyById(recordId);
        if (record == null) {
            throw new RuntimeException("listening_record_not_found");
        }
        listeningRecordMapper.restoreById(recordId);
    }

    private ListeningTestDetailVO buildTestDetailVO(Long testId, boolean activeOnly) {
        ListeningTest test = activeOnly
                ? listeningTestMapper.findActiveById(testId)
                : listeningTestMapper.findAnyById(testId);
        if (test == null) {
            throw new RuntimeException("listening_test_not_found");
        }

        List<TestPartGroup> partGroups = activeOnly
                ? listeningPartGroupService.listActiveByTestId(testId)
                : listeningPartGroupService.listAnyByTestId(testId);
        List<ListeningQuestion> questions = activeOnly
                ? listeningQuestionMapper.findActiveByTestId(testId)
                : listeningQuestionMapper.findAnyByTestId(testId);
        List<ListeningAudio> audios = listeningAudioService.listByTestId(testId);

        if (partGroups == null) {
            partGroups = new ArrayList<>();
        }
        if (questions == null) {
            questions = new ArrayList<>();
        }
        if (audios == null) {
            audios = new ArrayList<>();
        }

        attachPartGroupImages(partGroups);

        Map<Long, List<BizImageResourceDTO>> partGroupImageMap = partGroups.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(
                        TestPartGroup::getId,
                        item -> toBizImageResourceDTOList(item.getImages()),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
        Map<Long, List<BizImageResource>> questionImageMap = findQuestionImageMap(questions);

        List<ListeningQuestionVO> questionVOList = questions.stream()
                .filter(Objects::nonNull)
                .map(this::toQuestionVO)
                .peek(vo -> vo.setGroupImages(new ArrayList<>(
                        partGroupImageMap.getOrDefault(vo.getPartGroupId(), new ArrayList<>())
                )))
                .peek(vo -> vo.setImages(toBizImageResourceDTOList(questionImageMap.get(vo.getId()))))
                .sorted(Comparator
                        .comparing(ListeningQuestionVO::getDisplayOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(ListeningQuestionVO::getQuestionNumber, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(ListeningQuestionVO::getId, Comparator.nullsLast(Long::compareTo)))
                .collect(Collectors.toList());

        ListeningTestDetailVO detailVO = new ListeningTestDetailVO();
        detailVO.setId(test.getId());
        detailVO.setTitle(test.getTitle());
        detailVO.setTotalScore(test.getTotalScore());
        detailVO.setTimerMode(test.getTimerMode());
        detailVO.setPrepSeconds(resolvePrepSeconds(test));
        detailVO.setTotalSeconds(resolveTotalSeconds(test));
        detailVO.setPrepMinutes(secondsToMinutes(detailVO.getPrepSeconds()));
        detailVO.setTotalMinutes(secondsToMinutes(detailVO.getTotalSeconds()));
        detailVO.setAutoSubmit(test.getAutoSubmit());
        detailVO.setAllowPause(test.getAllowPause());
        detailVO.setAllowAudioSeek(resolveAllowAudioSeek(test.getAllowAudioSeek()));
        detailVO.setTestAudio(findTestAudio(audios));
        detailVO.setParts(buildPartVOList(partGroups, questionVOList, audios));
        detailVO.setPartGroups(sortPartGroups(partGroups));
        detailVO.setPartGroupAudios(findPartGroupAudios(audios));
        detailVO.setQuestions(questionVOList);
        return detailVO;
    }

    private ListeningRecordDetailVO buildRecordDetailVO(ListeningRecord record) {
        ListeningTest test = listeningTestMapper.findAnyById(record.getTestId());
        if (test == null) {
            throw new RuntimeException("listening_test_not_found");
        }

        List<TestPartGroup> partGroups = listeningPartGroupService.listAnyByTestId(test.getId());
        List<ListeningQuestion> questions = listeningQuestionMapper.findAnyByTestId(test.getId());
        List<ListeningAnswerRecord> answerRecords = listeningAnswerRecordMapper.findByRecordId(record.getId());
        List<ListeningAudio> audios = listeningAudioService.listByTestId(test.getId());

        if (partGroups == null) {
            partGroups = new ArrayList<>();
        }
        if (questions == null) {
            questions = new ArrayList<>();
        }
        if (answerRecords == null) {
            answerRecords = new ArrayList<>();
        }
        if (audios == null) {
            audios = new ArrayList<>();
        }

        attachPartGroupImages(partGroups);

        Map<Long, TestPartGroup> groupMap = partGroups.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(
                        TestPartGroup::getId,
                        item -> item,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        Map<Long, List<BizImageResourceDTO>> partGroupImageMap = partGroups.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(
                        TestPartGroup::getId,
                        item -> toBizImageResourceDTOList(item.getImages()),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
        Map<Long, List<BizImageResource>> questionImageMap = findQuestionImageMap(questions);

        Map<Long, ListeningAnswerRecord> answerMap = answerRecords.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getQuestionId() != null)
                .collect(Collectors.toMap(
                        ListeningAnswerRecord::getQuestionId,
                        item -> item,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        List<ListeningQuestionVO> questionVOList = questions.stream()
                .filter(Objects::nonNull)
                .map(this::toQuestionVO)
                .peek(vo -> vo.setGroupImages(new ArrayList<>(
                        partGroupImageMap.getOrDefault(vo.getPartGroupId(), new ArrayList<>())
                )))
                .peek(vo -> vo.setImages(toBizImageResourceDTOList(questionImageMap.get(vo.getId()))))
                .sorted(Comparator
                        .comparing(ListeningQuestionVO::getDisplayOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(ListeningQuestionVO::getQuestionNumber, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(ListeningQuestionVO::getId, Comparator.nullsLast(Long::compareTo)))
                .collect(Collectors.toList());

        List<ListeningAnswerResultVO> answerVOList = new ArrayList<>();
        for (ListeningQuestion question : questions) {
            ListeningAnswerRecord matched = answerMap.get(question.getId());
            TestPartGroup partGroup = groupMap.get(question.getPartGroupId());
            ListeningGroupAnswerRuleSupport.ResolvedRule resolvedRule =
                    listeningGroupAnswerRuleSupport.resolve(question, partGroup);

            ListeningAnswerResultVO answerVO = new ListeningAnswerResultVO();
            answerVO.setQuestionId(question.getId());
            answerVO.setQuestionNumber(question.getQuestionNumber());
            answerVO.setQuestionType(resolvedRule.getQuestionType());
            answerVO.setAnswerMode(resolvedRule.getAnswerMode());
            answerVO.setQuestionText(question.getQuestionText());
            answerVO.setOptionsJson(resolvedRule.getOptionsJson());
            answerVO.setCorrectAnswer(buildDisplayCorrectAnswer(question, partGroup));

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

        answerVOList.sort(Comparator
                .comparing(ListeningAnswerResultVO::getQuestionNumber, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(ListeningAnswerResultVO::getQuestionId, Comparator.nullsLast(Long::compareTo)));

        ListeningRecordDetailVO detailVO = new ListeningRecordDetailVO();
        detailVO.setRecordId(record.getId());
        detailVO.setTestId(test.getId());
        detailVO.setTestTitle(test.getTitle());
        detailVO.setTestAudio(findTestAudio(audios));
        detailVO.setAllowAudioSeek(resolveAllowAudioSeek(test.getAllowAudioSeek()));
        detailVO.setParts(buildPartVOList(partGroups, questionVOList, audios));
        detailVO.setPartGroupAudios(findPartGroupAudios(audios));
        detailVO.setTotalScore(record.getTotalScore());
        detailVO.setCreatedTime(record.getCreatedTime());
        detailVO.setQuestions(questionVOList);
        detailVO.setAnswers(answerVOList);
        return detailVO;
    }

    private ListeningQuestion buildQuestionForCreate(Long testId, ListeningQuestionDTO dto) {
        validateListeningQuestionDto(dto);
        validatePartGroup(testId, dto.getPartGroupId());

        ListeningQuestion question = new ListeningQuestion();
        question.setTestId(testId);
        question.setPartGroupId(dto.getPartGroupId());
        question.setSectionNumber(resolveSectionNumberFromPartGroup(testId, dto));
        question.setQuestionNumber(dto.getQuestionNumber());
        question.setQuestionType(normalizeQuestionType(dto.getQuestionType()));
        question.setAnswerMode(resolveAnswerMode(dto.getQuestionType(), dto.getAnswerMode()));
        question.setQuestionText(preserveTextOrNull(dto.getQuestionText()));
        question.setCorrectAnswer(trimToNull(dto.getCorrectAnswer()));
        question.setOptionsJson(trimToNull(dto.getOptionsJson()));
        question.setAcceptedAnswersJson(trimToNull(dto.getAcceptedAnswersJson()));
        question.setCaseInsensitive(defaultInt(dto.getCaseInsensitive(), 1));
        question.setIgnoreWhitespace(defaultInt(dto.getIgnoreWhitespace(), 1));
        question.setIgnorePunctuation(defaultInt(dto.getIgnorePunctuation(), 0));
        question.setDisplayOrder(defaultInt(dto.getDisplayOrder(), 0));
        question.setScore(defaultInt(dto.getScore(), 1));
        question.setIsDeleted(ListeningConstants.NOT_DELETED);
        return question;
    }

    private ListeningQuestion buildQuestionForUpdate(ListeningQuestion existing, ListeningQuestionDTO dto) {
        validateListeningQuestionDto(dto);
        validatePartGroup(existing.getTestId(), dto.getPartGroupId());

        ListeningQuestion question = new ListeningQuestion();
        question.setId(existing.getId());
        question.setTestId(existing.getTestId());
        question.setPartGroupId(dto.getPartGroupId());
        question.setSectionNumber(resolveSectionNumberFromPartGroup(existing.getTestId(), dto));
        question.setQuestionNumber(dto.getQuestionNumber());
        question.setQuestionType(normalizeQuestionType(dto.getQuestionType()));
        question.setAnswerMode(resolveAnswerMode(dto.getQuestionType(), dto.getAnswerMode()));
        question.setQuestionText(preserveTextOrNull(dto.getQuestionText()));
        question.setCorrectAnswer(trimToNull(dto.getCorrectAnswer()));
        question.setOptionsJson(trimToNull(dto.getOptionsJson()));
        question.setAcceptedAnswersJson(trimToNull(dto.getAcceptedAnswersJson()));
        question.setCaseInsensitive(defaultInt(dto.getCaseInsensitive(), 1));
        question.setIgnoreWhitespace(defaultInt(dto.getIgnoreWhitespace(), 1));
        question.setIgnorePunctuation(defaultInt(dto.getIgnorePunctuation(), 0));
        question.setDisplayOrder(defaultInt(dto.getDisplayOrder(), 0));
        question.setScore(defaultInt(dto.getScore(), 1));
        question.setIsDeleted(existing.getIsDeleted());
        return question;
    }

    private void attachPartGroupImages(List<TestPartGroup> partGroups) {
        if (partGroups == null || partGroups.isEmpty()) {
            return;
        }

        List<Long> partGroupIds = partGroups.stream()
                .filter(Objects::nonNull)
                .map(TestPartGroup::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (partGroupIds.isEmpty()) {
            for (TestPartGroup partGroup : partGroups) {
                if (partGroup != null) {
                    partGroup.setImages(new ArrayList<>());
                }
            }
            return;
        }

        Map<Long, List<BizImageResource>> imageMap = bizImageResourceService.listByTargets(
                ListeningAudioConstants.TARGET_TYPE_LISTENING_PART_GROUP,
                partGroupIds
        );

        for (TestPartGroup partGroup : partGroups) {
            if (partGroup == null) {
                continue;
            }
            List<BizImageResource> images = imageMap == null ? null : imageMap.get(partGroup.getId());
            partGroup.setImages(images == null ? new ArrayList<>() : new ArrayList<>(images));
        }
    }

    private void replacePartGroupImages(Long partGroupId, List<BizImageResourceDTO> images) {
        if (partGroupId == null) {
            return;
        }
        if (images == null) {
            return;
        }

        List<BizImageResourceDTO> safeImages = images.stream().filter(Objects::nonNull).collect(Collectors.toList());

        bizImageResourceService.replaceByTarget(
                ListeningAudioConstants.TARGET_TYPE_LISTENING_PART_GROUP,
                partGroupId,
                BucketType.QUESTION_GROUP_IMAGE.getKey(),
                ListeningAudioConstants.BIZ_PATH_LISTENING_PART_GROUP_IMAGE,
                safeImages
        );
    }

    private void replaceQuestionImages(Long questionId, List<BizImageResourceDTO> images) {
        if (questionId == null || images == null) {
            return;
        }
        bizImageResourceService.replaceByTarget(
                ListeningAudioConstants.TARGET_TYPE_LISTENING_QUESTION,
                questionId,
                BucketType.QUESTION_GROUP_IMAGE.getKey(),
                ListeningAudioConstants.BIZ_PATH_LISTENING_QUESTION_IMAGE,
                images
        );
    }

    private void syncFullPartGroups(Long testId, List<TestPartGroup> incomingPartGroups) {
        if (incomingPartGroups == null) {
            return;
        }
        Set<Long> incomingIds = incomingPartGroups.stream()
                .filter(Objects::nonNull)
                .map(TestPartGroup::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (TestPartGroup existingPartGroup : safeList(listeningPartGroupService.listAnyByTestId(testId))) {
            if (existingPartGroup == null || existingPartGroup.getId() == null) {
                continue;
            }
            if (!incomingIds.contains(existingPartGroup.getId())) {
                listeningQuestionMapper.softDeleteByPartGroupId(existingPartGroup.getId());
                listeningAudioService.deleteByPartGroupId(existingPartGroup.getId());
                listeningPartGroupService.deleteById(existingPartGroup.getId());
            }
        }

        for (TestPartGroup incomingPartGroup : incomingPartGroups) {
            if (incomingPartGroup == null) {
                continue;
            }
            incomingPartGroup.setTestId(testId);
            incomingPartGroup.setIsDeleted(ListeningConstants.NOT_DELETED);
            TestPartGroup savedPartGroup;
            if (incomingPartGroup.getId() == null) {
                savedPartGroup = listeningPartGroupService.createPartGroup(incomingPartGroup);
            } else {
                TestPartGroup existing = listeningPartGroupService.getAnyById(incomingPartGroup.getId());
                if (existing == null || !Objects.equals(existing.getTestId(), testId)) {
                    throw new RuntimeException("listening_part_group_not_found");
                }
                savedPartGroup = listeningPartGroupService.updatePartGroup(incomingPartGroup.getId(), incomingPartGroup);
                listeningPartGroupService.restoreById(incomingPartGroup.getId());
            }
            replacePartGroupImages(savedPartGroup.getId(), toBizImageResourceDTOList(incomingPartGroup.getImages()));
        }
    }

    private void syncFullQuestions(Long testId, List<ListeningQuestionDTO> incomingQuestions) {
        if (incomingQuestions == null) {
            return;
        }
        Set<Long> incomingIds = incomingQuestions.stream()
                .filter(Objects::nonNull)
                .map(ListeningQuestionDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (ListeningQuestion existingQuestion : safeList(listeningQuestionMapper.findAnyByTestId(testId))) {
            if (existingQuestion == null || existingQuestion.getId() == null) {
                continue;
            }
            if (!incomingIds.contains(existingQuestion.getId())) {
                listeningQuestionMapper.softDeleteById(existingQuestion.getId());
            }
        }

        for (ListeningQuestionDTO questionDTO : incomingQuestions) {
            if (questionDTO == null) {
                continue;
            }
            questionDTO.setTestId(testId);
            validatePartGroup(testId, questionDTO.getPartGroupId());
            if (questionDTO.getId() == null) {
                createQuestion(testId, questionDTO);
            } else {
                ListeningQuestion existing = listeningQuestionMapper.findAnyById(questionDTO.getId());
                if (existing == null || !Objects.equals(existing.getTestId(), testId)) {
                    throw new RuntimeException("listening_question_not_found");
                }
                updateQuestion(questionDTO.getId(), questionDTO);
                listeningQuestionMapper.restoreById(questionDTO.getId());
            }

            Long questionId = questionDTO.getId() == null ? newestQuestionId(testId, questionDTO) : questionDTO.getId();
            if (questionDTO.getImages() != null) {
                replaceQuestionImages(questionId, questionDTO.getImages());
            }
            if (questionDTO.getGroupImages() != null && questionDTO.getPartGroupId() != null) {
                replacePartGroupImages(questionDTO.getPartGroupId(), questionDTO.getGroupImages());
            }
        }
    }

    private void syncFullAudioReferences(Long testId, List<ListeningAudioUpsertDTO> incomingAudios) {
        if (incomingAudios == null) {
            return;
        }
        Set<Long> incomingIds = incomingAudios.stream()
                .filter(Objects::nonNull)
                .map(ListeningAudioUpsertDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (ListeningAudio audio : safeList(listeningAudioService.listByTestId(testId))) {
            if (audio != null && audio.getId() != null && !incomingIds.contains(audio.getId())) {
                listeningAudioService.deleteById(audio.getId());
            }
        }
        for (ListeningAudioUpsertDTO audioDTO : incomingAudios) {
            if (audioDTO == null || audioDTO.getId() == null) {
                continue;
            }
            ListeningAudio audio = listeningAudioService.getById(audioDTO.getId());
            if (audio == null || !Objects.equals(audio.getTestId(), testId)) {
                throw new RuntimeException("listening_audio_not_found");
            }
            if (audioDTO.getTitle() != null || audioDTO.getTranscriptText() != null) {
                listeningAudioService.updateAudioMetadata(audioDTO.getId(), audioDTO.getTitle(), audioDTO.getTranscriptText());
            }
        }
    }

    private Long newestQuestionId(Long testId, ListeningQuestionDTO dto) {
        return safeList(listeningQuestionMapper.findAnyByTestId(testId)).stream()
                .filter(item -> Objects.equals(item.getPartGroupId(), dto.getPartGroupId()))
                .filter(item -> Objects.equals(item.getQuestionNumber(), dto.getQuestionNumber()))
                .map(ListeningQuestion::getId)
                .filter(Objects::nonNull)
                .max(Long::compareTo)
                .orElse(null);
    }

    private void deletePartGroupImages(List<TestPartGroup> partGroups) {
        if (partGroups == null || partGroups.isEmpty()) {
            return;
        }
        for (TestPartGroup partGroup : partGroups) {
            if (partGroup == null || partGroup.getId() == null) {
                continue;
            }
            bizImageResourceService.deleteByTargetAndObjects(
                    ListeningAudioConstants.TARGET_TYPE_LISTENING_PART_GROUP,
                    partGroup.getId(),
                    BucketType.QUESTION_GROUP_IMAGE
            );
        }
    }

    private void deleteListeningQuestionImages(List<ListeningQuestion> questions) {
        if (questions == null || questions.isEmpty()) {
            return;
        }
        for (ListeningQuestion question : questions) {
            if (question == null || question.getId() == null) {
                continue;
            }
            bizImageResourceService.deleteByTargetAndObjects(
                    ListeningAudioConstants.TARGET_TYPE_LISTENING_QUESTION,
                    question.getId(),
                    BucketType.QUESTION_GROUP_IMAGE
            );
        }
    }

    private Map<Long, List<BizImageResource>> findQuestionImageMap(List<ListeningQuestion> questions) {
        List<Long> questionIds = questions == null
                ? new ArrayList<>()
                : questions.stream()
                .filter(Objects::nonNull)
                .map(ListeningQuestion::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (questionIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, List<BizImageResource>> imageMap = bizImageResourceService.listByTargets(
                ListeningAudioConstants.TARGET_TYPE_LISTENING_QUESTION,
                questionIds
        );
        return imageMap == null ? Collections.emptyMap() : imageMap;
    }

    private ListeningQuestionVO toQuestionVO(ListeningQuestion question) {
        ListeningQuestionVO vo = new ListeningQuestionVO();
        vo.setId(question.getId());
        vo.setPartGroupId(question.getPartGroupId());
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
        return vo;
    }

    private String buildDisplayCorrectAnswer(ListeningQuestion question, TestPartGroup partGroup) {
        ListeningGroupAnswerRuleSupport.ResolvedRule resolvedRule =
                listeningGroupAnswerRuleSupport.resolve(question, partGroup);
        String correctAnswer = trimToNull(resolvedRule.getCorrectAnswer());
        return correctAnswer != null ? correctAnswer : trimToNull(resolvedRule.getAcceptedAnswersJson());
    }

    private void validateListeningTestDto(ListeningTestDTO dto) {
        if (dto == null) {
            throw new RuntimeException("listening_test_payload_is_required");
        }
        if (trimToNull(dto.getTitle()) == null) {
            throw new RuntimeException("listening_test_title_is_required");
        }
        requiredPrepSeconds(dto);
        requiredTotalMinutes(dto.getTotalMinutes());
    }

    private void validateListeningQuestionDto(ListeningQuestionDTO dto) {
        if (dto == null) {
            throw new RuntimeException("listening_question_payload_is_required");
        }
        if (dto.getQuestionNumber() == null) {
            throw new RuntimeException("question_number_is_required");
        }
        if (trimToNull(dto.getQuestionType()) == null) {
            throw new RuntimeException("question_type_is_required");
        }
        if (preserveTextOrNull(dto.getQuestionText()) == null) {
            throw new RuntimeException("question_text_is_required");
        }
    }

    private void validatePartGroup(Long testId, Long partGroupId) {
        if (partGroupId != null) {
            TestPartGroup partGroup = requireActivePartGroup(partGroupId);
            if (!Objects.equals(partGroup.getTestId(), testId)) {
                throw new RuntimeException("listening_part_group_does_not_belong_to_test");
            }
        }
    }

    private Integer resolveSectionNumberFromPartGroup(Long testId, ListeningQuestionDTO dto) {
        if (dto == null || dto.getPartGroupId() == null) {
            return defaultInt(dto == null ? null : dto.getSectionNumber(), 1);
        }

        TestPartGroup partGroup = requireActivePartGroup(dto.getPartGroupId());
        if (!Objects.equals(partGroup.getTestId(), testId)) {
            throw new RuntimeException("listening_part_group_does_not_belong_to_test");
        }
        return defaultInt(partGroup.getPartNumber(), defaultInt(dto.getSectionNumber(), 1));
    }

    private ListeningTest requireActiveTest(Long testId) {
        ListeningTest test = listeningTestMapper.findActiveById(testId);
        if (test == null) {
            throw new RuntimeException("listening_test_not_found");
        }
        return test;
    }

    private ListeningQuestion requireActiveQuestion(Long questionId) {
        ListeningQuestion question = listeningQuestionMapper.findActiveById(questionId);
        if (question == null) {
            throw new RuntimeException("listening_question_not_found");
        }
        return question;
    }

    private ListeningAudio requireAudio(Long audioId) {
        ListeningAudio audio = listeningAudioService.getById(audioId);
        if (audio == null) {
            throw new RuntimeException("listening_audio_not_found");
        }
        return audio;
    }

    private TestPartGroup requireActivePartGroup(Long partGroupId) {
        TestPartGroup partGroup = listeningPartGroupService.getActiveById(partGroupId);
        if (partGroup == null) {
            throw new RuntimeException("listening_part_group_not_found");
        }
        return partGroup;
    }

    private String normalizeQuestionType(String questionType) {
        String normalized = ListeningQuestionConstants.normalizeQuestionType(questionType);
        if (normalized == null || !ListeningQuestionConstants.supportsQuestionType(normalized)) {
            throw new RuntimeException("unsupported_question_type");
        }
        return normalized;
    }

    private String resolveAnswerMode(String questionType, String answerMode) {
        String normalizedQuestionType = normalizeQuestionType(questionType);
        String resolved = ListeningQuestionConstants.inferAnswerMode(normalizedQuestionType, answerMode);
        if (!ListeningQuestionConstants.supportsAnswerMode(resolved)) {
            throw new RuntimeException("unsupported_answer_mode");
        }
        return resolved;
    }

    private String resolveTimerMode(String timerMode) {
        String normalized = trimToNull(timerMode);
        return normalized == null ? ListeningConstants.TIMER_MODE_TEST_LEVEL : normalized;
    }

    private Integer requiredPrepMinutes(Integer prepMinutes) {
        if (prepMinutes == null) {
            throw new RuntimeException("prepMinutes is required");
        }
        if (prepMinutes < 0) {
            throw new RuntimeException("prepMinutes cannot be negative");
        }
        return prepMinutes;
    }

    private Integer requiredPrepSeconds(ListeningTestDTO dto) {
        Integer prepSeconds = dto.getPrepSeconds();
        if (prepSeconds != null) {
            if (prepSeconds < 0) {
                throw new RuntimeException("prepSeconds cannot be negative");
            }
            return prepSeconds;
        }
        return minutesToSeconds(requiredPrepMinutes(dto.getPrepMinutes()));
    }

    private Integer requiredTotalMinutes(Integer totalMinutes) {
        if (totalMinutes == null) {
            throw new RuntimeException("totalMinutes is required");
        }
        if (totalMinutes <= 0) {
            throw new RuntimeException("totalMinutes must be greater than 0");
        }
        return totalMinutes;
    }

    private Integer minutesToSeconds(Integer minutes) {
        return minutes == null ? null : minutes * 60;
    }

    private Integer secondsToMinutes(Integer seconds) {
        return seconds == null ? null : seconds / 60;
    }

    private Integer resolvePrepSeconds(ListeningTest test) {
        if (test == null || test.getPrepSeconds() == null || test.getPrepSeconds() < 0) {
            return ListeningConstants.DEFAULT_PREP_SECONDS;
        }
        return test.getPrepSeconds();
    }

    private Integer resolveTotalSeconds(ListeningTest test) {
        if (test == null || test.getTotalSeconds() == null || test.getTotalSeconds() <= 0) {
            return ListeningConstants.DEFAULT_TOTAL_SECONDS;
        }
        return test.getTotalSeconds();
    }

    private Integer resolveAutoSubmit(Integer autoSubmit) {
        return defaultInt(autoSubmit, ListeningConstants.DEFAULT_AUTO_SUBMIT);
    }

    private Integer resolveAllowPause(Integer allowPause) {
        return defaultInt(allowPause, ListeningConstants.DEFAULT_ALLOW_PAUSE);
    }

    private Integer resolveAllowAudioSeek(Integer allowAudioSeek) {
        return defaultInt(allowAudioSeek, ListeningConstants.DEFAULT_ALLOW_AUDIO_SEEK);
    }

    private List<ListeningPartVO> buildPartVOList(List<TestPartGroup> partGroups,
                                                  List<ListeningQuestionVO> questions,
                                                  List<ListeningAudio> audios) {
        List<TestPartGroup> sortedPartGroups = sortPartGroups(partGroups == null ? new ArrayList<>() : partGroups);
        List<ListeningQuestionVO> safeQuestions = questions == null ? new ArrayList<>() : questions;
        List<ListeningAudio> safeAudios = audios == null ? new ArrayList<>() : audios;

        Map<Long, List<ListeningQuestionVO>> questionsByGroup = safeQuestions.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getPartGroupId() != null)
                .collect(Collectors.groupingBy(
                        ListeningQuestionVO::getPartGroupId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        Map<Integer, List<ListeningQuestionVO>> orphanQuestionsByPart = safeQuestions.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getPartGroupId() == null)
                .collect(Collectors.groupingBy(
                        item -> defaultInt(item.getSectionNumber(), 1),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        Map<Long, List<ListeningAudio>> audiosByGroup = safeAudios.stream()
                .filter(Objects::nonNull)
                .filter(item -> ListeningAudioConstants.AUDIO_SCOPE_PART_GROUP.equals(item.getAudioScope()))
                .filter(item -> item.getPartGroupId() != null)
                .collect(Collectors.groupingBy(
                        ListeningAudio::getPartGroupId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        Map<Integer, ListeningPartVO> partMap = new LinkedHashMap<>();
        for (TestPartGroup partGroup : sortedPartGroups) {
            if (partGroup == null) {
                continue;
            }
            Integer partNumber = defaultInt(partGroup.getPartNumber(), 1);
            ListeningPartVO partVO = partMap.computeIfAbsent(partNumber, this::newListeningPartVO);
            if (partVO.getDisplayOrder() == null
                    || (partGroup.getDisplayOrder() != null && partGroup.getDisplayOrder() < partVO.getDisplayOrder())) {
                partVO.setDisplayOrder(partGroup.getDisplayOrder());
            }

            ListeningPartGroupVO groupVO = toPartGroupVO(partGroup);
            groupVO.setImages(toBizImageResourceDTOList(partGroup.getImages()));
            groupVO.setAudios(new ArrayList<>(audiosByGroup.getOrDefault(partGroup.getId(), new ArrayList<>())));
            groupVO.setQuestions(new ArrayList<>(questionsByGroup.getOrDefault(partGroup.getId(), new ArrayList<>())));
            partVO.getGroups().add(groupVO);
        }

        for (Map.Entry<Integer, List<ListeningQuestionVO>> entry : orphanQuestionsByPart.entrySet()) {
            ListeningPartVO partVO = partMap.computeIfAbsent(entry.getKey(), this::newListeningPartVO);
            ListeningPartGroupVO groupVO = new ListeningPartGroupVO();
            groupVO.setPartNumber(entry.getKey());
            groupVO.setGroupNumber(0);
            groupVO.setTitle("Ungrouped");
            groupVO.setDisplayOrder(Integer.MAX_VALUE);
            groupVO.setImages(new ArrayList<>());
            groupVO.setAudios(new ArrayList<>());
            groupVO.setQuestions(new ArrayList<>(entry.getValue()));
            partVO.getGroups().add(groupVO);
        }

        return partMap.values().stream()
                .sorted(Comparator
                        .comparing(ListeningPartVO::getDisplayOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(ListeningPartVO::getPartNumber, Comparator.nullsLast(Integer::compareTo)))
                .peek(part -> part.getGroups().sort(Comparator
                        .comparing(ListeningPartGroupVO::getDisplayOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(ListeningPartGroupVO::getGroupNumber, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(ListeningPartGroupVO::getId, Comparator.nullsLast(Long::compareTo))))
                .collect(Collectors.toList());
    }

    private ListeningPartVO newListeningPartVO(Integer partNumber) {
        ListeningPartVO partVO = new ListeningPartVO();
        partVO.setPartNumber(partNumber);
        partVO.setTitle("Part " + partNumber);
        partVO.setGroups(new ArrayList<>());
        return partVO;
    }

    private ListeningPartGroupVO toPartGroupVO(TestPartGroup partGroup) {
        ListeningPartGroupVO vo = new ListeningPartGroupVO();
        vo.setId(partGroup.getId());
        vo.setTestId(partGroup.getTestId());
        vo.setPartNumber(partGroup.getPartNumber());
        vo.setGroupNumber(partGroup.getGroupNumber());
        vo.setTitle(partGroup.getTitle());
        vo.setInstructionText(partGroup.getInstructionText());
        vo.setGroupGuideText(partGroup.getGroupGuideText());
        vo.setGroupRequirementText(partGroup.getGroupRequirementText());
        vo.setQuestionType(partGroup.getQuestionType());
        vo.setAnswerMode(partGroup.getAnswerMode());
        vo.setOptionsJson(partGroup.getOptionsJson());
        vo.setAcceptedAnswersJson(partGroup.getAcceptedAnswersJson());
        vo.setAnswerRulesJson(partGroup.getAnswerRulesJson());
        vo.setCaseInsensitive(partGroup.getCaseInsensitive());
        vo.setIgnoreWhitespace(partGroup.getIgnoreWhitespace());
        vo.setIgnorePunctuation(partGroup.getIgnorePunctuation());
        vo.setQuestionNoStart(partGroup.getQuestionNoStart());
        vo.setQuestionNoEnd(partGroup.getQuestionNoEnd());
        vo.setDisplayOrder(partGroup.getDisplayOrder());
        vo.setTimeLimitSeconds(partGroup.getTimeLimitSeconds());
        vo.setIsDeleted(partGroup.getIsDeleted());
        return vo;
    }

    private List<TestPartGroup> sortPartGroups(List<TestPartGroup> partGroups) {
        return partGroups.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator
                        .comparing(TestPartGroup::getDisplayOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(TestPartGroup::getPartNumber, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(TestPartGroup::getGroupNumber, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(TestPartGroup::getId, Comparator.nullsLast(Long::compareTo)))
                .collect(Collectors.toList());
    }

    private ListeningAudio findTestAudio(List<ListeningAudio> audios) {
        return audios.stream()
                .filter(Objects::nonNull)
                .filter(item -> ListeningAudioConstants.AUDIO_SCOPE_TEST.equals(item.getAudioScope()))
                .findFirst()
                .orElse(null);
    }

    private List<ListeningAudio> findPartGroupAudios(List<ListeningAudio> audios) {
        return audios.stream()
                .filter(Objects::nonNull)
                .filter(item -> ListeningAudioConstants.AUDIO_SCOPE_PART_GROUP.equals(item.getAudioScope()))
                .collect(Collectors.toList());
    }

    private List<BizImageResourceDTO> toBizImageResourceDTOList(List<BizImageResource> images) {
        if (images == null || images.isEmpty()) {
            return new ArrayList<>();
        }

        return images.stream()
                .filter(Objects::nonNull)
                .map(this::toBizImageResourceDTO)
                .collect(Collectors.toList());
    }

    private BizImageResourceDTO toBizImageResourceDTO(BizImageResource image) {
        BizImageResourceDTO dto = new BizImageResourceDTO();
        dto.setObjectKey(image.getObjectKey());
        dto.setFileUrl(image.getFileUrl());
        dto.setOriginalName(image.getOriginalName());
        dto.setContentType(image.getContentType());
        dto.setFileSize(image.getFileSize());
        dto.setWidth(image.getWidth());
        dto.setHeight(image.getHeight());
        dto.setSortOrder(image.getSortOrder());
        return dto;
    }

    private Integer defaultInt(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
    }

    private <T> List<T> safeList(List<T> source) {
        return source == null ? new ArrayList<>() : source;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String preserveTextOrNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value;
    }
}

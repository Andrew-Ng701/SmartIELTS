package com.andrew.smartielts.reading.service.admin.impl;

import com.andrew.smartielts.common.domain.pojo.TestPartGroup;
import com.andrew.smartielts.common.image.service.BizImageResourceService;
import com.andrew.smartielts.reading.domain.dto.AdminReadingTestFullSaveDTO;
import com.andrew.smartielts.reading.domain.dto.ReadingPassageDTO;
import com.andrew.smartielts.reading.domain.dto.ReadingQuestionDTO;
import com.andrew.smartielts.reading.domain.pojo.ReadingPassage;
import com.andrew.smartielts.reading.domain.pojo.ReadingQuestion;
import com.andrew.smartielts.reading.domain.pojo.ReadingTest;
import com.andrew.smartielts.reading.domain.vo.ReadingTestDetailVO;
import com.andrew.smartielts.reading.mapper.ReadingAnswerRecordMapper;
import com.andrew.smartielts.reading.mapper.ReadingPassageMapper;
import com.andrew.smartielts.reading.mapper.ReadingQuestionMapper;
import com.andrew.smartielts.reading.mapper.ReadingRecordMapper;
import com.andrew.smartielts.reading.mapper.ReadingTestMapper;
import com.andrew.smartielts.reading.service.admin.ReadingPartGroupService;
import com.andrew.smartielts.reading.service.admin.ReadingQuestionAnswerRuleService;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminReadingServiceImplTest {

    @Mock
    private ReadingTestMapper readingTestMapper;

    @Mock
    private ReadingPassageMapper readingPassageMapper;

    @Mock
    private ReadingQuestionMapper readingQuestionMapper;

    @Mock
    private ReadingRecordMapper readingRecordMapper;

    @Mock
    private ReadingAnswerRecordMapper readingAnswerRecordMapper;

    @Mock
    private ReadingPartGroupService readingPartGroupService;

    @Mock
    private ReadingQuestionAnswerRuleService readingQuestionAnswerRuleService;

    @Mock
    private BizImageResourceService bizImageResourceService;

    @Test
    void getTestDetail_shouldExposeTopLevelPassagesWithIds() {
        AdminReadingServiceImpl service = service();
        ReadingTest test = test();
        TestPartGroup group = group();
        ReadingPassage passage = passage(9205L, group.getId());
        ReadingQuestion question = question(300L, passage.getId(), group.getId());

        when(readingTestMapper.findActiveById(1L)).thenReturn(test);
        when(readingPartGroupService.listActiveByTestId(1L)).thenReturn(List.of(group));
        when(readingPassageMapper.findActiveByTestId(1L)).thenReturn(List.of(passage));
        when(readingQuestionMapper.findActiveByPassageId(9205L)).thenReturn(List.of(question));
        when(bizImageResourceService.listByTargets(any(), any())).thenReturn(Collections.emptyMap());
        when(readingQuestionAnswerRuleService.listByQuestionId(300L)).thenReturn(List.of());

        ReadingTestDetailVO result = service.getTestDetail(1L);

        assertNotNull(result.getPassages());
        assertEquals(1, result.getPassages().size());
        assertEquals(9205L, result.getPassages().get(0).getId());
        assertEquals(300L, result.getPassages().get(0).getQuestions().get(0).getId());
    }

    @Test
    void saveFullTest_shouldCreateQuestionsAgainstClientKeyPassage() {
        AdminReadingServiceImpl service = service();
        ReadingTest test = test();
        TestPartGroup group = group();
        AtomicReference<ReadingPassage> savedPassage = new AtomicReference<>();
        AtomicReference<ReadingQuestion> savedQuestion = new AtomicReference<>();

        when(readingTestMapper.findActiveById(1L)).thenReturn(test);
        when(readingPartGroupService.listActiveByTestId(1L)).thenReturn(List.of(group));
        when(readingPartGroupService.getActiveById(group.getId())).thenReturn(group);
        when(readingPassageMapper.findAnyByTestId(1L)).thenReturn(List.of());
        when(readingPassageMapper.findActiveByTestId(1L)).thenAnswer(invocation -> List.of(savedPassage.get()));
        when(readingQuestionMapper.findAnyByPassageId(9205L)).thenReturn(List.of());
        when(readingQuestionMapper.findActiveByPassageId(9205L)).thenAnswer(invocation -> List.of(savedQuestion.get()));
        when(bizImageResourceService.listByTargets(any(), any())).thenReturn(Collections.emptyMap());
        when(readingQuestionAnswerRuleService.listByQuestionId(300L)).thenReturn(List.of());
        doAnswer(invocation -> {
            ReadingPassage passage = invocation.getArgument(0);
            passage.setId(9205L);
            savedPassage.set(passage);
            return null;
        }).when(readingPassageMapper).insertReadingPassage(any(ReadingPassage.class));
        doAnswer(invocation -> {
            ReadingQuestion question = invocation.getArgument(0);
            question.setId(300L);
            savedQuestion.set(question);
            return null;
        }).when(readingQuestionMapper).insertReadingQuestion(any(ReadingQuestion.class));

        AdminReadingTestFullSaveDTO dto = new AdminReadingTestFullSaveDTO();
        ReadingPassageDTO passageDTO = new ReadingPassageDTO();
        passageDTO.setClientKey("p1");
        passageDTO.setPartGroupId(group.getId());
        passageDTO.setPassageNo(1);
        passageDTO.setTitle("Passage");
        passageDTO.setContent("Content");
        dto.setPassages(List.of(passageDTO));

        ReadingQuestionDTO questionDTO = new ReadingQuestionDTO();
        questionDTO.setClientPassageKey("p1");
        questionDTO.setPartGroupId(group.getId());
        questionDTO.setQuestionNumber(1);
        questionDTO.setQuestionType("TRUE_FALSE_NOT_GIVEN");
        String formattedQuestionText = "Challenges faced by RFDS dentists\n"
                + "      need to bring equipment including (1) for records\n\n"
                + "Products supplied by RFDS dentists if necessary RFDS provides:\n"
                + "      (4) and (5) for regular use\n";
        questionDTO.setQuestionText(formattedQuestionText);
        dto.setQuestions(List.of(questionDTO));

        ReadingTestDetailVO result = service.saveFullTest(1L, dto);

        assertEquals(9205L, savedQuestion.get().getPassageId());
        assertEquals(formattedQuestionText, savedQuestion.get().getQuestionText());
        assertEquals(9205L, result.getPassages().get(0).getId());
        assertEquals(9205L, result.getQuestions().get(0).getPassageId());
        assertEquals(formattedQuestionText, result.getQuestions().get(0).getQuestionText());
    }

    @Test
    void saveFullTest_whenQuestionPassageCannotResolve_shouldThrow() {
        AdminReadingServiceImpl service = service();
        when(readingTestMapper.findActiveById(1L)).thenReturn(test());
        when(readingPassageMapper.findAnyByTestId(1L)).thenReturn(List.of());

        AdminReadingTestFullSaveDTO dto = new AdminReadingTestFullSaveDTO();
        ReadingQuestionDTO questionDTO = new ReadingQuestionDTO();
        questionDTO.setClientPassageKey("missing");
        dto.setQuestions(List.of(questionDTO));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.saveFullTest(1L, dto));

        assertEquals("Reading passage not found", ex.getMessage());
    }

    @Test
    void createQuestion_shouldNormalizeMatchingAnswerBankOptionsAndAnswerLabel() {
        AdminReadingServiceImpl service = service();
        TestPartGroup group = group();
        ReadingPassage passage = passage(9205L, group.getId());
        when(readingPassageMapper.findActiveById(9205L)).thenReturn(passage);
        when(readingPartGroupService.getActiveById(group.getId())).thenReturn(group);

        ReadingQuestionDTO dto = new ReadingQuestionDTO();
        dto.setPartGroupId(group.getId());
        dto.setQuestionNumber(19);
        dto.setQuestionType("matching headings");
        dto.setAnswerMode("single");
        dto.setQuestionText("An approach commonly used in language research has to narrow a focus.");
        dto.setCorrectAnswer(" c ");
        dto.setOptionsJson("[{\"label\":\"Z\",\"text\":\"Dr Peter Forster & Dr Alfred Toth\"},"
                + "{\"label\":\"Y\",\"optionText\":\"Dr Merritt Ruhlen\"},"
                + "\"Dr Colin Renfrew\","
                + "{\"value\":\"Dr April McMahon\"}]");

        service.createQuestion(9205L, dto);

        ArgumentCaptor<ReadingQuestion> captor = ArgumentCaptor.forClass(ReadingQuestion.class);
        verify(readingQuestionMapper).insertReadingQuestion(captor.capture());
        ReadingQuestion saved = captor.getValue();
        assertEquals("MATCHING", saved.getQuestionType());
        assertEquals("SINGLE", saved.getAnswerMode());
        assertEquals("C", saved.getCorrectAnswer());
        assertEquals("[{\"label\":\"A\",\"text\":\"Dr Peter Forster & Dr Alfred Toth\"},"
                + "{\"label\":\"B\",\"text\":\"Dr Merritt Ruhlen\"},"
                + "{\"label\":\"C\",\"text\":\"Dr Colin Renfrew\"},"
                + "{\"label\":\"D\",\"text\":\"Dr April McMahon\"}]", saved.getOptionsJson());
    }

    private AdminReadingServiceImpl service() {
        return new AdminReadingServiceImpl(
                readingTestMapper,
                readingPassageMapper,
                readingQuestionMapper,
                readingRecordMapper,
                readingAnswerRecordMapper,
                readingPartGroupService,
                readingQuestionAnswerRuleService,
                bizImageResourceService
        );
    }

    private ReadingTest test() {
        ReadingTest test = new ReadingTest();
        test.setId(1L);
        test.setTitle("Reading Test");
        test.setTotalScore(40);
        return test;
    }

    private TestPartGroup group() {
        TestPartGroup group = new TestPartGroup();
        group.setId(100L);
        group.setTestId(1L);
        group.setPartNumber(1);
        group.setGroupNumber(1);
        group.setDisplayOrder(1);
        return group;
    }

    private ReadingPassage passage(Long id, Long partGroupId) {
        ReadingPassage passage = new ReadingPassage();
        passage.setId(id);
        passage.setTestId(1L);
        passage.setPartGroupId(partGroupId);
        passage.setPassageNo(1);
        passage.setTitle("Passage");
        passage.setContent("Content");
        passage.setDisplayOrder(1);
        return passage;
    }

    private ReadingQuestion question(Long id, Long passageId, Long partGroupId) {
        ReadingQuestion question = new ReadingQuestion();
        question.setId(id);
        question.setPassageId(passageId);
        question.setPartGroupId(partGroupId);
        question.setQuestionNumber(1);
        question.setQuestionType("TRUE_FALSE_NOT_GIVEN");
        question.setQuestionText("Question");
        question.setDisplayOrder(1);
        question.setScore(1);
        return question;
    }
}

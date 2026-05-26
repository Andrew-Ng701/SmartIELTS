package com.andrew.smartielts.exam;

import com.andrew.smartielts.listening.domain.dto.ListeningTestDTO;
import com.andrew.smartielts.listening.domain.pojo.ListeningTest;
import com.andrew.smartielts.listening.domain.vo.ListeningTestDetailVO;
import com.andrew.smartielts.listening.mapper.ListeningAnswerRecordMapper;
import com.andrew.smartielts.listening.mapper.ListeningQuestionMapper;
import com.andrew.smartielts.listening.mapper.ListeningRecordMapper;
import com.andrew.smartielts.listening.mapper.ListeningTestMapper;
import com.andrew.smartielts.listening.service.admin.ListeningAudioService;
import com.andrew.smartielts.listening.service.admin.ListeningPartGroupService;
import com.andrew.smartielts.listening.service.admin.impl.AdminListeningServiceImpl;
import com.andrew.smartielts.listening.support.ListeningGroupAnswerRuleSupport;
import com.andrew.smartielts.common.image.service.BizImageResourceService;
import com.andrew.smartielts.reading.domain.dto.ReadingTestDTO;
import com.andrew.smartielts.reading.domain.pojo.ReadingTest;
import com.andrew.smartielts.reading.mapper.ReadingAnswerRecordMapper;
import com.andrew.smartielts.reading.mapper.ReadingPassageMapper;
import com.andrew.smartielts.reading.mapper.ReadingQuestionMapper;
import com.andrew.smartielts.reading.mapper.ReadingRecordMapper;
import com.andrew.smartielts.reading.mapper.ReadingTestMapper;
import com.andrew.smartielts.reading.service.admin.ReadingPartGroupService;
import com.andrew.smartielts.reading.service.admin.ReadingQuestionAnswerRuleService;
import com.andrew.smartielts.reading.service.admin.impl.AdminReadingServiceImpl;
import com.andrew.smartielts.writing.domain.dto.WritingQuestionDTO;
import com.andrew.smartielts.writing.domain.pojo.WritingQuestion;
import com.andrew.smartielts.writing.domain.vo.WritingQuestionVO;
import com.andrew.smartielts.writing.mapper.WritingQuestionMapper;
import com.andrew.smartielts.writing.mapper.WritingRecordAttachmentMapper;
import com.andrew.smartielts.writing.mapper.WritingRecordMapper;
import com.andrew.smartielts.writing.ai.service.WritingQuestionImageDescriptionService;
import com.andrew.smartielts.writing.service.admin.impl.AdminWritingServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExamTimeSettingsServiceTest {

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
    private ListeningTestMapper listeningTestMapper;
    @Mock
    private ListeningQuestionMapper listeningQuestionMapper;
    @Mock
    private ListeningRecordMapper listeningRecordMapper;
    @Mock
    private ListeningAnswerRecordMapper listeningAnswerRecordMapper;
    @Mock
    private ListeningAudioService listeningAudioService;
    @Mock
    private ListeningPartGroupService listeningPartGroupService;
    @Mock
    private WritingQuestionMapper writingQuestionMapper;
    @Mock
    private WritingRecordMapper writingRecordMapper;
    @Mock
    private WritingRecordAttachmentMapper writingRecordAttachmentMapper;
    @Mock
    private BizImageResourceService bizImageResourceService;
    @Mock
    private WritingQuestionImageDescriptionService writingQuestionImageDescriptionService;

    @Test
    void readingCreate_shouldUsePrepSecondsAndTotalMinutes() {
        AdminReadingServiceImpl service = newAdminReadingService();
        ReadingTestDTO missingPrep = readingDto(null, 60);
        assertThrows(RuntimeException.class, () -> service.createTest(missingPrep));

        ReadingTestDTO dto = readingDto(null, 60);
        dto.setPrepSeconds(0);
        doAnswer(invocation -> {
            ReadingTest test = invocation.getArgument(0);
            test.setId(1L);
            return null;
        }).when(readingTestMapper).insertReadingTest(any(ReadingTest.class));
        when(readingPartGroupService.listAnyByTestId(1L)).thenReturn(List.of());

        ReadingTest created = service.createTest(dto);

        ArgumentCaptor<ReadingTest> captor = ArgumentCaptor.forClass(ReadingTest.class);
        verify(readingTestMapper).insertReadingTest(captor.capture());
        assertEquals(0, captor.getValue().getPrepSeconds());
        assertEquals(3600, captor.getValue().getTotalSeconds());
        assertEquals(0, created.getPrepMinutes());
        assertEquals(60, created.getTotalMinutes());
    }

    @Test
    void listeningCreate_shouldUsePrepSecondsAndTotalMinutes() {
        AdminListeningServiceImpl service = newAdminListeningService();
        ListeningTestDTO missingTotal = listeningDto(1, null);
        assertThrows(RuntimeException.class, () -> service.createTest(missingTotal));

        ListeningTestDTO dto = listeningDto(null, 45);
        dto.setPrepSeconds(0);
        doAnswer(invocation -> {
            ListeningTest test = invocation.getArgument(0);
            test.setId(2L);
            return 1;
        }).when(listeningTestMapper).insertListeningTest(any(ListeningTest.class));
        when(listeningTestMapper.findActiveById(2L)).thenAnswer(invocation -> {
            ListeningTest test = new ListeningTest();
            test.setId(2L);
            test.setTitle("Listening");
            test.setTotalScore(40);
            test.setTimerMode("test_level");
            test.setPrepSeconds(0);
            test.setTotalSeconds(2700);
            test.setAutoSubmit(1);
            test.setAllowPause(1);
            return test;
        });
        when(listeningPartGroupService.listActiveByTestId(2L)).thenReturn(List.of());
        when(listeningQuestionMapper.findActiveByTestId(2L)).thenReturn(List.of());
        when(listeningAudioService.listByTestId(2L)).thenReturn(List.of());

        ListeningTestDetailVO result = service.createTest(dto);

        ArgumentCaptor<ListeningTest> captor = ArgumentCaptor.forClass(ListeningTest.class);
        verify(listeningTestMapper).insertListeningTest(captor.capture());
        assertEquals(0, captor.getValue().getPrepSeconds());
        assertEquals(2700, captor.getValue().getTotalSeconds());
        assertEquals(0, result.getPrepMinutes());
        assertEquals(45, result.getTotalMinutes());
    }

    @Test
    void writingCreate_shouldRequireTimePersistSecondsAndReturnBothUnits() {
        AdminWritingServiceImpl service = newAdminWritingService();
        WritingQuestionDTO invalid = writingDto(-1, 60);
        assertThrows(RuntimeException.class, () -> service.createQuestion(invalid));

        WritingQuestionDTO dto = writingDto(3, 40);
        doAnswer(invocation -> {
            WritingQuestion question = invocation.getArgument(0);
            question.setId(3L);
            return null;
        }).when(writingQuestionMapper).insert(any(WritingQuestion.class));
        when(bizImageResourceService.listByTarget("WRITING_QUESTION", 3L)).thenReturn(List.of());

        WritingQuestionVO result = service.createQuestion(dto);

        ArgumentCaptor<WritingQuestion> captor = ArgumentCaptor.forClass(WritingQuestion.class);
        verify(writingQuestionMapper).insert(captor.capture());
        assertEquals(180, captor.getValue().getPrepSeconds());
        assertEquals(2400, captor.getValue().getTotalSeconds());
        assertEquals(180, result.getPrepSeconds());
        assertEquals(2400, result.getTotalSeconds());
        assertEquals(3, result.getPrepMinutes());
        assertEquals(40, result.getTotalMinutes());
    }

    @Test
    void writingCreate_shouldUsePrepSecondsAndTotalMinutesWhenProvided() {
        AdminWritingServiceImpl service = newAdminWritingService();
        WritingQuestionDTO dto = writingDto(3, 40);
        dto.setPrepSeconds(203);
        doAnswer(invocation -> {
            WritingQuestion question = invocation.getArgument(0);
            question.setId(4L);
            return null;
        }).when(writingQuestionMapper).insert(any(WritingQuestion.class));
        when(bizImageResourceService.listByTarget("WRITING_QUESTION", 4L)).thenReturn(List.of());

        WritingQuestionVO result = service.createQuestion(dto);

        ArgumentCaptor<WritingQuestion> captor = ArgumentCaptor.forClass(WritingQuestion.class);
        verify(writingQuestionMapper).insert(captor.capture());
        assertEquals(203, captor.getValue().getPrepSeconds());
        assertEquals(2400, captor.getValue().getTotalSeconds());
        assertEquals(203, result.getPrepSeconds());
        assertEquals(2400, result.getTotalSeconds());
        assertEquals(3, result.getPrepMinutes());
        assertEquals(40, result.getTotalMinutes());
    }

    private AdminReadingServiceImpl newAdminReadingService() {
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

    private AdminListeningServiceImpl newAdminListeningService() {
        return new AdminListeningServiceImpl(
                listeningTestMapper,
                listeningQuestionMapper,
                listeningRecordMapper,
                listeningAnswerRecordMapper,
                listeningAudioService,
                listeningPartGroupService,
                new ListeningGroupAnswerRuleSupport(),
                bizImageResourceService
        );
    }

    private AdminWritingServiceImpl newAdminWritingService() {
        return new AdminWritingServiceImpl(
                writingQuestionMapper,
                writingRecordMapper,
                writingRecordAttachmentMapper,
                bizImageResourceService,
                writingQuestionImageDescriptionService
        );
    }

    private ReadingTestDTO readingDto(Integer prepMinutes, Integer totalMinutes) {
        ReadingTestDTO dto = new ReadingTestDTO();
        dto.setTitle("Reading");
        dto.setTotalScore(40);
        dto.setTimerMode("test_level");
        dto.setPrepMinutes(prepMinutes);
        dto.setTotalMinutes(totalMinutes);
        return dto;
    }

    private ListeningTestDTO listeningDto(Integer prepMinutes, Integer totalMinutes) {
        ListeningTestDTO dto = new ListeningTestDTO();
        dto.setTitle("Listening");
        dto.setTotalScore(40);
        dto.setTimerMode("test_level");
        dto.setPrepMinutes(prepMinutes);
        dto.setTotalMinutes(totalMinutes);
        return dto;
    }

    private WritingQuestionDTO writingDto(Integer prepMinutes, Integer totalMinutes) {
        WritingQuestionDTO dto = new WritingQuestionDTO();
        dto.setTaskType("TASK1");
        dto.setTitle("Task 1");
        dto.setDescription("Describe the chart.");
        dto.setPrepMinutes(prepMinutes);
        dto.setTotalMinutes(totalMinutes);
        return dto;
    }
}

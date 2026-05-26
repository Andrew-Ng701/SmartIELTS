package com.andrew.smartielts.writing.service.admin.impl;

import com.andrew.smartielts.common.image.domain.pojo.BizImageResource;
import com.andrew.smartielts.common.image.service.BizImageResourceService;
import com.andrew.smartielts.writing.domain.dto.WritingQuestionDTO;
import com.andrew.smartielts.writing.domain.pojo.WritingQuestion;
import com.andrew.smartielts.writing.domain.vo.WritingQuestionVO;
import com.andrew.smartielts.writing.ai.service.WritingQuestionImageDescriptionService;
import com.andrew.smartielts.writing.mapper.WritingQuestionMapper;
import com.andrew.smartielts.writing.mapper.WritingRecordAttachmentMapper;
import com.andrew.smartielts.writing.mapper.WritingRecordMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminWritingServiceImplTest {

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
    void createQuestion_whenDescriptionBlank_shouldCreateWithNullDescription() {
        AdminWritingServiceImpl service = new AdminWritingServiceImpl(
                writingQuestionMapper,
                writingRecordMapper,
                writingRecordAttachmentMapper,
                bizImageResourceService,
                writingQuestionImageDescriptionService
        );
        WritingQuestionDTO dto = new WritingQuestionDTO();
        dto.setTaskType("TASK_2");
        dto.setTitle("Essay task");
        dto.setDescription("   ");
        dto.setPrepSeconds(0);
        dto.setTotalSeconds(2400);

        doAnswer(invocation -> {
            WritingQuestion question = invocation.getArgument(0);
            question.setId(30L);
            return null;
        }).when(writingQuestionMapper).insert(any(WritingQuestion.class));
        when(bizImageResourceService.replaceByTarget("WRITING_QUESTION", 30L, "writing-question", "writing-question-image", null))
                .thenReturn(List.of());
        when(bizImageResourceService.listByTarget("WRITING_QUESTION", 30L)).thenReturn(List.of());

        WritingQuestionVO result = service.createQuestion(dto);

        ArgumentCaptor<WritingQuestion> captor = ArgumentCaptor.forClass(WritingQuestion.class);
        verify(writingQuestionMapper).insert(captor.capture());
        assertNull(captor.getValue().getDescription());
        assertNull(result.getDescription());
    }

    @Test
    void getQuestion_shouldReturnQuestionWithImagesAndTimeFields() {
        AdminWritingServiceImpl service = new AdminWritingServiceImpl(
                writingQuestionMapper,
                writingRecordMapper,
                writingRecordAttachmentMapper,
                bizImageResourceService,
                writingQuestionImageDescriptionService
        );
        WritingQuestion question = new WritingQuestion();
        question.setId(10L);
        question.setTaskType("TASK_1");
        question.setChartType("Line graph");
        question.setTitle("Chart task");
        question.setDescription("Describe the chart");
        question.setPrepSeconds(120);
        question.setTotalSeconds(1800);

        BizImageResource image = new BizImageResource();
        image.setId(20L);
        image.setFileUrl("https://oss.test/writing/chart.png");
        image.setObjectKey("writing/chart.png");
        image.setSortOrder(0);

        when(writingQuestionMapper.findByIdForAdmin(10L)).thenReturn(question);
        when(bizImageResourceService.listByTarget("WRITING_QUESTION", 10L)).thenReturn(List.of(image));

        WritingQuestionVO result = service.getQuestion(10L);

        assertEquals(10L, result.getId());
        assertEquals("Line graph", result.getChartType());
        assertEquals("https://oss.test/writing/chart.png", result.getImageUrl());
        assertEquals("writing/chart.png", result.getImageObjectKey());
        assertEquals(120, result.getPrepSeconds());
        assertEquals(1800, result.getTotalSeconds());
        assertEquals(2, result.getPrepMinutes());
        assertEquals(30, result.getTotalMinutes());
        assertEquals(1, result.getImages().size());
    }

    @Test
    void getQuestion_whenMissing_shouldThrow() {
        AdminWritingServiceImpl service = new AdminWritingServiceImpl(
                writingQuestionMapper,
                writingRecordMapper,
                writingRecordAttachmentMapper,
                bizImageResourceService,
                writingQuestionImageDescriptionService
        );

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.getQuestion(999L));

        assertEquals("Writing question not found", ex.getMessage());
    }
}

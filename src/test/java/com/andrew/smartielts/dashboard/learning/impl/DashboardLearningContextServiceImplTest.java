package com.andrew.smartielts.dashboard.learning.impl;

import com.andrew.smartielts.dashboard.controller.dto.DashboardAskObjectRef;
import com.andrew.smartielts.dashboard.learning.DashboardLearningContextConstants;
import com.andrew.smartielts.dashboard.learning.LearningObjectQueryService;
import com.andrew.smartielts.dashboard.learning.dto.LearningObjectDTO;
import com.andrew.smartielts.dashboard.learning.dto.ModuleLearningContextDTO;
import com.andrew.smartielts.dashboard.learning.dto.UserAttemptDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardLearningContextServiceImplTest {

    @Mock
    private LearningObjectQueryService learningObjectQueryService;

    @InjectMocks
    private DashboardLearningContextServiceImpl service;

    @Test
    void locatesListeningQuestionByNumberAndLoadsRecordQuestionContext() {
        DashboardAskObjectRef objectRef = new DashboardAskObjectRef();
        objectRef.setModule("listening");
        objectRef.setRecordId(9001L);
        objectRef.setQuestionNumber(4);

        when(learningObjectQueryService.locateByQuestionNumber("listening", 2L, 9001L, 4))
                .thenReturn(Map.of(
                        "testId", 101L,
                        "passageId", 201L,
                        "questionId", 301L
                ));

        ModuleLearningContextDTO context = ModuleLearningContextDTO.builder()
                .module("listening")
                .test(LearningObjectDTO.builder().testId(101L).title("Listening Test A").build())
                .passage(LearningObjectDTO.builder().passageId(201L).transcriptText("original listening script").build())
                .question(LearningObjectDTO.builder().questionId(301L).questionText("What is the answer?").build())
                .userAttempt(UserAttemptDTO.builder().recordId(9001L).questionId(301L).userAnswer("B").build())
                .build();
        when(learningObjectQueryService.getListeningContext(2L, 9001L, 301L)).thenReturn(context);
        List<Map<String, Object>> recordQuestions = List.of(
                Map.of(
                        "question_number", 4,
                        "question_text", "What is the answer?",
                        "user_answer", "B",
                        "correct_answer", "A"
                )
        );
        when(learningObjectQueryService.listRecordQuestions("listening", 2L, 9001L)).thenReturn(recordQuestions);

        Map<String, Object> result = service.buildLearningContext(
                "USER",
                2L,
                2L,
                "QUESTION_RESULT_EXPLAIN",
                objectRef
        );

        ArgumentCaptor<Long> questionIdCaptor = ArgumentCaptor.forClass(Long.class);
        verify(learningObjectQueryService).getListeningContext(
                org.mockito.ArgumentMatchers.eq(2L),
                org.mockito.ArgumentMatchers.eq(9001L),
                questionIdCaptor.capture()
        );

        assertEquals(301L, questionIdCaptor.getValue());
        assertEquals(301L, objectRef.getQuestionId());
        assertEquals(201L, objectRef.getPassageId());
        assertEquals(101L, objectRef.getTestId());
        assertSame(context, result.get("moduleContext"));
        assertSame(context.getQuestion(), result.get("question"));
        assertSame(context.getUserAttempt(), result.get("userAttempt"));
        assertSame(recordQuestions, result.get(DashboardLearningContextConstants.CONTEXT_KEY_RECORD_QUESTIONS));
    }
}

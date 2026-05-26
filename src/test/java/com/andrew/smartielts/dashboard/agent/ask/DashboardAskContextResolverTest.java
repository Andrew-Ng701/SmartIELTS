package com.andrew.smartielts.dashboard.agent.ask;

import com.andrew.smartielts.dashboard.controller.dto.DashboardAskObjectRef;
import com.andrew.smartielts.dashboard.controller.dto.DashboardAskRequest;
import com.andrew.smartielts.dashboard.learning.DashboardLearningContextConstants;
import com.andrew.smartielts.dashboard.learning.dto.LearningObjectDTO;
import com.andrew.smartielts.dashboard.learning.dto.ModuleLearningContextDTO;
import com.andrew.smartielts.dashboard.learning.dto.UserAttemptDTO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DashboardAskContextResolverTest {

    private final DashboardAskContextResolver resolver = new DashboardAskContextResolver();

    @Test
    void expandsListeningRecordQuestionContextForAi() {
        DashboardAskObjectRef objectRef = new DashboardAskObjectRef();
        objectRef.setModule("listening");
        objectRef.setObjectType("record");
        objectRef.setRecordId(9001L);
        objectRef.setQuestionId(301L);
        objectRef.setQuestionNumber(4);

        DashboardAskRequest request = new DashboardAskRequest();
        request.setAskScene("QUESTION_RESULT_EXPLAIN");
        request.setObjectRef(objectRef);

        ModuleLearningContextDTO moduleContext = ModuleLearningContextDTO.builder()
                .module("listening")
                .test(LearningObjectDTO.builder()
                        .testId(101L)
                        .title("Listening Test A")
                        .build())
                .passage(LearningObjectDTO.builder()
                        .passageId(201L)
                        .title("Part 1")
                        .passageContent("Listen and answer questions 1-5.")
                        .transcriptText("The speaker says the appointment is on Friday.")
                        .audioUrl("https://example.test/audio.mp3")
                        .build())
                .question(LearningObjectDTO.builder()
                        .questionId(301L)
                        .questionNumber(4)
                        .questionText("When is the appointment?")
                        .questionType("SHORT_ANSWER")
                        .correctAnswer("Friday")
                        .acceptedAnswers(List.of("Friday", "Fri"))
                        .transcriptText("The speaker says the appointment is on Friday.")
                        .audioUrl("https://example.test/audio.mp3")
                        .build())
                .userAttempt(UserAttemptDTO.builder()
                        .recordId(9001L)
                        .questionId(301L)
                        .userAnswer("Monday")
                        .correct(Boolean.FALSE)
                        .score(0)
                        .totalScore(28)
                        .build())
                .build();

        Map<String, Object> learningContext = Map.of(
                DashboardLearningContextConstants.CONTEXT_KEY_MODULE_CONTEXT, moduleContext,
                DashboardLearningContextConstants.CONTEXT_KEY_RECORD_QUESTIONS, List.of(
                        Map.of(
                                "question_number", 4,
                                "question_text", "When is the appointment?",
                                "user_answer", "Monday",
                                "correct_answer", "Friday"
                        )
                )
        );

        Map<String, Object> result = resolver.resolve(request, null, learningContext);

        assertEquals("listening", result.get(DashboardAskContextKeys.CONTEXT_KEY_MODULE));
        assertEquals(9001L, result.get(DashboardAskContextKeys.CONTEXT_KEY_RECORD_ID));
        assertEquals(301L, result.get(DashboardAskContextKeys.CONTEXT_KEY_QUESTION_ID));
        assertEquals(4, result.get(DashboardAskContextKeys.CONTEXT_KEY_QUESTION_NUMBER));
        assertEquals("Listening Test A", result.get(DashboardAskContextKeys.CONTEXT_KEY_TEST_TITLE));
        assertEquals("Part 1", result.get(DashboardAskContextKeys.CONTEXT_KEY_PASSAGE_TITLE));
        assertEquals("When is the appointment?", result.get(DashboardAskContextKeys.CONTEXT_KEY_QUESTION_TEXT));
        assertEquals("Friday", result.get(DashboardAskContextKeys.CONTEXT_KEY_CORRECT_ANSWER));
        assertEquals(List.of("Friday", "Fri"), result.get(DashboardAskContextKeys.CONTEXT_KEY_ACCEPTED_ANSWERS));
        assertEquals("Monday", result.get(DashboardAskContextKeys.CONTEXT_KEY_USER_ANSWER));
        assertEquals("The speaker says the appointment is on Friday.", result.get(DashboardAskContextKeys.CONTEXT_KEY_TRANSCRIPT_TEXT));
        assertEquals("https://example.test/audio.mp3", result.get(DashboardAskContextKeys.CONTEXT_KEY_AUDIO_URL));
        assertEquals(1, ((List<?>) result.get(DashboardAskContextKeys.CONTEXT_KEY_RECORD_QUESTIONS)).size());

        Object questionContext = result.get(DashboardAskContextKeys.CONTEXT_KEY_QUESTION_CONTEXT);
        assertTrue(questionContext instanceof Map<?, ?>);
        assertEquals("When is the appointment?",
                ((Map<?, ?>) questionContext).get(DashboardAskContextKeys.CONTEXT_KEY_QUESTION_TEXT));
        assertEquals(1,
                ((List<?>) ((Map<?, ?>) questionContext).get(DashboardAskContextKeys.CONTEXT_KEY_RECORD_QUESTIONS)).size());
    }
}

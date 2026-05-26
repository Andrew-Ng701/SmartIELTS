package com.andrew.smartielts.dashboard.agent;

import com.andrew.smartielts.dashboard.agent.answer.DashboardAnswerComposeService;
import com.andrew.smartielts.dashboard.agent.answer.dto.DashboardAnswerComposeResult;
import com.andrew.smartielts.dashboard.agent.ask.DashboardAskContextResolver;
import com.andrew.smartielts.dashboard.agent.ask.DashboardAskDecisionService;
import com.andrew.smartielts.dashboard.agent.ask.dto.DashboardAskDecisionResult;
import com.andrew.smartielts.dashboard.agent.ask.dto.DashboardAskDecisionRequest;
import com.andrew.smartielts.dashboard.agent.intent.DashboardIntentPermissionValidator;
import com.andrew.smartielts.dashboard.controller.dto.DashboardAskConversationMessage;
import com.andrew.smartielts.dashboard.controller.dto.DashboardAskObjectRef;
import com.andrew.smartielts.dashboard.controller.dto.DashboardAskRequest;
import com.andrew.smartielts.dashboard.controller.dto.DashboardAssistantResponse;
import com.andrew.smartielts.dashboard.learning.DashboardLearningContextService;
import com.andrew.smartielts.dashboard.preload.DashboardPreloadService;
import com.andrew.smartielts.dashboard.query.DashboardStructuredAiQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.andrew.smartielts.dashboard.agent.ask.DashboardAskConstants.ACTION_DIRECT_ANSWER;
import static com.andrew.smartielts.dashboard.agent.ask.DashboardAskConstants.ACTION_GENERATE_SQL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardIntentExecutionFacadeTest {

    @Mock
    private DashboardAskDecisionService dashboardAskDecisionService;

    @Mock
    private DashboardAskContextResolver dashboardAskContextResolver;

    @Mock
    private DashboardAnswerComposeService dashboardAnswerComposeService;

    @Mock
    private DashboardStructuredAiQueryService dashboardStructuredAiQueryService;

    @Mock
    private DashboardIntentPermissionValidator permissionValidator;

    @Mock
    private DashboardLearningContextService dashboardLearningContextService;

    @Mock
    private DashboardPreloadService dashboardPreloadService;

    @InjectMocks
    private DashboardIntentExecutionFacade facade;

    @Test
    void returnsContextAnswerWhenStructuredQueryFails() {
        DashboardAskRequest request = new DashboardAskRequest();
        request.setQuery("那第 4 題呢？");
        request.setContext(Map.of("timeRange", "30d"));

        DashboardAskConversationMessage previous = new DashboardAskConversationMessage();
        previous.setRole("assistant");
        previous.setContent("第 3 題主要錯在定位句理解。");
        request.setConversationHistory(List.of(previous));

        DashboardAskDecisionResult decision = DashboardAskDecisionResult.builder()
                .action(ACTION_GENERATE_SQL)
                .sufficient(Boolean.FALSE)
                .requiredDataScopes(List.of("STRUCTURED_QUERY_RESULT"))
                .suggestions(List.of("請補充題號"))
                .meta(new LinkedHashMap<>())
                .build();

        when(dashboardAskContextResolver.resolve(eq(request), any(), any()))
                .thenReturn(Map.of("questionText", "Question 4"));
        when(dashboardAskDecisionService.decide(any())).thenReturn(decision);
        when(dashboardStructuredAiQueryService.execute(any(), any(), any(), any(), any(), any()))
                .thenThrow(new IllegalStateException("sql generation failed"));
        when(dashboardAnswerComposeService.compose(any())).thenReturn(DashboardAnswerComposeResult.builder()
                .answer("根據目前上下文，第 4 題需要重新檢查定位句。")
                .suggestions(List.of("檢查第 4 題定位句"))
                .build());

        AtomicReference<String> progressDisplay = new AtomicReference<>();
        DashboardAssistantResponse response = facade.ask(
                "USER",
                2L,
                2L,
                request,
                (displayAnswer, meta) -> progressDisplay.set(displayAnswer)
        );

        assertEquals("我需要再查詢一些 dashboard 資料，確認後再回答。", progressDisplay.get());
        assertEquals("根據目前上下文，第 4 題需要重新檢查定位句。", response.getAnswer());
        assertEquals("FALLBACK_SQL_ERROR_DIRECT", response.getMeta().get("answerMode"));
        assertEquals("sql generation failed", response.getMeta().get("errorMessage"));
        assertNotNull(response.getData());
    }

    @Test
    void continuesWithQuestionContextWhenPreloadFailsBeforeDecision() {
        DashboardAskObjectRef objectRef = new DashboardAskObjectRef();
        objectRef.setModule("listening");
        objectRef.setObjectType("record");
        objectRef.setRecordId(9001L);
        objectRef.setQuestionId(301L);
        objectRef.setQuestionNumber(4);

        DashboardAskRequest request = new DashboardAskRequest();
        request.setQuery("Explain Question 1-5 in this listening record.");
        request.setAskScene("QUESTION_RESULT_EXPLAIN");
        request.setObjectRef(objectRef);

        Map<String, Object> learningContext = Map.of("module", "listening");
        Map<String, Object> resolvedContext = new LinkedHashMap<>();
        resolvedContext.put("module", "listening");
        resolvedContext.put("record_id", 9001L);
        resolvedContext.put("question_id", 301L);
        resolvedContext.put("question_number", 4);
        resolvedContext.put("question_text", "When is the appointment?");
        resolvedContext.put("user_answer", "Monday");
        resolvedContext.put("correct_answer", "Friday");
        resolvedContext.put("record_questions", List.of(
                Map.of(
                        "question_number", 4,
                        "question_text", "When is the appointment?",
                        "user_answer", "Monday",
                        "correct_answer", "Friday"
                )
        ));

        when(dashboardPreloadService.getCached(any(), any(), any(), any(), any(), any()))
                .thenThrow(new IllegalStateException("preload failed"));
        when(dashboardPreloadService.preload(any(), any(), any(), any(), any(), any()))
                .thenThrow(new IllegalStateException("database preload failed"));
        when(dashboardLearningContextService.buildLearningContext("USER", 2L, 2L, "QUESTION_RESULT_EXPLAIN", objectRef))
                .thenReturn(learningContext);
        when(dashboardAskContextResolver.resolve(request, null, learningContext)).thenReturn(resolvedContext);
        when(dashboardAskDecisionService.decide(any())).thenReturn(DashboardAskDecisionResult.builder()
                .action(ACTION_DIRECT_ANSWER)
                .sufficient(Boolean.TRUE)
                .answer("I can explain this item.")
                .suggestions(List.of())
                .meta(new LinkedHashMap<>())
                .build());
        when(dashboardAnswerComposeService.compose(any())).thenReturn(DashboardAnswerComposeResult.builder()
                .answer("The correct answer is Friday because the speaker says the appointment is on Friday.")
                .suggestions(List.of())
                .build());

        DashboardAssistantResponse response = facade.ask("USER", 2L, 2L, request);

        assertEquals("AI_DIRECT", response.getMeta().get("answerMode"));
        assertEquals("preload_error", response.getMeta().get("preloadSource"));
        assertEquals("The correct answer is Friday because the speaker says the appointment is on Friday.",
                response.getAnswer());

        ArgumentCaptor<DashboardAskDecisionRequest> captor =
                ArgumentCaptor.forClass(DashboardAskDecisionRequest.class);
        org.mockito.Mockito.verify(dashboardAskDecisionService).decide(captor.capture());

        Map<String, Object> questionContext = captor.getValue().getQuestionContext();
        assertEquals("When is the appointment?", questionContext.get("question_text"));
        assertEquals("Monday", questionContext.get("user_answer"));
        assertEquals("Friday", questionContext.get("correct_answer"));
        assertEquals(1, ((List<?>) questionContext.get("record_questions")).size());
    }

    @Test
    void carriesObjectRefFromConversationHistoryForFollowUpQuestion() {
        DashboardAskConversationMessage previous = new DashboardAskConversationMessage();
        previous.setRole("assistant");
        previous.setContent("I can review Question 6 with the backend context.");
        previous.setMeta(Map.of(
                "data", Map.of(
                        "objectRef", Map.of(
                                "module", "listening",
                                "objectType", "record",
                                "recordId", 9001L,
                                "questionId", 306L,
                                "questionNumber", 6
                        )
                )
        ));

        DashboardAskRequest request = new DashboardAskRequest();
        request.setQuery("Explain why this answer was wrong and what the correct reasoning should be.");
        request.setAskScene("QUESTION_RESULT_EXPLAIN");
        request.setConversationHistory(List.of(previous));

        Map<String, Object> learningContext = Map.of("module", "listening");
        Map<String, Object> resolvedContext = new LinkedHashMap<>();
        resolvedContext.put("module", "listening");
        resolvedContext.put("record_id", 9001L);
        resolvedContext.put("question_id", 306L);
        resolvedContext.put("question_number", 6);
        resolvedContext.put("question_text", "Which room should the student go to?");
        resolvedContext.put("user_answer", "Room 12");
        resolvedContext.put("correct_answer", "Room 20");

        when(dashboardAskContextResolver.resolve(eq(request), any(), eq(learningContext))).thenReturn(resolvedContext);
        when(dashboardLearningContextService.buildLearningContext(eq("USER"), eq(2L), eq(2L), eq("QUESTION_RESULT_EXPLAIN"), any()))
                .thenReturn(learningContext);
        when(dashboardAskDecisionService.decide(any())).thenReturn(DashboardAskDecisionResult.builder()
                .action(ACTION_DIRECT_ANSWER)
                .sufficient(Boolean.TRUE)
                .answer("I can explain this item.")
                .suggestions(List.of("Show me the evidence in the source material"))
                .meta(new LinkedHashMap<>())
                .build());
        when(dashboardAnswerComposeService.compose(any())).thenReturn(DashboardAnswerComposeResult.builder()
                .answer("Room 20 is correct because the source points to that room.")
                .suggestions(List.of("Give me one similar listening practice question"))
                .build());

        DashboardAssistantResponse response = facade.ask("USER", 2L, 2L, request);

        ArgumentCaptor<DashboardAskObjectRef> objectRefCaptor = ArgumentCaptor.forClass(DashboardAskObjectRef.class);
        org.mockito.Mockito.verify(dashboardLearningContextService)
                .buildLearningContext(eq("USER"), eq(2L), eq(2L), eq("QUESTION_RESULT_EXPLAIN"), objectRefCaptor.capture());

        DashboardAskObjectRef carriedObjectRef = objectRefCaptor.getValue();
        assertEquals("listening", carriedObjectRef.getModule());
        assertEquals(9001L, carriedObjectRef.getRecordId());
        assertEquals(306L, carriedObjectRef.getQuestionId());
        assertEquals(6, carriedObjectRef.getQuestionNumber());
        assertEquals(Boolean.TRUE, response.getMeta().get("contextCarriedFromHistory"));
    }

    @Test
    void returnsEnglishUserFocusedSuggestionsWhenUnhandledAskFails() {
        DashboardAskRequest request = new DashboardAskRequest();
        request.setQuery("Explain Question 6.");

        when(dashboardAskContextResolver.resolve(eq(request), any(), any())).thenReturn(Map.of());
        when(dashboardAskDecisionService.decide(any())).thenThrow(new IllegalStateException("decision failed"));

        DashboardAssistantResponse response = facade.ask("USER", 2L, 2L, request);

        assertEquals("ASK_ERROR_DIRECT", response.getMeta().get("answerMode"));
        assertEquals(List.of(
                "Explain the current question using my saved answer and the correct answer",
                "Show me the evidence in the source material for this answer",
                "Give me one focused practice step for this question type"
        ), response.getSuggestions());
    }

    @Test
    void sendsResolvedListeningQuestionAndRecordContextToAiDecision() {
        DashboardAskObjectRef objectRef = new DashboardAskObjectRef();
        objectRef.setModule("listening");
        objectRef.setObjectType("record");
        objectRef.setRecordId(9001L);
        objectRef.setQuestionId(301L);
        objectRef.setQuestionNumber(4);

        DashboardAskRequest request = new DashboardAskRequest();
        request.setQuery("Explain this listening question.");
        request.setAskScene("QUESTION_RESULT_EXPLAIN");
        request.setObjectRef(objectRef);

        Map<String, Object> learningContext = Map.of("module", "listening");
        Map<String, Object> resolvedContext = new LinkedHashMap<>();
        resolvedContext.put("module", "listening");
        resolvedContext.put("record_id", 9001L);
        resolvedContext.put("question_id", 301L);
        resolvedContext.put("question_number", 4);
        resolvedContext.put("question_text", "When is the appointment?");
        resolvedContext.put("transcript_text", "The speaker says the appointment is on Friday.");
        resolvedContext.put("user_answer", "Monday");
        resolvedContext.put("correct_answer", "Friday");
        resolvedContext.put("accepted_answers", List.of("Friday", "Fri"));
        resolvedContext.put("record_questions", List.of(
                Map.of(
                        "question_number", 4,
                        "question_text", "When is the appointment?",
                        "user_answer", "Monday",
                        "correct_answer", "Friday"
                )
        ));

        when(dashboardLearningContextService.buildLearningContext("USER", 2L, 2L, "QUESTION_RESULT_EXPLAIN", objectRef))
                .thenReturn(learningContext);
        when(dashboardAskContextResolver.resolve(request, null, learningContext)).thenReturn(resolvedContext);
        when(dashboardAskDecisionService.decide(any())).thenReturn(DashboardAskDecisionResult.builder()
                .action(ACTION_DIRECT_ANSWER)
                .sufficient(Boolean.TRUE)
                .answer("I can explain this item.")
                .suggestions(List.of())
                .meta(new LinkedHashMap<>())
                .build());
        when(dashboardAnswerComposeService.compose(any())).thenReturn(DashboardAnswerComposeResult.builder()
                .answer("The correct answer is Friday because the speaker says the appointment is on Friday.")
                .suggestions(List.of())
                .build());

        facade.ask("USER", 2L, 2L, request);

        ArgumentCaptor<DashboardAskDecisionRequest> captor =
                ArgumentCaptor.forClass(DashboardAskDecisionRequest.class);
        org.mockito.Mockito.verify(dashboardAskDecisionService).decide(captor.capture());

        Map<String, Object> questionContext = captor.getValue().getQuestionContext();
        assertEquals("When is the appointment?", questionContext.get("question_text"));
        assertEquals("The speaker says the appointment is on Friday.", questionContext.get("transcript_text"));
        assertEquals("Monday", questionContext.get("user_answer"));
        assertEquals("Friday", questionContext.get("correct_answer"));
        assertEquals(List.of("Friday", "Fri"), questionContext.get("accepted_answers"));
        assertEquals(1, ((List<?>) questionContext.get("record_questions")).size());
    }
}

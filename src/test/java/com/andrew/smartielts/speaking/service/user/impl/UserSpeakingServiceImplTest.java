package com.andrew.smartielts.speaking.service.user.impl;

import com.andrew.smartielts.speaking.ai.SpeakingScoreAiProperties;
import com.andrew.smartielts.speaking.ai.dto.SpeakingEvaluationResult;
import com.andrew.smartielts.speaking.ai.dto.SpeakingFinalEvaluationResult;
import com.andrew.smartielts.speaking.ai.service.SpeakingFinalEvaluationService;
import com.andrew.smartielts.speaking.ai.service.SpeakingScoreAiService;
import com.andrew.smartielts.speaking.aliyun.AliyunBailianAsrClient;
import com.andrew.smartielts.speaking.did.service.DidSpeakingService;
import com.andrew.smartielts.speaking.domain.model.ExamStep;
import com.andrew.smartielts.speaking.domain.pojo.SpeakingQuestion;
import com.andrew.smartielts.speaking.domain.pojo.SpeakingRecord;
import com.andrew.smartielts.speaking.domain.pojo.SpeakingSession;
import com.andrew.smartielts.speaking.domain.vo.SubmitAnswerVO;
import com.andrew.smartielts.speaking.domain.vo.UploadSpeakingAudioVO;
import com.andrew.smartielts.speaking.mapper.SpeakingMapper;
import com.andrew.smartielts.speaking.mapper.SpeakingRecordMapper;
import com.andrew.smartielts.speaking.mapper.SpeakingSessionMapper;
import com.andrew.smartielts.speaking.mapper.SpeakingTalkMapper;
import com.andrew.smartielts.speaking.oss.service.SpeakingAudioStorageService;
import com.andrew.smartielts.utils.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSpeakingServiceImplTest {

    @Mock
    private SpeakingMapper speakingMapper;

    @Mock
    private SpeakingRecordMapper speakingRecordMapper;

    @Mock
    private SpeakingSessionMapper speakingSessionMapper;

    @Mock
    private SpeakingTalkMapper speakingTalkMapper;

    @Mock
    private DidSpeakingService didSpeakingService;

    @Mock
    private SpeakingExamPlanner speakingExamPlanner;

    @Mock
    private SpeakingScriptBuilder speakingScriptBuilder;

    @Mock
    private SpeakingAudioStorageService speakingAudioStorageService;

    @Mock
    private AliyunBailianAsrClient aliyunBailianAsrClient;

    @Mock
    private SpeakingScoreAiService speakingScoreAiService;

    @Mock
    private SpeakingFinalEvaluationService speakingFinalEvaluationService;

    @Mock
    private SpeakingScoreAiProperties speakingScoreAiProperties;

    @Mock
    private Executor speakingScoringExecutor;

    @InjectMocks
    private UserSpeakingServiceImpl userSpeakingService;

    private SpeakingSession session;
    private SpeakingRecord r1;
    private SpeakingRecord r2;
    private SpeakingQuestion q1;
    private SpeakingQuestion q2;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        lenient().when(speakingScoreAiProperties.getPerQuestionModelOrDefault()).thenReturn("qwen3-omni-flash");

        session = new SpeakingSession();
        session.setId(1L);
        session.setSessionId("sess-000001");
        session.setUserId(100L);
        session.setTotalQuestions(2);
        session.setCurrentIndex(2);
        session.setExamStatus("WAITING_FINAL_EVALUATION");

        r1 = new SpeakingRecord();
        r1.setId(11L);
        r1.setSessionId("sess-000001");
        r1.setQuestionId(101L);
        r1.setAnswerStatus("SCORED");
        r1.setTranscript("I enjoy reading books and learning languages.");
        r1.setFluencyAndCoherence(new BigDecimal("6.5"));
        r1.setLexicalResource(new BigDecimal("6.0"));
        r1.setGrammaticalRangeAndAccuracy(new BigDecimal("6.0"));
        r1.setPronunciation(new BigDecimal("6.5"));
        r1.setOverallScore(new BigDecimal("6.5"));
        r1.setFeedback("Fairly clear response.");

        r2 = new SpeakingRecord();
        r2.setId(12L);
        r2.setSessionId("sess-000001");
        r2.setQuestionId(102L);
        r2.setAnswerStatus("SCORED");
        r2.setTranscript("Technology helps people communicate more efficiently.");
        r2.setFluencyAndCoherence(new BigDecimal("7.0"));
        r2.setLexicalResource(new BigDecimal("6.5"));
        r2.setGrammaticalRangeAndAccuracy(new BigDecimal("6.5"));
        r2.setPronunciation(new BigDecimal("7.0"));
        r2.setOverallScore(new BigDecimal("6.5"));
        r2.setFeedback("Reasonably well-developed answer.");

        q1 = new SpeakingQuestion();
        q1.setId(101L);
        q1.setPart("PART1");
        q1.setQuestionText("Do you enjoy reading?");
        q1.setCueCard(null);

        q2 = new SpeakingQuestion();
        q2.setId(102L);
        q2.setPart("PART3");
        q2.setQuestionText("How does technology affect communication?");
        q2.setCueCard(null);
    }

    @Test
    void finalizeSessionEvaluation_success_shouldWriteSessionFinalScoresAndCompletedStatus() {
        when(speakingSessionMapper.findBySessionId("sess-000001")).thenReturn(session);
        when(speakingRecordMapper.findBySessionId("sess-000001")).thenReturn(List.of(r1, r2));
        when(speakingMapper.findAnyById(101L)).thenReturn(q1);
        when(speakingMapper.findAnyById(102L)).thenReturn(q2);

        SpeakingFinalEvaluationResult aiResult = new SpeakingFinalEvaluationResult();
        aiResult.setFluencyAndCoherence(new BigDecimal("6.5"));
        aiResult.setLexicalResource(new BigDecimal("6.5"));
        aiResult.setGrammaticalRangeAndAccuracy(new BigDecimal("6.0"));
        aiResult.setPronunciation(new BigDecimal("7.0"));
        aiResult.setOverallScore(new BigDecimal("6.5"));
        aiResult.setFeedback("The candidate demonstrates a reasonably effective speaking performance across the full session.");

        when(speakingFinalEvaluationService.evaluateFinal(
                eq("sess-000001"),
                anyMap(),
                anyList(),
                eq(new BigDecimal("6.8")),
                eq(new BigDecimal("6.3")),
                eq(new BigDecimal("6.3")),
                eq(new BigDecimal("6.8")),
                eq(new BigDecimal("6.6"))
        )).thenReturn(aiResult);

        userSpeakingService.finalizeSessionEvaluation("sess-000001");

        ArgumentCaptor<SpeakingSession> sessionCaptor = ArgumentCaptor.forClass(SpeakingSession.class);
        verify(speakingSessionMapper).updateSpeakingSession(sessionCaptor.capture());

        SpeakingSession updated = sessionCaptor.getValue();
        assertEquals("COMPLETED", updated.getExamStatus());
        assertEquals(new BigDecimal("6.5"), updated.getFluencyAndCoherence());
        assertEquals(new BigDecimal("6.5"), updated.getLexicalResource());
        assertEquals(new BigDecimal("6.0"), updated.getGrammaticalRangeAndAccuracy());
        assertEquals(new BigDecimal("7.0"), updated.getPronunciation());
        assertEquals(new BigDecimal("6.5"), updated.getOverallScore());
        assertEquals("The candidate demonstrates a reasonably effective speaking performance across the full session.", updated.getFinalFeedback());
        assertNotNull(updated.getCompletedTime());
        assertNotNull(updated.getUpdatedTime());
    }

    @Test
    void finalizeSessionEvaluation_whenFinalAiFails_shouldFallbackToAggregatedResult() {
        when(speakingSessionMapper.findBySessionId("sess-000001")).thenReturn(session);
        when(speakingRecordMapper.findBySessionId("sess-000001")).thenReturn(List.of(r1, r2));
        when(speakingMapper.findAnyById(101L)).thenReturn(q1);
        when(speakingMapper.findAnyById(102L)).thenReturn(q2);

        when(speakingFinalEvaluationService.evaluateFinal(
                anyString(), anyMap(), anyList(), any(), any(), any(), any(), any()
        )).thenThrow(new RuntimeException("AI timeout"));

        userSpeakingService.finalizeSessionEvaluation("sess-000001");

        ArgumentCaptor<SpeakingSession> sessionCaptor = ArgumentCaptor.forClass(SpeakingSession.class);
        verify(speakingSessionMapper).updateSpeakingSession(sessionCaptor.capture());

        SpeakingSession updated = sessionCaptor.getValue();
        assertEquals("COMPLETED", updated.getExamStatus());
        assertEquals(new BigDecimal("6.8"), updated.getFluencyAndCoherence());
        assertEquals(new BigDecimal("6.3"), updated.getLexicalResource());
        assertEquals(new BigDecimal("6.3"), updated.getGrammaticalRangeAndAccuracy());
        assertEquals(new BigDecimal("6.8"), updated.getPronunciation());
        assertEquals(new BigDecimal("6.6"), updated.getOverallScore());
        assertNotNull(updated.getFinalFeedback());
        assertFalse(updated.getFinalFeedback().isBlank());
    }

    @Test
    void finalizeSessionEvaluation_whenNotAllRecordsScored_shouldThrowException() {
        r2.setAnswerStatus("FAILED");

        when(speakingSessionMapper.findBySessionId("sess-000001")).thenReturn(session);
        when(speakingRecordMapper.findBySessionId("sess-000001")).thenReturn(List.of(r1, r2));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userSpeakingService.finalizeSessionEvaluation("sess-000001"));

        assertEquals("Not all questions are scored yet", ex.getMessage());
        verify(speakingSessionMapper, never()).updateSpeakingSession(any());
    }

    @Test
    void submitAnswer_shouldPersistProcessingRecordAndQueueAsyncScoring() throws Exception {
        String sessionId = "sess-000001";
        Long userId = 100L;
        Long questionId = 101L;

        SpeakingSession activeSession = new SpeakingSession();
        activeSession.setId(1L);
        activeSession.setSessionId(sessionId);
        activeSession.setUserId(userId);
        activeSession.setTotalQuestions(2);
        activeSession.setCurrentIndex(0);
        activeSession.setExamStatus("STARTED");
        activeSession.setExamPlanJson(objectMapper.writeValueAsString(List.of(
                buildStep("PART1", questionId),
                buildStep("PART3", 102L)
        )));

        SpeakingQuestion question = new SpeakingQuestion();
        question.setId(questionId);
        question.setPart("PART1");
        question.setQuestionText("Do you enjoy reading?");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "answer.mp3",
                "audio/mpeg",
                "fake".getBytes()
        );

        UploadSpeakingAudioVO uploadVo = new UploadSpeakingAudioVO();
        uploadVo.setAudioUrl("https://oss.example.com/answer.mp3");

        when(speakingSessionMapper.findBySessionId(sessionId)).thenReturn(activeSession);
        when(speakingMapper.findById(questionId)).thenReturn(question);
        when(speakingRecordMapper.findBySessionIdAndQuestionId(sessionId, questionId)).thenReturn(null);
        when(speakingAudioStorageService.uploadAudio(file, userId, sessionId, questionId)).thenReturn(uploadVo);
        doAnswer(invocation -> {
            SpeakingRecord inserted = invocation.getArgument(0);
            inserted.setId(501L);
            return null;
        }).when(speakingRecordMapper).insertSpeakingRecord(any(SpeakingRecord.class));

        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

            SubmitAnswerVO result = userSpeakingService.submitAnswer(sessionId, questionId, file);

            assertEquals(501L, result.getRecordId());
            assertEquals("PROCESSING", result.getAnswerStatus());
            assertEquals("PROCESSING", result.getAiStatus());
            assertEquals("https://oss.example.com/answer.mp3", result.getAudioUrl());
            verify(speakingScoringExecutor).execute(any(Runnable.class));

            ArgumentCaptor<SpeakingRecord> recordCaptor = ArgumentCaptor.forClass(SpeakingRecord.class);
            verify(speakingRecordMapper).insertSpeakingRecord(recordCaptor.capture());
            SpeakingRecord inserted = recordCaptor.getValue();
            assertEquals("PROCESSING", inserted.getAnswerStatus());
            assertEquals("PROCESSING", inserted.getAiStatus());
            assertNull(inserted.getOverallScore());

            ArgumentCaptor<SpeakingSession> sessionCaptor = ArgumentCaptor.forClass(SpeakingSession.class);
            verify(speakingSessionMapper).updateSpeakingSession(sessionCaptor.capture());
            assertEquals(1, sessionCaptor.getValue().getCurrentIndex());
            assertEquals("IN_PROGRESS", sessionCaptor.getValue().getExamStatus());
        }
    }

    @Test
    void processSubmittedAnswer_success_shouldScoreRecordAndTriggerFinalWhenComplete() {
        SpeakingRecord record = new SpeakingRecord();
        record.setId(501L);
        record.setSessionId("sess-000001");
        record.setQuestionId(101L);
        record.setAudioUrl("https://oss.example.com/answer.mp3");
        record.setAnswerStatus("PROCESSING");

        SpeakingQuestion question = new SpeakingQuestion();
        question.setId(101L);
        question.setPart("PART1");
        question.setQuestionText("Do you enjoy reading?");

        SpeakingEvaluationResult evaluation = new SpeakingEvaluationResult();
        evaluation.setFluencyAndCoherence(new BigDecimal("6.5"));
        evaluation.setLexicalResource(new BigDecimal("6.0"));
        evaluation.setGrammaticalRangeAndAccuracy(new BigDecimal("6.0"));
        evaluation.setPronunciation(new BigDecimal("6.5"));
        evaluation.setOverallScore(new BigDecimal("6.5"));
        evaluation.setFeedback("Clear and relevant.");

        session.setTotalQuestions(1);
        session.setCurrentIndex(1);

        when(speakingRecordMapper.findAnyById(501L)).thenReturn(record);
        when(speakingMapper.findAnyById(101L)).thenReturn(question);
        when(aliyunBailianAsrClient.transcribe("https://oss.example.com/answer.mp3"))
                .thenReturn("Yes, I enjoy reading.");
        when(speakingScoreAiService.evaluate(
                eq("PART1"),
                eq("Do you enjoy reading?"),
                isNull(),
                eq("Yes, I enjoy reading."),
                eq("https://oss.example.com/answer.mp3")
        )).thenReturn(evaluation);
        when(speakingSessionMapper.findBySessionId("sess-000001")).thenReturn(session);
        when(speakingRecordMapper.findBySessionId("sess-000001")).thenReturn(List.of(record));

        SpeakingFinalEvaluationResult finalResult = new SpeakingFinalEvaluationResult();
        finalResult.setFluencyAndCoherence(new BigDecimal("6.5"));
        finalResult.setLexicalResource(new BigDecimal("6.0"));
        finalResult.setGrammaticalRangeAndAccuracy(new BigDecimal("6.0"));
        finalResult.setPronunciation(new BigDecimal("6.5"));
        finalResult.setOverallScore(new BigDecimal("6.5"));
        finalResult.setFeedback("Final feedback.");
        when(speakingFinalEvaluationService.evaluateFinal(
                anyString(), anyMap(), anyList(), any(), any(), any(), any(), any()
        )).thenReturn(finalResult);

        userSpeakingService.processSubmittedAnswer(501L, "https://oss.example.com/answer.mp3");

        ArgumentCaptor<SpeakingRecord> recordCaptor = ArgumentCaptor.forClass(SpeakingRecord.class);
        verify(speakingRecordMapper).updateSpeakingRecord(recordCaptor.capture());
        SpeakingRecord scored = recordCaptor.getValue();
        assertEquals("SCORED", scored.getAnswerStatus());
        assertEquals("SCORED", scored.getAiStatus());
        assertEquals(new BigDecimal("6.5"), scored.getOverallScore());
        assertEquals("Yes, I enjoy reading.", scored.getTranscript());

        ArgumentCaptor<SpeakingSession> sessionCaptor = ArgumentCaptor.forClass(SpeakingSession.class);
        verify(speakingSessionMapper, atLeastOnce()).updateSpeakingSession(sessionCaptor.capture());
        assertEquals("COMPLETED", sessionCaptor.getAllValues().get(sessionCaptor.getAllValues().size() - 1).getExamStatus());
    }

    @Test
    void averageScore_shouldRoundHalfUpToOneDecimal() throws Exception {
        Method method = UserSpeakingServiceImpl.class.getDeclaredMethod("averageScore", List.class);
        method.setAccessible(true);

        BigDecimal result = (BigDecimal) method.invoke(
                userSpeakingService,
                List.of(new BigDecimal("6.25"), new BigDecimal("6.25"))
        );

        assertEquals(new BigDecimal("6.3"), result);
    }

    private ExamStep buildStep(String stepType, Long questionId) {
        ExamStep step = new ExamStep();
        step.setStepType(stepType);
        step.setPart(stepType);
        step.setQuestionId(questionId);
        step.setTopicKey("topic-1");
        return step;
    }
}

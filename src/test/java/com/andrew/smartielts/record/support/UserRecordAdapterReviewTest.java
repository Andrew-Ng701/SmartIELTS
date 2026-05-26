package com.andrew.smartielts.record.support;

import com.andrew.smartielts.common.image.domain.pojo.BizImageResource;
import com.andrew.smartielts.listening.domain.vo.ListeningAnswerResultVO;
import com.andrew.smartielts.listening.domain.vo.ListeningRecordDetailVO;
import com.andrew.smartielts.reading.domain.vo.ReadingAnswerResultVO;
import com.andrew.smartielts.reading.domain.vo.ReadingRecordDetailVO;
import com.andrew.smartielts.record.domain.vo.UserRecordDetailVO;
import com.andrew.smartielts.record.domain.vo.review.RecordReviewVO;
import com.andrew.smartielts.speaking.domain.vo.SpeakingRecordDetailVO;
import com.andrew.smartielts.speaking.domain.vo.SpeakingRecordVO;
import com.andrew.smartielts.speaking.domain.vo.SpeakingSessionSummaryVO;
import com.andrew.smartielts.speaking.service.user.UserSpeakingService;
import com.andrew.smartielts.writing.domain.vo.WritingAttachmentVO;
import com.andrew.smartielts.writing.domain.vo.WritingRecordDetailVO;
import com.andrew.smartielts.writing.service.user.UserWritingService;
import com.andrew.smartielts.reading.service.user.UserReadingService;
import com.andrew.smartielts.listening.service.user.UserListeningService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserRecordAdapterReviewTest {

    private final RecordReviewBuilder recordReviewBuilder = new RecordReviewBuilder();

    @Test
    void readingGetRecord_shouldKeepDetailAndBuildQuestionReviews() {
        UserReadingService readingService = mock(UserReadingService.class);
        ReadingRecordDetailVO detail = new ReadingRecordDetailVO();
        detail.setRecordId(100L);
        detail.setTestId(10L);
        detail.setTestTitle("Reading Test");
        detail.setTotalScore(1);
        detail.setCreatedTime(LocalDateTime.now());
        ReadingAnswerResultVO answer = new ReadingAnswerResultVO();
        answer.setQuestionId(1000L);
        answer.setQuestionText("Question text");
        answer.setUserAnswer("user");
        answer.setCorrectAnswer("answer");
        answer.setIsCorrect(1);
        answer.setScore(1);
        detail.setAnswers(List.of(answer));
        when(readingService.getRecord(100L, 2L)).thenReturn(detail);
        ReadingUserRecordAdapter adapter = new ReadingUserRecordAdapter(readingService, recordReviewBuilder);

        UserRecordDetailVO result = adapter.getRecord(2L, 100L);

        assertSame(detail, result.getDetail());
        RecordReviewVO review = (RecordReviewVO) result.getReview();
        assertEquals("EXAM_PAGE", review.getLayoutType());
        assertEquals("user", review.getExamPageReview().getQuestionReviews().get(0).getUserAnswer());
        assertEquals("answer", review.getExamPageReview().getQuestionReviews().get(0).getCorrectAnswer());
    }

    @Test
    void listeningGetRecord_shouldKeepDetailAndBuildQuestionReviews() {
        UserListeningService listeningService = mock(UserListeningService.class);
        ListeningRecordDetailVO detail = new ListeningRecordDetailVO();
        detail.setRecordId(101L);
        detail.setTestId(11L);
        detail.setTestTitle("Listening Test");
        detail.setTotalScore(1);
        detail.setCreatedTime(LocalDateTime.now());
        ListeningAnswerResultVO answer = new ListeningAnswerResultVO();
        answer.setQuestionId(1001L);
        answer.setQuestionNumber(1);
        answer.setQuestionText("Question text");
        answer.setUserAnswer("typed");
        answer.setCorrectAnswer("correct");
        answer.setIsCorrect(1);
        answer.setScore(1);
        detail.setAnswers(List.of(answer));
        when(listeningService.getRecord(101L, 2L)).thenReturn(detail);
        ListeningUserRecordAdapter adapter = new ListeningUserRecordAdapter(listeningService, recordReviewBuilder);

        UserRecordDetailVO result = adapter.getRecord(2L, 101L);

        assertSame(detail, result.getDetail());
        RecordReviewVO review = (RecordReviewVO) result.getReview();
        assertEquals("EXAM_PAGE", review.getLayoutType());
        assertEquals(1, review.getExamPageReview().getQuestionReviews().get(0).getQuestionNumber());
        assertEquals("typed", review.getExamPageReview().getQuestionReviews().get(0).getUserAnswer());
    }

    @Test
    void writingGetRecord_whenOcrResultExists_shouldUseExtractedTextAsAnswerText() {
        UserWritingService writingService = mock(UserWritingService.class);
        WritingRecordDetailVO detail = new WritingRecordDetailVO();
        detail.setRecordId(102L);
        detail.setQuestionId(12L);
        detail.setQuestionTitle("Writing Task");
        detail.setQuestionImageUrl("https://oss.test/question.png");
        detail.setInputType("PDF");
        detail.setPrompt("Analyze the chart.");
        detail.setTextContent("raw typed text");
        detail.setExtractedText("final ocr text");
        detail.setAiScore(new BigDecimal("7.0"));
        detail.setCreatedTime(LocalDateTime.now());
        BizImageResource questionImage = new BizImageResource();
        questionImage.setFileUrl("https://oss.test/question.png");
        WritingAttachmentVO attachment = new WritingAttachmentVO();
        attachment.setFileType("PDF");
        attachment.setFileUrl("https://oss.test/answer.pdf");
        attachment.setOcrText("final ocr text");
        detail.setQuestionImages(List.of(questionImage));
        detail.setAttachments(List.of(attachment));
        when(writingService.getRecord(102L, 2L)).thenReturn(detail);
        WritingUserRecordAdapter adapter = new WritingUserRecordAdapter(writingService, recordReviewBuilder);

        UserRecordDetailVO result = adapter.getRecord(2L, 102L);

        assertSame(detail, result.getDetail());
        RecordReviewVO review = (RecordReviewVO) result.getReview();
        assertEquals("WRITING_REVIEW", review.getLayoutType());
        assertEquals("final ocr text", review.getWritingReview().getAnswerText());
        assertEquals("OCR", review.getWritingReview().getAnswerSource());
        assertEquals("Analyze the chart.", review.getWritingReview().getPrompt());
        assertEquals("https://oss.test/question.png", review.getWritingReview().getQuestionImageUrl());
        assertSame(questionImage, review.getWritingReview().getQuestionImages().get(0));
        assertSame(attachment, review.getWritingReview().getAttachments().get(0));
    }

    @Test
    void speakingGetRecord_shouldReturnWholeSessionReview() {
        UserSpeakingService speakingService = mock(UserSpeakingService.class);
        SpeakingRecordDetailVO current = new SpeakingRecordDetailVO();
        current.setRecordId(103L);
        current.setSessionId("session-1");
        current.setQuestionText("Current question");
        current.setCreatedTime(LocalDateTime.now());
        SpeakingRecordVO record = new SpeakingRecordVO();
        record.setId(103L);
        record.setQuestionId(13L);
        record.setPart("PART1");
        record.setQuestionText("Question text");
        record.setAudioUrl("https://oss.test/audio.mp3");
        record.setTranscript("spoken answer");
        record.setRelevanceComment("reference comment");
        record.setQualityComment("quality comment");
        record.setOverallScore(new BigDecimal("6.5"));
        SpeakingSessionSummaryVO summary = new SpeakingSessionSummaryVO();
        summary.setSessionId("session-1");
        summary.setExamStatus("COMPLETED");
        summary.setOverallScore(new BigDecimal("6.5"));
        summary.setRecords(List.of(record));
        when(speakingService.getRecord(103L, 2L)).thenReturn(current);
        when(speakingService.getSessionSummary("session-1", 2L)).thenReturn(summary);
        SpeakingUserRecordAdapter adapter = new SpeakingUserRecordAdapter(speakingService, recordReviewBuilder);

        UserRecordDetailVO result = adapter.getRecord(2L, 103L);

        assertSame(current, result.getDetail());
        RecordReviewVO review = (RecordReviewVO) result.getReview();
        assertEquals("SPEAKING_SESSION_REVIEW", review.getLayoutType());
        assertEquals("session-1", review.getSpeakingSessionReview().getSessionId());
        assertEquals("spoken answer", review.getSpeakingSessionReview().getConversations().get(0).getTranscript());
        assertEquals("reference comment",
                review.getSpeakingSessionReview().getConversations().get(0).getRelevanceComment());
        assertEquals("quality comment",
                review.getSpeakingSessionReview().getConversations().get(0).getQualityComment());
        verify(speakingService).getSessionSummary("session-1", 2L);
    }
}

package com.andrew.smartielts.record.support;

import com.andrew.smartielts.listening.domain.vo.ListeningAnswerResultVO;
import com.andrew.smartielts.listening.domain.vo.ListeningRecordDetailVO;
import com.andrew.smartielts.reading.domain.vo.ReadingAnswerResultVO;
import com.andrew.smartielts.reading.domain.vo.ReadingRecordDetailVO;
import com.andrew.smartielts.record.constants.UserRecordModuleConstants;
import com.andrew.smartielts.record.domain.vo.review.ExamPageReviewVO;
import com.andrew.smartielts.record.domain.vo.review.QuestionReviewVO;
import com.andrew.smartielts.record.domain.vo.review.RecordReviewVO;
import com.andrew.smartielts.record.domain.vo.review.SpeakingConversationReviewVO;
import com.andrew.smartielts.record.domain.vo.review.SpeakingSessionReviewVO;
import com.andrew.smartielts.record.domain.vo.review.WritingReviewVO;
import com.andrew.smartielts.speaking.domain.vo.SpeakingRecordDetailVO;
import com.andrew.smartielts.speaking.domain.vo.SpeakingRecordVO;
import com.andrew.smartielts.speaking.domain.vo.SpeakingSessionSummaryVO;
import com.andrew.smartielts.writing.domain.vo.WritingRecordDetailVO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class RecordReviewBuilder {

    private static final String LAYOUT_EXAM_PAGE = "EXAM_PAGE";
    private static final String LAYOUT_WRITING = "WRITING_REVIEW";
    private static final String LAYOUT_SPEAKING_SESSION = "SPEAKING_SESSION_REVIEW";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_DELETED = "DELETED";
    private static final String ANSWER_SOURCE_TEXT = "TEXT";
    private static final String ANSWER_SOURCE_OCR = "OCR";

    public RecordReviewVO buildReading(Long userId, ReadingRecordDetailVO detail) {
        RecordReviewVO review = base(UserRecordModuleConstants.READING, userId, detail.getRecordId());
        review.setLayoutType(LAYOUT_EXAM_PAGE);
        review.setTitle(detail.getTestTitle());
        review.setScore(toBigDecimal(detail.getTotalScore()));
        review.setScoreText(detail.getTotalScore() == null ? null : String.valueOf(detail.getTotalScore()));
        review.setStatus(STATUS_COMPLETED);
        review.setCreatedTime(detail.getCreatedTime());

        ExamPageReviewVO examPageReview = new ExamPageReviewVO();
        examPageReview.setTestId(detail.getTestId());
        examPageReview.setTestTitle(detail.getTestTitle());
        examPageReview.setTotalScore(detail.getTotalScore());
        examPageReview.setParts(detail.getParts());
        examPageReview.setQuestions(detail.getQuestions());
        examPageReview.setAnswers(detail.getAnswers());
        examPageReview.setQuestionReviews(buildReadingQuestionReviews(detail.getAnswers()));
        review.setExamPageReview(examPageReview);
        return review;
    }

    public RecordReviewVO buildListening(Long userId, ListeningRecordDetailVO detail) {
        RecordReviewVO review = base(UserRecordModuleConstants.LISTENING, userId, detail.getRecordId());
        review.setLayoutType(LAYOUT_EXAM_PAGE);
        review.setTitle(detail.getTestTitle());
        review.setScore(toBigDecimal(detail.getTotalScore()));
        review.setScoreText(detail.getTotalScore() == null ? null : String.valueOf(detail.getTotalScore()));
        review.setStatus(STATUS_COMPLETED);
        review.setCreatedTime(detail.getCreatedTime());

        ExamPageReviewVO examPageReview = new ExamPageReviewVO();
        examPageReview.setTestId(detail.getTestId());
        examPageReview.setTestTitle(detail.getTestTitle());
        examPageReview.setTotalScore(detail.getTotalScore());
        examPageReview.setTestAudio(detail.getTestAudio());
        examPageReview.setAllowAudioSeek(detail.getAllowAudioSeek());
        examPageReview.setParts(detail.getParts());
        examPageReview.setPartGroupAudios(detail.getPartGroupAudios());
        examPageReview.setQuestions(detail.getQuestions());
        examPageReview.setAnswers(detail.getAnswers());
        examPageReview.setQuestionReviews(buildListeningQuestionReviews(detail.getAnswers()));
        review.setExamPageReview(examPageReview);
        return review;
    }

    public RecordReviewVO buildWriting(Long userId, WritingRecordDetailVO detail) {
        RecordReviewVO review = base(UserRecordModuleConstants.WRITING, userId, detail.getRecordId());
        review.setLayoutType(LAYOUT_WRITING);
        review.setTitle(detail.getQuestionTitle());
        review.setScore(detail.getAiScore());
        review.setScoreText(detail.getAiScore() == null ? null : detail.getAiScore().toPlainString());
        review.setStatus(detail.getAiStatus());
        review.setCreatedTime(detail.getCreatedTime());

        WritingReviewVO writingReview = new WritingReviewVO();
        writingReview.setQuestionId(detail.getQuestionId());
        writingReview.setQuestionTitle(detail.getQuestionTitle());
        writingReview.setQuestionDescription(detail.getQuestionDescription());
        writingReview.setPrompt(detail.getPrompt() == null ? detail.getQuestionDescription() : detail.getPrompt());
        writingReview.setImageDetailDescription(detail.getImageDetailDescription());
        writingReview.setQuestionImageUrl(detail.getQuestionImageUrl());
        writingReview.setQuestionImages(detail.getQuestionImages());
        writingReview.setPreviewAssets(detail.getPreviewAssets());
        writingReview.setTaskType(detail.getTaskType());
        writingReview.setInputType(detail.getInputType());
        writingReview.setTextContent(detail.getTextContent());
        writingReview.setExtractedText(detail.getExtractedText());
        writingReview.setAnswerText(resolveWritingAnswerText(detail));
        writingReview.setAnswerSource(resolveWritingAnswerSource(detail));
        writingReview.setAnswerPreview(detail.getAnswerPreview());
        writingReview.setAttachmentCount(detail.getAttachmentCount());
        writingReview.setAttachments(detail.getAttachments());
        writingReview.setTargetScore(detail.getTargetScore());
        writingReview.setAiScore(detail.getAiScore());
        writingReview.setAiFeedback(detail.getAiFeedback());
        writingReview.setAiStatus(detail.getAiStatus());
        writingReview.setAiProvider(detail.getAiProvider());
        writingReview.setAiModel(detail.getAiModel());
        review.setWritingReview(writingReview);
        return review;
    }

    public RecordReviewVO buildSpeaking(Long userId,
                                        SpeakingRecordDetailVO currentRecord,
                                        SpeakingSessionSummaryVO summary) {
        RecordReviewVO review = base(UserRecordModuleConstants.SPEAKING, userId, currentRecord.getRecordId());
        review.setLayoutType(LAYOUT_SPEAKING_SESSION);
        review.setTitle(currentRecord.getQuestionText());
        review.setScore(summary.getOverallScore());
        review.setScoreText(summary.getOverallScore() == null ? null : summary.getOverallScore().toPlainString());
        review.setStatus(summary.getExamStatus());
        review.setCreatedTime(currentRecord.getCreatedTime());

        SpeakingSessionReviewVO speakingReview = new SpeakingSessionReviewVO();
        speakingReview.setSessionId(summary.getSessionId());
        speakingReview.setExamStatus(summary.getExamStatus());
        speakingReview.setTotalQuestions(summary.getTotalQuestions());
        speakingReview.setAnsweredCount(summary.getAnsweredCount());
        speakingReview.setProcessingCount(summary.getProcessingCount());
        speakingReview.setScoredCount(summary.getScoredCount());
        speakingReview.setFailedCount(summary.getFailedCount());
        speakingReview.setFluencyAndCoherence(summary.getFluencyAndCoherence());
        speakingReview.setLexicalResource(summary.getLexicalResource());
        speakingReview.setGrammaticalRangeAndAccuracy(summary.getGrammaticalRangeAndAccuracy());
        speakingReview.setPronunciation(summary.getPronunciation());
        speakingReview.setOverallScore(summary.getOverallScore());
        speakingReview.setFeedback(summary.getFeedback());
        speakingReview.setConversations(buildSpeakingConversations(summary.getRecords()));
        review.setSpeakingSessionReview(speakingReview);
        return review;
    }

    private RecordReviewVO base(String moduleType, Long userId, Long recordId) {
        RecordReviewVO review = new RecordReviewVO();
        review.setModuleType(moduleType);
        review.setUserId(userId);
        review.setRecordId(recordId);
        return review;
    }

    private List<QuestionReviewVO> buildReadingQuestionReviews(List<ReadingAnswerResultVO> answers) {
        if (answers == null) {
            return List.of();
        }
        List<QuestionReviewVO> reviews = new ArrayList<>();
        for (ReadingAnswerResultVO answer : answers) {
            QuestionReviewVO review = new QuestionReviewVO();
            review.setQuestionId(answer.getQuestionId());
            review.setQuestionType(answer.getQuestionType());
            review.setAnswerMode(answer.getAnswerMode());
            review.setQuestionText(answer.getQuestionText());
            review.setPrompt(answer.getQuestionText());
            review.setOptionsJson(answer.getOptionsJson());
            review.setUserAnswer(answer.getUserAnswer());
            review.setCorrectAnswer(answer.getCorrectAnswer());
            review.setIsCorrect(answer.getIsCorrect());
            review.setScore(answer.getScore());
            reviews.add(review);
        }
        return reviews;
    }

    private List<QuestionReviewVO> buildListeningQuestionReviews(List<ListeningAnswerResultVO> answers) {
        if (answers == null) {
            return List.of();
        }
        List<QuestionReviewVO> reviews = new ArrayList<>();
        for (ListeningAnswerResultVO answer : answers) {
            QuestionReviewVO review = new QuestionReviewVO();
            review.setQuestionId(answer.getQuestionId());
            review.setQuestionNumber(answer.getQuestionNumber());
            review.setQuestionType(answer.getQuestionType());
            review.setAnswerMode(answer.getAnswerMode());
            review.setQuestionText(answer.getQuestionText());
            review.setPrompt(answer.getQuestionText());
            review.setOptionsJson(answer.getOptionsJson());
            review.setUserAnswer(answer.getUserAnswer());
            review.setCorrectAnswer(answer.getCorrectAnswer());
            review.setIsCorrect(answer.getIsCorrect());
            review.setScore(answer.getScore());
            reviews.add(review);
        }
        return reviews;
    }

    private List<SpeakingConversationReviewVO> buildSpeakingConversations(List<SpeakingRecordVO> records) {
        if (records == null) {
            return List.of();
        }
        List<SpeakingConversationReviewVO> conversations = new ArrayList<>();
        for (SpeakingRecordVO record : records) {
            SpeakingConversationReviewVO conversation = new SpeakingConversationReviewVO();
            conversation.setRecordId(record.getId());
            conversation.setQuestionId(record.getQuestionId());
            conversation.setPart(record.getPart());
            conversation.setQuestionText(record.getQuestionText());
            conversation.setPrompt(record.getPrompt() == null ? record.getQuestionText() : record.getPrompt());
            conversation.setCueCard(record.getCueCard());
            conversation.setAudioUrl(record.getAudioUrl());
            conversation.setTranscript(record.getTranscript());
            conversation.setFluencyAndCoherence(record.getFluencyAndCoherence());
            conversation.setLexicalResource(record.getLexicalResource());
            conversation.setGrammaticalRangeAndAccuracy(record.getGrammaticalRangeAndAccuracy());
            conversation.setPronunciation(record.getPronunciation());
            conversation.setOverallScore(record.getOverallScore());
            conversation.setFeedback(record.getFeedback());
            conversation.setRelevanceComment(record.getRelevanceComment());
            conversation.setQualityComment(record.getQualityComment());
            conversation.setAnswerStatus(record.getAnswerStatus());
            conversation.setAiStatus(record.getAiStatus());
            conversation.setAiProvider(record.getAiProvider());
            conversation.setAiModel(record.getAiModel());
            conversation.setAiErrorMessage(record.getAiErrorMessage());
            conversation.setIsDeleted(record.getIsDeleted());
            conversation.setDeletedTime(record.getDeletedTime());
            conversation.setCreatedTime(record.getCreatedTime());
            conversations.add(conversation);
        }
        return conversations;
    }

    private String resolveWritingAnswerText(WritingRecordDetailVO detail) {
        if (hasText(detail.getExtractedText())) {
            return detail.getExtractedText();
        }
        return detail.getTextContent();
    }

    private String resolveWritingAnswerSource(WritingRecordDetailVO detail) {
        if (hasText(detail.getExtractedText())) {
            return ANSWER_SOURCE_OCR;
        }
        return ANSWER_SOURCE_TEXT;
    }

    private boolean hasText(String text) {
        return text != null && !text.isBlank();
    }

    private BigDecimal toBigDecimal(Integer value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }
}

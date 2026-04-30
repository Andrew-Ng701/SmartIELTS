package com.andrew.smartielts.listening.support;

import com.andrew.smartielts.common.domain.pojo.QuestionAnswerRule;
import com.andrew.smartielts.common.support.QuestionAnswerRuleJudgeSupport;
import com.andrew.smartielts.listening.domain.pojo.ListeningAnswerRecord;
import com.andrew.smartielts.listening.domain.pojo.ListeningQuestion;
import com.andrew.smartielts.listening.mapper.ListeningAnswerRecordMapper;
import com.andrew.smartielts.listening.mapper.ListeningQuestionAnswerRuleMapper;
import com.andrew.smartielts.listening.mapper.ListeningQuestionMapper;
import com.andrew.smartielts.listening.mapper.ListeningRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class ListeningAnswerRecordBuilder {

    private final ListeningQuestionMapper listeningQuestionMapper;
    private final ListeningQuestionAnswerRuleMapper listeningQuestionAnswerRuleMapper;
    private final ListeningAnswerRecordMapper listeningAnswerRecordMapper;
    private final ListeningRecordMapper listeningRecordMapper;
    private final QuestionAnswerRuleJudgeSupport judgeSupport;

    public ListeningAnswerRecordBuilder(ListeningQuestionMapper listeningQuestionMapper,
                                        ListeningQuestionAnswerRuleMapper listeningQuestionAnswerRuleMapper,
                                        ListeningAnswerRecordMapper listeningAnswerRecordMapper,
                                        ListeningRecordMapper listeningRecordMapper,
                                        QuestionAnswerRuleJudgeSupport judgeSupport) {
        this.listeningQuestionMapper = listeningQuestionMapper;
        this.listeningQuestionAnswerRuleMapper = listeningQuestionAnswerRuleMapper;
        this.listeningAnswerRecordMapper = listeningAnswerRecordMapper;
        this.listeningRecordMapper = listeningRecordMapper;
        this.judgeSupport = judgeSupport;
    }

    @Transactional
    public BuildResult persist(Long recordId, Long testId, Map<Long, List<String>> answerMap) {
        List<ListeningQuestion> questions = listeningQuestionMapper.findActiveByTestId(testId);
        if (questions == null || questions.isEmpty()) {
            listeningRecordMapper.updateTotalScore(recordId, 0);
            return new BuildResult(recordId, 0, Collections.emptyList());
        }

        int totalScore = 0;
        List<QuestionResult> questionResults = new ArrayList<>();

        for (ListeningQuestion question : questions) {
            List<QuestionAnswerRule> rules = listeningQuestionAnswerRuleMapper.findByQuestionId(question.getId());

            List<String> rawAnswers = extractAnswers(answerMap, question.getId());

            QuestionAnswerRuleJudgeSupport.GradeResult gradeResult = judgeSupport.grade(
                    rawAnswers,
                    question.getAnswerMode(),
                    question.getCorrectAnswer(),
                    question.getAcceptedAnswersJson(),
                    question.getCaseInsensitive(),
                    question.getIgnoreWhitespace(),
                    question.getIgnorePunctuation(),
                    rules,
                    question.getScore()
            );

            ListeningAnswerRecord answerRecord = new ListeningAnswerRecord();
            answerRecord.setRecordId(recordId);
            answerRecord.setQuestionId(question.getId());
            answerRecord.setPartGroupId(question.getPartGroupId());
            answerRecord.setUserAnswer(gradeResult.getStoredUserAnswer());
            answerRecord.setNormalizedAnswer(gradeResult.getNormalizedUserAnswer());
            answerRecord.setRawAnswersJson(gradeResult.getRawAnswersJson());
            answerRecord.setIsCorrect(gradeResult.isCorrect() ? 1 : 0);
            answerRecord.setScore(gradeResult.getEarnedScore());

            listeningAnswerRecordMapper.insertListeningAnswerRecord(answerRecord);

            totalScore += gradeResult.getEarnedScore();

            questionResults.add(new QuestionResult(
                    question.getId(),
                    question.getQuestionNumber(),
                    gradeResult.isCorrect(),
                    gradeResult.getEarnedScore(),
                    gradeResult.getStoredUserAnswer(),
                    gradeResult.getDisplayCorrectAnswer()
            ));
        }

        listeningRecordMapper.updateTotalScore(recordId, totalScore);
        return new BuildResult(recordId, totalScore, questionResults);
    }

    public Map<Long, List<String>> normalizeSubmitMap(Map<Long, List<String>> source) {
        return source == null ? Collections.emptyMap() : source;
    }

    private List<String> extractAnswers(Map<Long, List<String>> answerMap, Long questionId) {
        if (answerMap == null || questionId == null) {
            return Collections.singletonList("");
        }
        List<String> answers = answerMap.get(questionId);
        if (answers == null || answers.isEmpty()) {
            return Collections.singletonList("");
        }
        return answers;
    }

    public static class BuildResult {
        private final Long recordId;
        private final int totalScore;
        private final List<QuestionResult> questionResults;

        public BuildResult(Long recordId, int totalScore, List<QuestionResult> questionResults) {
            this.recordId = recordId;
            this.totalScore = totalScore;
            this.questionResults = questionResults == null
                    ? Collections.emptyList()
                    : Collections.unmodifiableList(new ArrayList<>(questionResults));
        }

        public Long getRecordId() {
            return recordId;
        }

        public int getTotalScore() {
            return totalScore;
        }

        public List<QuestionResult> getQuestionResults() {
            return questionResults;
        }
    }

    public static class QuestionResult {
        private final Long questionId;
        private final Integer questionNumber;
        private final boolean correct;
        private final int earnedScore;
        private final String userAnswer;
        private final String correctAnswerDisplay;

        public QuestionResult(Long questionId,
                              Integer questionNumber,
                              boolean correct,
                              int earnedScore,
                              String userAnswer,
                              String correctAnswerDisplay) {
            this.questionId = questionId;
            this.questionNumber = questionNumber;
            this.correct = correct;
            this.earnedScore = earnedScore;
            this.userAnswer = userAnswer;
            this.correctAnswerDisplay = correctAnswerDisplay;
        }

        public Long getQuestionId() {
            return questionId;
        }

        public Integer getQuestionNumber() {
            return questionNumber;
        }

        public boolean isCorrect() {
            return correct;
        }

        public int getEarnedScore() {
            return earnedScore;
        }

        public String getUserAnswer() {
            return userAnswer;
        }

        public String getCorrectAnswerDisplay() {
            return correctAnswerDisplay;
        }
    }
}
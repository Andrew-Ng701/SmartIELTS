package com.andrew.smartielts.reading.support;

import com.andrew.smartielts.common.domain.pojo.QuestionAnswerRule;
import com.andrew.smartielts.common.support.QuestionAnswerRuleJudgeSupport;
import com.andrew.smartielts.reading.domain.pojo.ReadingAnswerRecord;
import com.andrew.smartielts.reading.domain.pojo.ReadingPassage;
import com.andrew.smartielts.reading.domain.pojo.ReadingQuestion;
import com.andrew.smartielts.reading.mapper.ReadingAnswerRecordMapper;
import com.andrew.smartielts.reading.mapper.ReadingPassageMapper;
import com.andrew.smartielts.reading.mapper.ReadingQuestionAnswerRuleMapper;
import com.andrew.smartielts.reading.mapper.ReadingQuestionMapper;
import com.andrew.smartielts.reading.mapper.ReadingRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ReadingAnswerRecordBuilder {

    private final ReadingPassageMapper reading_passage_mapper;
    private final ReadingQuestionMapper reading_question_mapper;
    private final ReadingQuestionAnswerRuleMapper reading_question_answer_rule_mapper;
    private final ReadingAnswerRecordMapper reading_answer_record_mapper;
    private final ReadingRecordMapper reading_record_mapper;
    private final QuestionAnswerRuleJudgeSupport judge_support;

    public ReadingAnswerRecordBuilder(ReadingPassageMapper reading_passage_mapper,
                                      ReadingQuestionMapper reading_question_mapper,
                                      ReadingQuestionAnswerRuleMapper reading_question_answer_rule_mapper,
                                      ReadingAnswerRecordMapper reading_answer_record_mapper,
                                      ReadingRecordMapper reading_record_mapper,
                                      QuestionAnswerRuleJudgeSupport judge_support) {
        this.reading_passage_mapper = reading_passage_mapper;
        this.reading_question_mapper = reading_question_mapper;
        this.reading_question_answer_rule_mapper = reading_question_answer_rule_mapper;
        this.reading_answer_record_mapper = reading_answer_record_mapper;
        this.reading_record_mapper = reading_record_mapper;
        this.judge_support = judge_support;
    }

    @Transactional
    public BuildResult persist(Long record_id, Long test_id, Map<Long, List<String>> answer_map) {
        List<ReadingPassage> passages = reading_passage_mapper.findActiveByTestId(test_id);
        if (passages == null || passages.isEmpty()) {
            reading_record_mapper.updateTotalScore(record_id, 0);
            return new BuildResult(record_id, 0, Collections.emptyList());
        }

        passages.sort(Comparator
                .comparing(ReadingPassage::getPassageNo, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(ReadingPassage::getDisplayOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(ReadingPassage::getId, Comparator.nullsLast(Long::compareTo)));

        Map<Long, List<String>> normalized_answer_map = normalize_submit_map(answer_map);

        int total_score = 0;
        List<QuestionResult> question_results = new ArrayList<>();

        for (ReadingPassage passage : passages) {
            if (passage == null || passage.getId() == null) {
                continue;
            }

            List<ReadingQuestion> questions = reading_question_mapper.findActiveByPassageId(passage.getId());
            if (questions == null || questions.isEmpty()) {
                continue;
            }

            questions.sort(Comparator
                    .comparing(ReadingQuestion::getDisplayOrder, Comparator.nullsLast(Integer::compareTo))
                    .thenComparing(ReadingQuestion::getQuestionNumber, Comparator.nullsLast(Integer::compareTo))
                    .thenComparing(ReadingQuestion::getId, Comparator.nullsLast(Long::compareTo)));

            for (ReadingQuestion question : questions) {
                if (question == null || question.getId() == null) {
                    continue;
                }

                List<QuestionAnswerRule> rules =
                        reading_question_answer_rule_mapper.findByQuestionId(question.getId());

                List<String> raw_answers = extract_answers(normalized_answer_map, question.getId());

                QuestionAnswerRuleJudgeSupport.GradeResult grade_result = judge_support.grade(
                        raw_answers,
                        question.getAnswerMode(),
                        question.getCorrectAnswer(),
                        question.getAcceptedAnswersJson(),
                        question.getCaseInsensitive(),
                        question.getIgnoreWhitespace(),
                        question.getIgnorePunctuation(),
                        rules,
                        question.getScore()
                );

                ReadingAnswerRecord answer_record = new ReadingAnswerRecord();
                answer_record.setRecordId(record_id);
                answer_record.setQuestionId(question.getId());
                answer_record.setPartGroupId(question.getPartGroupId());
                answer_record.setUserAnswer(grade_result.getStoredUserAnswer());
                answer_record.setNormalizedAnswer(grade_result.getNormalizedUserAnswer());
                answer_record.setRawAnswersJson(grade_result.getRawAnswersJson());
                answer_record.setIsCorrect(grade_result.isCorrect() ? 1 : 0);
                answer_record.setScore(grade_result.getEarnedScore());
                reading_answer_record_mapper.insertReadingAnswerRecord(answer_record);

                total_score += grade_result.getEarnedScore();

                question_results.add(new QuestionResult(
                        passage.getId(),
                        question.getId(),
                        question.getQuestionNumber(),
                        grade_result.isCorrect(),
                        grade_result.getEarnedScore(),
                        grade_result.getStoredUserAnswer(),
                        grade_result.getDisplayCorrectAnswer()
                ));
            }
        }

        reading_record_mapper.updateTotalScore(record_id, total_score);
        return new BuildResult(record_id, total_score, question_results);
    }

    public Map<Long, List<String>> normalizeSubmitMap(Map<Long, List<String>> source) {
        return normalize_submit_map(source);
    }

    private Map<Long, List<String>> normalize_submit_map(Map<Long, List<String>> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, List<String>> result = new LinkedHashMap<>();
        for (Map.Entry<Long, List<String>> entry : source.entrySet()) {
            if (entry == null || entry.getKey() == null) {
                continue;
            }
            result.put(entry.getKey(), normalize_raw_list(entry.getValue()));
        }
        return result;
    }

    private List<String> extract_answers(Map<Long, List<String>> answer_map, Long question_id) {
        if (answer_map == null || answer_map.isEmpty() || question_id == null) {
            return new ArrayList<>();
        }
        List<String> answers = answer_map.get(question_id);
        return normalize_raw_list(answers);
    }

    private List<String> normalize_raw_list(List<String> raw_list) {
        List<String> result = new ArrayList<>();
        if (raw_list == null || raw_list.isEmpty()) {
            return result;
        }

        for (String item : raw_list) {
            String value = trim_to_null(item);
            if (value != null) {
                result.add(value);
            }
        }
        return result;
    }

    private String trim_to_null(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static class BuildResult {
        private final Long record_id;
        private final int total_score;
        private final List<QuestionResult> question_results;

        public BuildResult(Long record_id, int total_score, List<QuestionResult> question_results) {
            this.record_id = record_id;
            this.total_score = total_score;
            this.question_results = question_results == null
                    ? Collections.emptyList()
                    : Collections.unmodifiableList(new ArrayList<>(question_results));
        }

        public Long getRecordId() {
            return record_id;
        }

        public int getTotalScore() {
            return total_score;
        }

        public List<QuestionResult> getQuestionResults() {
            return question_results;
        }
    }

    public static class QuestionResult {
        private final Long passage_id;
        private final Long question_id;
        private final Integer question_number;
        private final boolean correct;
        private final int earned_score;
        private final String user_answer;
        private final String correct_answer_display;

        public QuestionResult(Long passage_id,
                              Long question_id,
                              Integer question_number,
                              boolean correct,
                              int earned_score,
                              String user_answer,
                              String correct_answer_display) {
            this.passage_id = passage_id;
            this.question_id = question_id;
            this.question_number = question_number;
            this.correct = correct;
            this.earned_score = earned_score;
            this.user_answer = user_answer;
            this.correct_answer_display = correct_answer_display;
        }

        public Long getPassageId() {
            return passage_id;
        }

        public Long getQuestionId() {
            return question_id;
        }

        public Integer getQuestionNumber() {
            return question_number;
        }

        public boolean isCorrect() {
            return correct;
        }

        public int getEarnedScore() {
            return earned_score;
        }

        public String getUserAnswer() {
            return user_answer;
        }

        public String getCorrectAnswerDisplay() {
            return correct_answer_display;
        }
    }
}
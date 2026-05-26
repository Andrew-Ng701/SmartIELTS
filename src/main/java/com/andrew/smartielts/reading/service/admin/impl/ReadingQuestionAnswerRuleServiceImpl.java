package com.andrew.smartielts.reading.service.admin.impl;

import com.andrew.smartielts.common.domain.pojo.QuestionAnswerRule;
import com.andrew.smartielts.reading.mapper.ReadingQuestionAnswerRuleMapper;
import com.andrew.smartielts.reading.service.admin.ReadingQuestionAnswerRuleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReadingQuestionAnswerRuleServiceImpl implements ReadingQuestionAnswerRuleService {

    private final ReadingQuestionAnswerRuleMapper readingQuestionAnswerRuleMapper;

    public ReadingQuestionAnswerRuleServiceImpl(ReadingQuestionAnswerRuleMapper readingQuestionAnswerRuleMapper) {
        this.readingQuestionAnswerRuleMapper = readingQuestionAnswerRuleMapper;
    }

    @Override
    public QuestionAnswerRule getById(Long id) {
        if (id == null) {
            throw new RuntimeException("Id is required");
        }
        return readingQuestionAnswerRuleMapper.findById(id);
    }

    @Override
    public List<QuestionAnswerRule> listByQuestionId(Long questionId) {
        if (questionId == null) {
            throw new RuntimeException("Question id is required");
        }
        return readingQuestionAnswerRuleMapper.findByQuestionId(questionId);
    }

    @Override
    @Transactional
    public List<QuestionAnswerRule> replaceByQuestionId(Long questionId, List<QuestionAnswerRule> rules) {
        if (questionId == null) {
            throw new RuntimeException("Question id is required");
        }

        readingQuestionAnswerRuleMapper.deleteByQuestionId(questionId);

        List<QuestionAnswerRule> saved = new ArrayList<>();
        if (rules == null || rules.isEmpty()) {
            return saved;
        }

        int index = 1;
        for (QuestionAnswerRule rule : rules) {
            if (rule == null) {
                continue;
            }

            QuestionAnswerRule entity = new QuestionAnswerRule();
            entity.setQuestionId(questionId);
            entity.setBlankNo(rule.getBlankNo() == null ? 1 : rule.getBlankNo());
            entity.setAnswerGroupNo(rule.getAnswerGroupNo() == null ? 1 : rule.getAnswerGroupNo());

            List<String> answerTexts = splitAcceptedAnswerText(rule.getAnswerText());
            List<String> normalizedAnswers = splitAcceptedAnswerText(rule.getNormalizedAnswer());
            if (answerTexts.isEmpty()) {
                continue;
            }

            for (int answerIndex = 0; answerIndex < answerTexts.size(); answerIndex++) {
                QuestionAnswerRule splitEntity = new QuestionAnswerRule();
                splitEntity.setQuestionId(entity.getQuestionId());
                splitEntity.setBlankNo(entity.getBlankNo());
                splitEntity.setAnswerGroupNo(entity.getAnswerGroupNo());
                splitEntity.setAnswerText(answerTexts.get(answerIndex));
                splitEntity.setNormalizedAnswer(answerIndex < normalizedAnswers.size() ? normalizedAnswers.get(answerIndex) : null);
                splitEntity.setIsPrimary(answerIndex == 0 && rule.getIsPrimary() != null ? rule.getIsPrimary() : 0);
                splitEntity.setDisplayOrder(rule.getDisplayOrder() == null ? index : rule.getDisplayOrder() + answerIndex);

                readingQuestionAnswerRuleMapper.insertReadingQuestionAnswerRule(splitEntity);
                saved.add(splitEntity);
                index++;
            }
        }

        return saved;
    }

    @Override
    @Transactional
    public void deleteByQuestionId(Long questionId) {
        if (questionId == null) {
            throw new RuntimeException("Question id is required");
        }
        readingQuestionAnswerRuleMapper.deleteByQuestionId(questionId);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private List<String> splitAcceptedAnswerText(String value) {
        List<String> result = new ArrayList<>();
        String text = trimToNull(value);
        if (text == null) {
            return result;
        }

        for (String item : text.split(",")) {
            String trimmed = trimToNull(item);
            if (trimmed != null) {
                result.add(trimmed);
            }
        }
        return result;
    }
}

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
            entity.setAnswerText(trimToNull(rule.getAnswerText()));
            entity.setNormalizedAnswer(trimToNull(rule.getNormalizedAnswer()));
            entity.setIsPrimary(rule.getIsPrimary() == null ? 0 : rule.getIsPrimary());
            entity.setDisplayOrder(rule.getDisplayOrder() == null ? index : rule.getDisplayOrder());

            if (entity.getAnswerText() == null) {
                index++;
                continue;
            }

            readingQuestionAnswerRuleMapper.insertReadingQuestionAnswerRule(entity);
            saved.add(entity);
            index++;
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
}
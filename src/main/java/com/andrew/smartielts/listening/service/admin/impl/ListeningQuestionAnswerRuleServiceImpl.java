package com.andrew.smartielts.listening.service.admin.impl;

import com.andrew.smartielts.common.domain.pojo.QuestionAnswerRule;
import com.andrew.smartielts.listening.mapper.ListeningQuestionAnswerRuleMapper;
import com.andrew.smartielts.listening.service.admin.ListeningQuestionAnswerRuleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ListeningQuestionAnswerRuleServiceImpl implements ListeningQuestionAnswerRuleService {

    private final ListeningQuestionAnswerRuleMapper listeningQuestionAnswerRuleMapper;

    public ListeningQuestionAnswerRuleServiceImpl(ListeningQuestionAnswerRuleMapper listeningQuestionAnswerRuleMapper) {
        this.listeningQuestionAnswerRuleMapper = listeningQuestionAnswerRuleMapper;
    }

    @Override
    public QuestionAnswerRule getById(Long id) {
        if (id == null) {
            throw new RuntimeException("Id is required");
        }
        return listeningQuestionAnswerRuleMapper.findById(id);
    }

    @Override
    public List<QuestionAnswerRule> listByQuestionId(Long questionId) {
        if (questionId == null) {
            throw new RuntimeException("Question id is required");
        }
        return listeningQuestionAnswerRuleMapper.findByQuestionId(questionId);
    }

    @Override
    @Transactional
    public List<QuestionAnswerRule> replaceByQuestionId(Long questionId, List<QuestionAnswerRule> rules) {
        if (questionId == null) {
            throw new RuntimeException("Question id is required");
        }

        listeningQuestionAnswerRuleMapper.deleteByQuestionId(questionId);

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

            listeningQuestionAnswerRuleMapper.insertListeningQuestionAnswerRule(entity);
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
        listeningQuestionAnswerRuleMapper.deleteByQuestionId(questionId);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
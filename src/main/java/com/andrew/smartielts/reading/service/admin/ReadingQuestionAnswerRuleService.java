package com.andrew.smartielts.reading.service.admin;

import com.andrew.smartielts.common.domain.pojo.QuestionAnswerRule;

import java.util.List;

public interface ReadingQuestionAnswerRuleService {

    QuestionAnswerRule getById(Long id);

    List<QuestionAnswerRule> listByQuestionId(Long questionId);

    List<QuestionAnswerRule> replaceByQuestionId(Long questionId, List<QuestionAnswerRule> rules);

    void deleteByQuestionId(Long questionId);
}
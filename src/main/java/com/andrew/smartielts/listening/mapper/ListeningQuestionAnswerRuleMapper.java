package com.andrew.smartielts.listening.mapper;

import com.andrew.smartielts.common.domain.pojo.QuestionAnswerRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ListeningQuestionAnswerRuleMapper {

    int insertListeningQuestionAnswerRule(QuestionAnswerRule rule);

    QuestionAnswerRule findById(@Param("id") Long id);

    List<QuestionAnswerRule> findByQuestionId(@Param("questionId") Long questionId);

    int deleteById(@Param("id") Long id);

    int deleteByQuestionId(@Param("questionId") Long questionId);
}
package com.andrew.smartielts.speaking.mapper;

import com.andrew.smartielts.speaking.domain.pojo.SpeakingQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SpeakingMapper {

    void insertSpeakingQuestion(SpeakingQuestion question);

    SpeakingQuestion findById(@Param("id") Long id);

    SpeakingQuestion findAnyById(@Param("id") Long id);

    List<SpeakingQuestion> findAll();

    void updateSpeakingQuestion(SpeakingQuestion question);

    void softDeleteById(@Param("id") Long id);

    void restoreById(@Param("id") Long id);

    List<SpeakingQuestion> findByPart(@Param("part") String part);

    List<SpeakingQuestion> findByPartAndSubType(@Param("part") String part,
                                                @Param("subType") String subType);

    List<SpeakingQuestion> findByPartAndTopicKeyAndSubType(@Param("part") String part,
                                                           @Param("topicKey") String topicKey,
                                                           @Param("subType") String subType);
}
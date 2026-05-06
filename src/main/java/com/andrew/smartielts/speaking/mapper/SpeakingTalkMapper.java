package com.andrew.smartielts.speaking.mapper;

import com.andrew.smartielts.speaking.domain.pojo.SpeakingTalk;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SpeakingTalkMapper {

    void insertSpeakingTalk(SpeakingTalk talk);

    SpeakingTalk findByTalkId(@Param("talkId") String talkId);

    SpeakingTalk findByTalkIdForUser(@Param("talkId") String talkId, @Param("userId") Long userId);

    void updateSpeakingTalk(SpeakingTalk talk);
}

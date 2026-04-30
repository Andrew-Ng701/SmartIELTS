package com.andrew.smartielts.listening.mapper;

import com.andrew.smartielts.common.domain.pojo.TestTimerConfig;
import org.apache.ibatis.annotations.Param;

public interface ListeningTestTimerMapper {

    int insertListeningTestTimer(TestTimerConfig timerConfig);

    TestTimerConfig findByTestId(@Param("testId") Long testId);

    int updateListeningTestTimer(TestTimerConfig timerConfig);

    int deleteByTestId(@Param("testId") Long testId);
}
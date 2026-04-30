package com.andrew.smartielts.reading.mapper;

import com.andrew.smartielts.common.domain.pojo.TestTimerConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReadingTestTimerMapper {

    int insertReadingTestTimer(TestTimerConfig config);

    TestTimerConfig findByTestId(@Param("testId") Long testId);

    int updateReadingTestTimer(TestTimerConfig config);

    int deleteByTestId(@Param("testId") Long testId);
}
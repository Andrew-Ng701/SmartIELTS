package com.andrew.smartielts.reading.mapper;

import com.andrew.smartielts.reading.domain.pojo.ReadingTest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReadingTestMapper {
    void insertReadingTest(ReadingTest test);

    ReadingTest findActiveById(@Param("id") Long id);

    ReadingTest findAnyById(@Param("id") Long id);

    List<ReadingTest> findAllActive();

    List<ReadingTest> findAllDeleted();

    List<ReadingTest> findAllIncludingDeleted();

    void updateReadingTest(ReadingTest test);

    void softDeleteById(@Param("id") Long id);

    void restoreById(@Param("id") Long id);
}

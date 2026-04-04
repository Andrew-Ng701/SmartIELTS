package com.andrew.smartielts.reading.mapper;

import com.andrew.smartielts.reading.domain.pojo.ReadingPassage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReadingPassageMapper {

    void insertReadingPassage(ReadingPassage passage);

    ReadingPassage findActiveById(@Param("id") Long id);

    ReadingPassage findAnyById(@Param("id") Long id);

    List<ReadingPassage> findActiveByTestId(@Param("testId") Long testId);

    List<ReadingPassage> findAnyByTestId(@Param("testId") Long testId);

    void updateReadingPassage(ReadingPassage passage);

    void softDeleteById(@Param("id") Long id);

    void softDeleteByTestId(@Param("testId") Long testId);

    void restoreById(@Param("id") Long id);

    void restoreByTestId(@Param("testId") Long testId);
}
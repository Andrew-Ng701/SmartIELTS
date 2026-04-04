package com.andrew.smartielts.reading.mapper;

import com.andrew.smartielts.reading.domain.pojo.ReadingQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReadingQuestionMapper {

    void insertReadingQuestion(ReadingQuestion question);

    ReadingQuestion findActiveById(@Param("id") Long id);

    ReadingQuestion findAnyById(@Param("id") Long id);

    List<ReadingQuestion> findActiveByPassageId(@Param("passageId") Long passageId);

    List<ReadingQuestion> findAnyByPassageId(@Param("passageId") Long passageId);

    void updateReadingQuestion(ReadingQuestion question);

    void softDeleteById(@Param("id") Long id);

    void softDeleteByPassageId(@Param("passageId") Long passageId);

    void restoreById(@Param("id") Long id);

    void restoreByPassageId(@Param("passageId") Long passageId);
}
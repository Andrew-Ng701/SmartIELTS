package com.andrew.smartielts.reading.mapper;

import com.andrew.smartielts.reading.domain.pojo.ReadingAnswerRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReadingAnswerRecordMapper {

    int insertReadingAnswerRecord(ReadingAnswerRecord record);

    List<ReadingAnswerRecord> findByRecordId(@Param("recordId") Long recordId);
}
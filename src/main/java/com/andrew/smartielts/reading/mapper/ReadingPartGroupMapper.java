package com.andrew.smartielts.reading.mapper;

import com.andrew.smartielts.common.domain.pojo.TestPartGroup;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ReadingPartGroupMapper {

    int insertReadingPartGroup(TestPartGroup partGroup);

    TestPartGroup findActiveById(@Param("id") Long id);

    TestPartGroup findAnyById(@Param("id") Long id);

    List<TestPartGroup> findActiveByTestId(@Param("testId") Long testId);

    List<TestPartGroup> findAnyByTestId(@Param("testId") Long testId);

    int updateReadingPartGroup(TestPartGroup partGroup);

    int softDeleteById(@Param("id") Long id);

    int softDeleteByTestId(@Param("testId") Long testId);

    int restoreById(@Param("id") Long id);

    int restoreByTestId(@Param("testId") Long testId);
}
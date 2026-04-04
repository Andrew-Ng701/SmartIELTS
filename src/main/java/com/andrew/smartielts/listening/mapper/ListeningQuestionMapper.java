package com.andrew.smartielts.listening.mapper;

import com.andrew.smartielts.listening.domain.pojo.ListeningQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ListeningQuestionMapper {

    void insertListeningQuestion(ListeningQuestion question);

    ListeningQuestion findActiveById(@Param("id") Long id);

    ListeningQuestion findAnyById(@Param("id") Long id);

    List<ListeningQuestion> findActiveByTestId(@Param("testId") Long testId);

    List<ListeningQuestion> findAnyByTestId(@Param("testId") Long testId);

    void updateListeningQuestion(ListeningQuestion question);

    void softDeleteById(@Param("id") Long id);

    void softDeleteByTestId(@Param("testId") Long testId);

    void restoreById(@Param("id") Long id);

    void restoreByTestId(@Param("testId") Long testId);
}
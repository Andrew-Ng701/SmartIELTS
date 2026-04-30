package com.andrew.smartielts.writing.service.admin;

import com.andrew.smartielts.common.page.PageResult;
import com.andrew.smartielts.writing.domain.dto.WritingQuestionDTO;
import com.andrew.smartielts.writing.domain.query.admin.AdminWritingDeletedRecordPageQuery;
import com.andrew.smartielts.writing.domain.query.admin.AdminWritingRecordPageQuery;
import com.andrew.smartielts.writing.domain.vo.WritingQuestionVO;
import com.andrew.smartielts.writing.domain.vo.WritingRecordDetailVO;
import com.andrew.smartielts.writing.domain.vo.WritingRecordVO;

import java.util.List;

public interface AdminWritingService {

    WritingQuestionVO createQuestion(WritingQuestionDTO dto);

    List<WritingQuestionVO> listQuestions();

    WritingQuestionVO getQuestion(Long id);

    WritingQuestionVO updateQuestion(Long id, WritingQuestionDTO dto);

    void deleteQuestion(Long id);

    void restoreQuestion(Long id);

    PageResult<WritingRecordVO> pageActiveRecords(AdminWritingRecordPageQuery query);

    PageResult<WritingRecordVO> pageDeletedRecords(AdminWritingDeletedRecordPageQuery query);

    WritingRecordDetailVO getRecord(Long recordId);

    void deleteRecord(Long recordId);

    void restoreRecord(Long recordId);
}
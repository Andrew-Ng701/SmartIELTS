package com.andrew.smartielts.writing.service.admin;

import com.andrew.smartielts.common.page.PageResult;
import com.andrew.smartielts.writing.domain.pojo.WritingQuestion;
import com.andrew.smartielts.writing.domain.query.admin.AdminWritingDeletedRecordPageQuery;
import com.andrew.smartielts.writing.domain.query.admin.AdminWritingRecordPageQuery;
import com.andrew.smartielts.writing.domain.vo.WritingRecordDetailVO;
import com.andrew.smartielts.writing.domain.vo.WritingRecordVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AdminWritingService {

    WritingQuestion createQuestion(String taskType, String title, String description, MultipartFile image);

    List<WritingQuestion> listQuestions();

    WritingQuestion getQuestion(Long id);

    WritingQuestion updateQuestion(Long id, String taskType, String title, String description, MultipartFile image);

    void deleteQuestion(Long id);

    void restoreQuestion(Long id);

    PageResult<WritingRecordVO> pageActiveRecords(AdminWritingRecordPageQuery query);

    PageResult<WritingRecordVO> pageDeletedRecords(AdminWritingDeletedRecordPageQuery query);

    WritingRecordDetailVO getRecord(Long recordId);

    void deleteRecord(Long recordId);

    void restoreRecord(Long recordId);
}
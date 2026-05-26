package com.andrew.smartielts.writing.service.admin;

import com.andrew.smartielts.writing.domain.dto.WritingQuestionDTO;
import com.andrew.smartielts.writing.domain.vo.WritingQuestionVO;
import com.andrew.smartielts.writing.domain.vo.WritingRecordDetailVO;
import com.andrew.smartielts.common.image.domain.pojo.BizImageResource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AdminWritingService {

    WritingQuestionVO createQuestion(WritingQuestionDTO dto);

    List<WritingQuestionVO> listQuestions();

    WritingQuestionVO getQuestion(Long id);

    WritingQuestionVO updateQuestion(Long id, WritingQuestionDTO dto);

    List<BizImageResource> replaceQuestionImages(Long id, MultipartFile[] images);

    void deleteQuestion(Long id);

    void restoreQuestion(Long id);

    WritingRecordDetailVO getRecord(Long recordId);

    void deleteRecord(Long recordId);

    void restoreRecord(Long recordId);
}

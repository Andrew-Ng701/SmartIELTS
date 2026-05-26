package com.andrew.smartielts.reading.service.admin;

import com.andrew.smartielts.reading.domain.dto.AdminReadingTestFullSaveDTO;
import com.andrew.smartielts.reading.domain.dto.ReadingPassageDTO;
import com.andrew.smartielts.reading.domain.dto.ReadingQuestionDTO;
import com.andrew.smartielts.reading.domain.dto.ReadingTestDTO;
import com.andrew.smartielts.reading.domain.pojo.ReadingTest;
import com.andrew.smartielts.reading.domain.vo.ReadingRecordDetailVO;
import com.andrew.smartielts.reading.domain.vo.ReadingTestDetailVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AdminReadingService {

    ReadingTest createTest(ReadingTestDTO dto);

    List<ReadingTest> listTests();

    ReadingTestDetailVO getTestDetail(Long testId);

    ReadingTest updateTest(Long id, ReadingTestDTO dto);

    ReadingTestDetailVO saveFullTest(Long testId, AdminReadingTestFullSaveDTO dto);

    void deleteTest(Long id);

    void restoreTest(Long id);

    void createPassage(Long testId, ReadingPassageDTO dto);

    void updatePassage(Long passageId, ReadingPassageDTO dto);

    void deletePassage(Long passageId);

    void restorePassage(Long passageId);

    void createQuestion(Long passageId, ReadingQuestionDTO dto);

    void updateQuestion(Long questionId, ReadingQuestionDTO dto);

    void replaceQuestionImages(Long questionId, MultipartFile[] images);

    void deleteQuestion(Long questionId);

    void restoreQuestion(Long questionId);

    ReadingRecordDetailVO getRecord(Long recordId);

    void deleteRecord(Long recordId);

    void restoreRecord(Long recordId);
}

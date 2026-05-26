package com.andrew.smartielts.listening.service.admin;

import com.andrew.smartielts.common.domain.pojo.TestPartGroup;
import com.andrew.smartielts.listening.domain.dto.AdminListeningTestFullSaveDTO;
import com.andrew.smartielts.listening.domain.dto.ListeningQuestionDTO;
import com.andrew.smartielts.listening.domain.dto.ListeningTestDTO;
import com.andrew.smartielts.listening.domain.pojo.ListeningAudio;
import com.andrew.smartielts.listening.domain.vo.ListeningRecordDetailVO;
import com.andrew.smartielts.listening.domain.vo.ListeningTestDetailVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AdminListeningService {

    ListeningTestDetailVO createTest(ListeningTestDTO dto);

    ListeningTestDetailVO updateTest(Long id, ListeningTestDTO dto);

    ListeningTestDetailVO saveFullTest(Long testId, AdminListeningTestFullSaveDTO dto);

    List<ListeningTestDetailVO> listTests();

    ListeningTestDetailVO getTestDetail(Long testId);

    void deleteTest(Long id);

    void restoreTest(Long id);

    void createQuestion(Long testId, ListeningQuestionDTO dto);

    void updateQuestion(Long questionId, ListeningQuestionDTO dto);

    void replaceQuestionImages(Long questionId, MultipartFile[] images);

    void deleteQuestion(Long questionId);

    void restoreQuestion(Long questionId);

    ListeningRecordDetailVO getRecord(Long recordId);

    void deleteRecord(Long recordId);

    void restoreRecord(Long recordId);
}

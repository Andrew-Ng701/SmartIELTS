package com.andrew.smartielts.listening.service.admin;

import com.andrew.smartielts.common.page.PageResult;
import com.andrew.smartielts.listening.domain.dto.ListeningCreateTestForm;
import com.andrew.smartielts.listening.domain.dto.ListeningQuestionDTO;
import com.andrew.smartielts.listening.domain.dto.ListeningTestDTO;
import com.andrew.smartielts.listening.domain.query.admin.AdminListeningDeletedRecordPageQuery;
import com.andrew.smartielts.listening.domain.query.admin.AdminListeningRecordPageQuery;
import com.andrew.smartielts.listening.domain.vo.ListeningRecordDetailVO;
import com.andrew.smartielts.listening.domain.vo.ListeningRecordVO;
import com.andrew.smartielts.listening.domain.vo.ListeningTestDetailVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AdminListeningService {

    ListeningTestDetailVO createTest(ListeningCreateTestForm form);

    ListeningTestDetailVO updateTest(Long id, ListeningTestDTO dto);

    ListeningTestDetailVO updateTestAudio(Long id, MultipartFile file, String title, Integer totalScore, String transcriptText);

    List<ListeningTestDetailVO> listTests();

    ListeningTestDetailVO getTestDetail(Long testId);

    void deleteTest(Long id);

    void restoreTest(Long id);

    void createQuestion(Long testId, ListeningQuestionDTO dto);

    void updateQuestion(Long questionId, ListeningQuestionDTO dto);

    void deleteQuestion(Long questionId);

    void restoreQuestion(Long questionId);

    PageResult<ListeningRecordVO> pageActiveRecords(AdminListeningRecordPageQuery query);

    PageResult<ListeningRecordVO> pageDeletedRecords(AdminListeningDeletedRecordPageQuery query);

    ListeningRecordDetailVO getRecord(Long recordId);

    void deleteRecord(Long recordId);

    void restoreRecord(Long recordId);
}
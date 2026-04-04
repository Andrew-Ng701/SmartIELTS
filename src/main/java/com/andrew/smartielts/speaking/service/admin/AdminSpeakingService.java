package com.andrew.smartielts.speaking.service.admin;

import com.andrew.smartielts.common.page.PageResult;
import com.andrew.smartielts.speaking.domain.pojo.SpeakingQuestion;
import com.andrew.smartielts.speaking.domain.query.admin.AdminSpeakingDeletedRecordPageQuery;
import com.andrew.smartielts.speaking.domain.query.admin.AdminSpeakingRecordPageQuery;
import com.andrew.smartielts.speaking.domain.vo.SpeakingRecordDetailVO;
import com.andrew.smartielts.speaking.domain.vo.SpeakingRecordVO;

import java.util.List;

public interface AdminSpeakingService {

    SpeakingQuestion createSpeakingQuestion(SpeakingQuestion question);

    List<SpeakingQuestion> listAllSpeakingQuestion();

    SpeakingQuestion getSpeakingQuestion(Long id);

    SpeakingQuestion updateSpeakingQuestion(Long id, SpeakingQuestion question);

    void deleteSpeakingQuestionById(Long id);

    void restoreSpeakingQuestion(Long id);

    PageResult<SpeakingRecordVO> pageActiveRecords(AdminSpeakingRecordPageQuery query);

    PageResult<SpeakingRecordVO> pageDeletedRecords(AdminSpeakingDeletedRecordPageQuery query);

    SpeakingRecordDetailVO getRecord(Long recordId);

    void deleteRecord(Long recordId);

    void restoreRecord(Long recordId);
}
package com.andrew.smartielts.speaking.service.admin;

import com.andrew.smartielts.speaking.domain.pojo.SpeakingQuestion;
import com.andrew.smartielts.speaking.domain.vo.SpeakingRecordDetailVO;

import java.util.List;

public interface AdminSpeakingService {

    SpeakingQuestion createSpeakingQuestion(SpeakingQuestion question);

    List<SpeakingQuestion> listAllSpeakingQuestion();

    SpeakingQuestion getSpeakingQuestion(Long id);

    SpeakingQuestion updateSpeakingQuestion(Long id, SpeakingQuestion question);

    void deleteSpeakingQuestionById(Long id);

    void restoreSpeakingQuestion(Long id);

    SpeakingRecordDetailVO getRecord(Long recordId);

    void deleteRecord(Long recordId);

    void restoreRecord(Long recordId);
}

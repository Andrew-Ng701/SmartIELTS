package com.andrew.smartielts.speaking.service.admin.impl;

import com.andrew.smartielts.speaking.domain.pojo.SpeakingQuestion;
import com.andrew.smartielts.speaking.domain.pojo.SpeakingRecord;
import com.andrew.smartielts.speaking.domain.vo.SpeakingRecordDetailVO;
import com.andrew.smartielts.speaking.domain.vo.SpeakingRecordVO;
import com.andrew.smartielts.speaking.mapper.SpeakingMapper;
import com.andrew.smartielts.speaking.mapper.SpeakingRecordMapper;
import com.andrew.smartielts.speaking.service.admin.AdminSpeakingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminSpeakingServiceImpl implements AdminSpeakingService {

    private final SpeakingMapper speakingMapper;
    private final SpeakingRecordMapper speakingRecordMapper;

    public AdminSpeakingServiceImpl(SpeakingMapper speakingMapper,
                                    SpeakingRecordMapper speakingRecordMapper) {
        this.speakingMapper = speakingMapper;
        this.speakingRecordMapper = speakingRecordMapper;
    }

    @Override
    @Transactional
    public SpeakingQuestion createSpeakingQuestion(SpeakingQuestion question) {
        if (question == null) {
            throw new RuntimeException("Speaking question is required");
        }

        if (question.getActive() == null) {
            question.setActive(1);
        }
        if (question.getIsDeleted() == null) {
            question.setIsDeleted(0);
        }
        question.setDeletedTime(null);

        speakingMapper.insertSpeakingQuestion(question);
        return question;
    }

    @Override
    public List<SpeakingQuestion> listAllSpeakingQuestion() {
        return speakingMapper.findAll();
    }

    @Override
    public SpeakingQuestion getSpeakingQuestion(Long id) {
        SpeakingQuestion question = speakingMapper.findAnyById(id);
        if (question == null) {
            throw new RuntimeException("Speaking question not found");
        }
        return question;
    }

    @Override
    @Transactional
    public SpeakingQuestion updateSpeakingQuestion(Long id, SpeakingQuestion question) {
        SpeakingQuestion existing = speakingMapper.findById(id);
        if (existing == null) {
            throw new RuntimeException("Speaking question not found");
        }
        if (question == null) {
            throw new RuntimeException("Speaking question is required");
        }

        question.setId(id);
        question.setIsDeleted(existing.getIsDeleted());
        question.setDeletedTime(existing.getDeletedTime());
        if (question.getActive() == null) {
            question.setActive(existing.getActive());
        }

        speakingMapper.updateSpeakingQuestion(question);
        return speakingMapper.findById(id);
    }

    @Override
    @Transactional
    public void deleteSpeakingQuestionById(Long id) {
        SpeakingQuestion existing = speakingMapper.findById(id);
        if (existing == null) {
            throw new RuntimeException("Speaking question not found");
        }
        speakingMapper.softDeleteById(id);
    }

    @Override
    @Transactional
    public void restoreSpeakingQuestion(Long id) {
        SpeakingQuestion existing = speakingMapper.findAnyById(id);
        if (existing == null) {
            throw new RuntimeException("Speaking question not found");
        }
        speakingMapper.restoreById(id);
    }

    @Override
    public SpeakingRecordDetailVO getRecord(Long recordId) {
        SpeakingRecord record = speakingRecordMapper.findAnyById(recordId);
        if (record == null) {
            throw new RuntimeException("Speaking record not found");
        }
        SpeakingRecordDetailVO detail = toRecordDetailVO(record);
        detail.setSessionRecords(toSessionRecordVOs(record.getSessionId()));
        return detail;
    }

    @Override
    @Transactional
    public void deleteRecord(Long recordId) {
        SpeakingRecord record = speakingRecordMapper.findActiveById(recordId);
        if (record == null) {
            throw new RuntimeException("Speaking record not found");
        }
        speakingRecordMapper.softDeleteById(recordId);
    }

    @Override
    @Transactional
    public void restoreRecord(Long recordId) {
        SpeakingRecord record = speakingRecordMapper.findAnyById(recordId);
        if (record == null) {
            throw new RuntimeException("Speaking record not found");
        }
        speakingRecordMapper.restoreById(recordId);
    }

    private SpeakingRecordVO toRecordVO(SpeakingRecord record) {
        SpeakingRecordVO vo = new SpeakingRecordVO();
        vo.setId(record.getId());
        vo.setQuestionId(record.getQuestionId());
        vo.setSessionId(record.getSessionId());

        SpeakingQuestion question = findQuestionIncludingDeleted(record.getQuestionId());
        vo.setPart(question == null ? null : question.getPart());
        vo.setQuestionText(question == null ? null : question.getQuestionText());
        vo.setPrompt(question == null ? null : question.getQuestionText());

        vo.setFluencyAndCoherence(record.getFluencyAndCoherence());
        vo.setLexicalResource(record.getLexicalResource());
        vo.setGrammaticalRangeAndAccuracy(record.getGrammaticalRangeAndAccuracy());
        vo.setPronunciation(record.getPronunciation());
        vo.setOverallScore(record.getOverallScore());
        vo.setFeedback(record.getFeedback());
        vo.setAnswerStatus(record.getAnswerStatus());
        vo.setIsDeleted(record.getIsDeleted());
        vo.setDeletedTime(record.getDeletedTime());
        vo.setAiStatus(record.getAiStatus());
        vo.setAiProvider(record.getAiProvider());
        vo.setAiModel(record.getAiModel());
        vo.setAiErrorMessage(record.getAiErrorMessage());
        vo.setCreatedTime(record.getCreatedTime());
        return vo;
    }

    private SpeakingRecordDetailVO toRecordDetailVO(SpeakingRecord record) {
        SpeakingRecordDetailVO vo = new SpeakingRecordDetailVO();
        vo.setRecordId(record.getId());
        vo.setSessionId(record.getSessionId());
        vo.setQuestionId(record.getQuestionId());

        SpeakingQuestion question = findQuestionIncludingDeleted(record.getQuestionId());
        vo.setPart(question == null ? null : question.getPart());
        vo.setQuestionText(question == null ? null : question.getQuestionText());
        vo.setPrompt(question == null ? null : question.getQuestionText());
        vo.setCueCard(question == null ? null : question.getCueCard());

        vo.setAudioUrl(record.getAudioUrl());
        vo.setTranscript(record.getTranscript());
        vo.setFluencyAndCoherence(record.getFluencyAndCoherence());
        vo.setLexicalResource(record.getLexicalResource());
        vo.setGrammaticalRangeAndAccuracy(record.getGrammaticalRangeAndAccuracy());
        vo.setPronunciation(record.getPronunciation());
        vo.setOverallScore(record.getOverallScore());
        vo.setFeedback(record.getFeedback());
        vo.setRelevanceComment(record.getRelevanceComment());
        vo.setQualityComment(record.getQualityComment());
        vo.setAnswerStatus(record.getAnswerStatus());
        vo.setIsDeleted(record.getIsDeleted());
        vo.setDeletedTime(record.getDeletedTime());
        vo.setAiStatus(record.getAiStatus());
        vo.setAiProvider(record.getAiProvider());
        vo.setAiModel(record.getAiModel());
        vo.setAiErrorMessage(record.getAiErrorMessage());
        vo.setCreatedTime(record.getCreatedTime());
        vo.setUpdatedTime(record.getUpdatedTime());
        return vo;
    }

    private List<SpeakingRecordVO> toSessionRecordVOs(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return new ArrayList<>();
        }
        List<SpeakingRecord> records = speakingRecordMapper.findBySessionId(sessionId);
        if (records == null || records.isEmpty()) {
            return new ArrayList<>();
        }
        List<SpeakingRecordVO> vos = new ArrayList<>();
        for (SpeakingRecord sessionRecord : records) {
            SpeakingRecordVO vo = toRecordVO(sessionRecord);
            SpeakingQuestion question = findQuestionIncludingDeleted(sessionRecord.getQuestionId());
            vo.setPrompt(question == null ? null : question.getQuestionText());
            vo.setCueCard(question == null ? null : question.getCueCard());
            vo.setAudioUrl(sessionRecord.getAudioUrl());
            vo.setTranscript(sessionRecord.getTranscript());
            vo.setRelevanceComment(sessionRecord.getRelevanceComment());
            vo.setQualityComment(sessionRecord.getQualityComment());
            vos.add(vo);
        }
        return vos;
    }

    private SpeakingQuestion findQuestionIncludingDeleted(Long questionId) {
        if (questionId == null) {
            return null;
        }
        try {
            return speakingMapper.findAnyById(questionId);
        } catch (Exception ignored) {
            return speakingMapper.findById(questionId);
        }
    }

}

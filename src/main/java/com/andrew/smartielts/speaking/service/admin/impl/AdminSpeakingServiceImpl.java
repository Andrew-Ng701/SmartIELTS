package com.andrew.smartielts.speaking.service.admin.impl;

import com.andrew.smartielts.common.page.PageResult;
import com.andrew.smartielts.speaking.domain.pojo.SpeakingQuestion;
import com.andrew.smartielts.speaking.domain.pojo.SpeakingRecord;
import com.andrew.smartielts.speaking.domain.query.admin.AdminSpeakingDeletedRecordPageQuery;
import com.andrew.smartielts.speaking.domain.query.admin.AdminSpeakingRecordPageQuery;
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
        SpeakingQuestion question = speakingMapper.findById(id);
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
    public PageResult<SpeakingRecordVO> pageActiveRecords(AdminSpeakingRecordPageQuery query) {
        AdminSpeakingRecordPageQuery safeQuery =
                query == null ? new AdminSpeakingRecordPageQuery() : query;

        int pageNum = normalizePageNum(safeQuery.getPageNum());
        int pageSize = normalizePageSize(safeQuery.getPageSize());
        int offset = (pageNum - 1) * pageSize;

        Long total = speakingRecordMapper.countAdminActive(safeQuery);
        if (total == null || total == 0L) {
            return new PageResult<>(new ArrayList<>(), 0L, pageNum, pageSize);
        }

        List<SpeakingRecordVO> records = speakingRecordMapper.pageAdminActive(safeQuery, offset, pageSize);
        return new PageResult<>(records, total, pageNum, pageSize);
    }

    @Override
    public PageResult<SpeakingRecordVO> pageDeletedRecords(AdminSpeakingDeletedRecordPageQuery query) {
        AdminSpeakingDeletedRecordPageQuery safeQuery =
                query == null ? new AdminSpeakingDeletedRecordPageQuery() : query;

        int pageNum = normalizePageNum(safeQuery.getPageNum());
        int pageSize = normalizePageSize(safeQuery.getPageSize());
        int offset = (pageNum - 1) * pageSize;

        Long total = speakingRecordMapper.countAdminDeleted(safeQuery);
        if (total == null || total == 0L) {
            return new PageResult<>(new ArrayList<>(), 0L, pageNum, pageSize);
        }

        List<SpeakingRecordVO> records = speakingRecordMapper.pageAdminDeleted(safeQuery, offset, pageSize);
        return new PageResult<>(records, total, pageNum, pageSize);
    }

    @Override
    public SpeakingRecordDetailVO getRecord(Long recordId) {
        SpeakingRecord record = speakingRecordMapper.findAnyById(recordId);
        if (record == null) {
            throw new RuntimeException("Speaking record not found");
        }
        return toRecordDetailVO(record);
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

    private int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }
}
package com.andrew.smartielts.writing.service.admin.impl;

import com.andrew.smartielts.common.page.PageResult;
import com.andrew.smartielts.common.storage.BucketType;
import com.andrew.smartielts.common.storage.UploadResult;
import com.andrew.smartielts.common.storage.service.StorageService;
import com.andrew.smartielts.writing.domain.pojo.WritingQuestion;
import com.andrew.smartielts.writing.domain.pojo.WritingRecord;
import com.andrew.smartielts.writing.domain.pojo.WritingRecordAttachment;
import com.andrew.smartielts.writing.domain.query.admin.AdminWritingDeletedRecordPageQuery;
import com.andrew.smartielts.writing.domain.query.admin.AdminWritingRecordPageQuery;
import com.andrew.smartielts.writing.domain.vo.WritingAttachmentVO;
import com.andrew.smartielts.writing.domain.vo.WritingRecordDetailVO;
import com.andrew.smartielts.writing.domain.vo.WritingRecordVO;
import com.andrew.smartielts.writing.mapper.WritingQuestionMapper;
import com.andrew.smartielts.writing.mapper.WritingRecordAttachmentMapper;
import com.andrew.smartielts.writing.mapper.WritingRecordMapper;
import com.andrew.smartielts.writing.service.admin.AdminWritingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminWritingServiceImpl implements AdminWritingService {

    @Autowired
    private WritingQuestionMapper writingQuestionMapper;

    @Autowired
    private WritingRecordMapper writingRecordMapper;

    @Autowired
    private WritingRecordAttachmentMapper writingRecordAttachmentMapper;

    @Autowired
    private StorageService storageService;

    @Override
    @Transactional
    public WritingQuestion createQuestion(String taskType, String title, String description, MultipartFile image) {
        validateQuestionInput(taskType, title, description);

        WritingQuestion question = new WritingQuestion();
        question.setTaskType(taskType.trim());
        question.setTitle(title.trim());
        question.setDescription(description.trim());
        question.setCreatedTime(LocalDateTime.now());
        question.setIsDeleted(0);

        if (image != null && !image.isEmpty()) {
            UploadResult upload = storageService.upload(
                    image,
                    BucketType.WRITING_QUESTION,
                    "writing/question"
            );
            question.setImageUrl(upload.getFileUrl());
            question.setImageObjectKey(upload.getFileKey());
        }

        writingQuestionMapper.insert(question);
        return question;
    }

    @Override
    public List<WritingQuestion> listQuestions() {
        return writingQuestionMapper.findAll();
    }

    @Override
    public WritingQuestion getQuestion(Long id) {
        WritingQuestion question = writingQuestionMapper.findById(id);
        if (question == null) {
            throw new RuntimeException("Writing question not found");
        }
        return question;
    }

    @Override
    @Transactional
    public WritingQuestion updateQuestion(Long id, String taskType, String title, String description, MultipartFile image) {
        validateQuestionInput(taskType, title, description);

        WritingQuestion existing = writingQuestionMapper.findById(id);
        if (existing == null) {
            throw new RuntimeException("Writing question not found");
        }

        existing.setTaskType(taskType.trim());
        existing.setTitle(title.trim());
        existing.setDescription(description.trim());

        if (image != null && !image.isEmpty()) {
            if (existing.getImageObjectKey() != null && !existing.getImageObjectKey().isBlank()) {
                storageService.delete(BucketType.WRITING_QUESTION, existing.getImageObjectKey());
            }

            UploadResult upload = storageService.upload(
                    image,
                    BucketType.WRITING_QUESTION,
                    "writing/question"
            );
            existing.setImageUrl(upload.getFileUrl());
            existing.setImageObjectKey(upload.getFileKey());
        }

        writingQuestionMapper.update(existing);
        return existing;
    }

    @Override
    @Transactional
    public void deleteQuestion(Long id) {
        WritingQuestion question = writingQuestionMapper.findById(id);
        if (question == null) {
            throw new RuntimeException("Writing question not found");
        }
        if (question.getIsDeleted() != null && question.getIsDeleted() == 1) {
            return;
        }
        writingQuestionMapper.logicalDeleteById(id, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void restoreQuestion(Long id) {
        WritingQuestion question = writingQuestionMapper.findByIdForAdmin(id);
        if (question == null) {
            throw new RuntimeException("Writing question not found");
        }
        if (question.getIsDeleted() != null && question.getIsDeleted() == 0) {
            return;
        }
        writingQuestionMapper.restoreById(id);
    }

    @Override
    public PageResult<WritingRecordVO> pageActiveRecords(AdminWritingRecordPageQuery query) {
        AdminWritingRecordPageQuery safeQuery = query == null ? new AdminWritingRecordPageQuery() : query;

        int pageNum = normalizePageNum(safeQuery.getPageNum());
        int pageSize = normalizePageSize(safeQuery.getPageSize());
        int offset = (pageNum - 1) * pageSize;

        Long total = writingRecordMapper.countAdminActive(safeQuery);
        if (total == null || total == 0L) {
            return new PageResult<>(new ArrayList<>(), 0L, pageNum, pageSize);
        }

        List<WritingRecord> records = writingRecordMapper.pageAdminActive(safeQuery, offset, pageSize);
        List<WritingRecordVO> voList = new ArrayList<>();

        if (records != null) {
            for (WritingRecord record : records) {
                voList.add(toRecordVO(record));
            }
        }

        return new PageResult<>(voList, total, pageNum, pageSize);
    }

    @Override
    public PageResult<WritingRecordVO> pageDeletedRecords(AdminWritingDeletedRecordPageQuery query) {
        AdminWritingDeletedRecordPageQuery safeQuery =
                query == null ? new AdminWritingDeletedRecordPageQuery() : query;

        int pageNum = normalizePageNum(safeQuery.getPageNum());
        int pageSize = normalizePageSize(safeQuery.getPageSize());
        int offset = (pageNum - 1) * pageSize;

        Long total = writingRecordMapper.countAdminDeleted(safeQuery);
        if (total == null || total == 0L) {
            return new PageResult<>(new ArrayList<>(), 0L, pageNum, pageSize);
        }

        List<WritingRecord> records = writingRecordMapper.pageAdminDeleted(safeQuery, offset, pageSize);
        List<WritingRecordVO> voList = new ArrayList<>();

        if (records != null) {
            for (WritingRecord record : records) {
                voList.add(toRecordVO(record));
            }
        }

        return new PageResult<>(voList, total, pageNum, pageSize);
    }

    @Override
    public WritingRecordDetailVO getRecord(Long recordId) {
        WritingRecord record = writingRecordMapper.findByIdForAdmin(recordId);
        if (record == null) {
            throw new RuntimeException("Writing record not found");
        }

        WritingQuestion question = writingQuestionMapper.findByIdForAdmin(record.getQuestionId());
        List<WritingRecordAttachment> attachments = writingRecordAttachmentMapper.findByRecordId(recordId);
        if (attachments == null) {
            attachments = new ArrayList<>();
        }

        return buildDetailVO(record, question, attachments);
    }

    @Override
    @Transactional
    public void deleteRecord(Long recordId) {
        WritingRecord record = writingRecordMapper.findByIdForAdmin(recordId);
        if (record == null) {
            throw new RuntimeException("Writing record not found");
        }
        if (record.getIsDeleted() != null && record.getIsDeleted() == 1) {
            return;
        }

        writingRecordMapper.softDeleteById(recordId);
    }

    @Override
    @Transactional
    public void restoreRecord(Long recordId) {
        WritingRecord record = writingRecordMapper.findByIdForAdmin(recordId);
        if (record == null) {
            throw new RuntimeException("Writing record not found");
        }
        if (record.getIsDeleted() != null && record.getIsDeleted() == 0) {
            return;
        }

        writingRecordMapper.restoreByIdForAdmin(recordId);
    }

    private WritingRecordVO toRecordVO(WritingRecord record) {
        WritingRecordVO vo = new WritingRecordVO();
        vo.setId(record.getId());
        vo.setQuestionId(record.getQuestionId());
        vo.setInputType(record.getInputType());
        vo.setTargetScore(record.getTargetScore());
        vo.setAiScore(record.getAiScore());
        vo.setAiStatus(record.getAiStatus());
        vo.setCreatedTime(record.getCreatedTime());

        WritingQuestion question = writingQuestionMapper.findByIdForAdmin(record.getQuestionId());
        vo.setQuestionTitle(question != null ? question.getTitle() : null);
        return vo;
    }

    private WritingRecordDetailVO buildDetailVO(WritingRecord record,
                                                WritingQuestion question,
                                                List<WritingRecordAttachment> attachments) {
        WritingRecordDetailVO vo = new WritingRecordDetailVO();
        vo.setRecordId(record.getId());
        vo.setQuestionId(record.getQuestionId());
        vo.setQuestionTitle(question != null ? question.getTitle() : null);
        vo.setQuestionDescription(question != null ? question.getDescription() : null);
        vo.setQuestionImageUrl(question != null ? question.getImageUrl() : null);
        vo.setTaskType(question != null ? question.getTaskType() : null);
        vo.setInputType(record.getInputType());
        vo.setTextContent(record.getTextContent());
        vo.setExtractedText(record.getExtractedText());
        vo.setTargetScore(record.getTargetScore());
        vo.setAiScore(record.getAiScore());
        vo.setAiFeedback(record.getAiFeedback());
        vo.setAiStatus(record.getAiStatus());
        vo.setAiProvider(record.getAiProvider());
        vo.setAiModel(record.getAiModel());
        vo.setCreatedTime(record.getCreatedTime());

        List<WritingAttachmentVO> attachmentVOList = new ArrayList<>();
        if (attachments != null) {
            for (WritingRecordAttachment attachment : attachments) {
                WritingAttachmentVO attachmentVO = new WritingAttachmentVO();
                attachmentVO.setId(attachment.getId());
                attachmentVO.setFileType(attachment.getFileType());
                attachmentVO.setFileUrl(attachment.getFileUrl());
                attachmentVO.setSortOrder(attachment.getSortOrder());
                attachmentVO.setCreatedTime(attachment.getCreatedTime());
                attachmentVO.setOcrText(attachment.getOcrText());
                attachmentVOList.add(attachmentVO);
            }
        }
        vo.setAttachments(attachmentVOList);
        return vo;
    }

    private void validateQuestionInput(String taskType, String title, String description) {
        if (taskType == null || taskType.isBlank()) {
            throw new RuntimeException("Task type cannot be empty");
        }
        if (title == null || title.isBlank()) {
            throw new RuntimeException("Title cannot be empty");
        }
        if (description == null || description.isBlank()) {
            throw new RuntimeException("Description cannot be empty");
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
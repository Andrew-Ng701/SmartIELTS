package com.andrew.smartielts.writing.service.user.impl;

import com.andrew.smartielts.common.page.PageResult;
import com.andrew.smartielts.common.storage.BucketType;
import com.andrew.smartielts.common.storage.UploadResult;
import com.andrew.smartielts.common.storage.service.StorageService;
import com.andrew.smartielts.utils.SecurityUtils;
import com.andrew.smartielts.writing.ai.AiWritingScore;
import com.andrew.smartielts.writing.ai.service.AiWritingScoringService;
import com.andrew.smartielts.writing.domain.pojo.WritingQuestion;
import com.andrew.smartielts.writing.domain.pojo.WritingRecord;
import com.andrew.smartielts.writing.domain.pojo.WritingRecordAttachment;
import com.andrew.smartielts.writing.domain.query.user.UserWritingDeletedRecordPageQuery;
import com.andrew.smartielts.writing.domain.query.user.UserWritingRecordPageQuery;
import com.andrew.smartielts.writing.domain.vo.WritingAttachmentVO;
import com.andrew.smartielts.writing.domain.vo.WritingRecordDetailVO;
import com.andrew.smartielts.writing.domain.vo.WritingRecordVO;
import com.andrew.smartielts.writing.io.WritingSubmissionValidator;
import com.andrew.smartielts.writing.mapper.WritingQuestionMapper;
import com.andrew.smartielts.writing.mapper.WritingRecordAttachmentMapper;
import com.andrew.smartielts.writing.mapper.WritingRecordMapper;
import com.andrew.smartielts.writing.ocr.service.OcrService;
import com.andrew.smartielts.writing.pdf.PdfTextExtractor;
import com.andrew.smartielts.writing.service.user.UserWritingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserWritingServiceImpl implements UserWritingService {

    @Autowired
    private WritingQuestionMapper writingQuestionMapper;

    @Autowired
    private WritingRecordMapper writingRecordMapper;

    @Autowired
    private WritingRecordAttachmentMapper writingRecordAttachmentMapper;

    @Autowired
    private WritingSubmissionValidator writingSubmissionValidator;

    @Autowired
    private StorageService storageService;

    @Autowired
    private OcrService ocrService;

    @Autowired
    private PdfTextExtractor pdfTextExtractor;

    @Autowired
    private AiWritingScoringService aiWritingScoringService;

    @Override
    public List<WritingQuestion> listAllWritingPaper() {
        return writingQuestionMapper.findAll();
    }

    @Override
    public WritingQuestion getQuestion(Long questionId) {
        WritingQuestion question = writingQuestionMapper.findById(questionId);
        if (question == null) {
            throw new RuntimeException("Writing question not found");
        }
        return question;
    }

    @Override
    @Transactional
    public WritingRecordDetailVO submitRecord(Long questionId, BigDecimal targetScore, String textContent, MultipartFile[] images, MultipartFile pdf) {
        writingSubmissionValidator.validate(textContent, images, pdf);

        WritingQuestion question = writingQuestionMapper.findById(questionId);
        if (question == null) {
            throw new RuntimeException("Writing question not found");
        }

        String inputType = writingSubmissionValidator.resolveInputType(textContent, images, pdf);

        WritingRecord record = new WritingRecord();
        record.setUserId(SecurityUtils.getCurrentUserId());
        record.setQuestionId(questionId);
        record.setInputType(inputType);
        record.setTextContent("TEXT".equals(inputType) ? textContent : null);
        record.setExtractedText(null);
        record.setTargetScore(targetScore);
        record.setAiStatus("PENDING");
        record.setAiProvider("ALIYUN");
        record.setAiModel("qwen3.5-flash");
        record.setIsDeleted(0);
        record.setCreatedTime(LocalDateTime.now());
        writingRecordMapper.insert(record);

        List<WritingRecordAttachment> attachments = new ArrayList<>();
        String finalText;

        if ("TEXT".equals(inputType)) {
            finalText = textContent;
        } else if ("IMAGE".equals(inputType)) {
            for (int i = 0; i < images.length; i++) {
                MultipartFile image = images[i];
                UploadResult upload = storageService.upload(image, BucketType.WRITING_RECORD, "writing/" + record.getId());

                WritingRecordAttachment attachment = new WritingRecordAttachment();
                attachment.setRecordId(record.getId());
                attachment.setFileType("IMAGE");
                attachment.setFileUrl(upload.getFileUrl());
                attachment.setFileKey(upload.getFileKey());
                attachment.setSortOrder(i + 1);
                attachment.setCreatedTime(LocalDateTime.now());
                attachment.setOcrText(null);
                writingRecordAttachmentMapper.insert(attachment);
                attachments.add(attachment);
            }

            try {
                attachments = ocrService.recognizeAndFill(attachments);
                finalText = ocrService.mergeText(attachments);

                for (WritingRecordAttachment attachment : attachments) {
                    writingRecordAttachmentMapper.updateOcrText(attachment);
                }

                record.setExtractedText(finalText);
                writingRecordMapper.updateExtractedText(record.getId(), finalText);
            } catch (Exception e) {
                record.setAiStatus("FAILED");
                record.setAiFeedback("OCR failed: " + e.getMessage());
                record.setAiRawResponse(e.toString());
                writingRecordMapper.updateAiResult(record);
                return buildDetailVO(record, question, attachments);
            }
        } else {
            UploadResult upload = storageService.upload(pdf, BucketType.WRITING_RECORD, "writing/" + record.getId());

            WritingRecordAttachment attachment = new WritingRecordAttachment();
            attachment.setRecordId(record.getId());
            attachment.setFileType("PDF");
            attachment.setFileUrl(upload.getFileUrl());
            attachment.setFileKey(upload.getFileKey());
            attachment.setSortOrder(1);
            attachment.setCreatedTime(LocalDateTime.now());

            String pdfText = pdfTextExtractor.extractText(pdf);
            attachment.setOcrText(pdfText);

            writingRecordAttachmentMapper.insert(attachment);
            attachments.add(attachment);

            finalText = pdfText;
            record.setExtractedText(finalText);
            writingRecordMapper.updateExtractedText(record.getId(), finalText);
        }

        try {
            AiWritingScore score = aiWritingScoringService.score(question, record, finalText);
            record.setAiScore(score.getAiScore());
            record.setAiFeedback(score.getAiFeedback());
            record.setAiRawResponse(score.getRawResponse());
            record.setAiStatus("SUCCESS");
            writingRecordMapper.updateAiResult(record);
        } catch (Exception e) {
            record.setAiStatus("FAILED");
            record.setAiFeedback(e.getMessage());
            record.setAiRawResponse(e.toString());
            writingRecordMapper.updateAiResult(record);
        }

        return buildDetailVO(record, question, attachments);
    }

    @Override
    public List<WritingRecordVO> listMyRecords(Long userId) {
        List<WritingRecord> records = writingRecordMapper.findByUserId(userId);
        List<WritingRecordVO> result = new ArrayList<>();

        for (WritingRecord record : records) {
            WritingQuestion question = writingQuestionMapper.findById(record.getQuestionId());

            WritingRecordVO vo = new WritingRecordVO();
            vo.setId(record.getId());
            vo.setQuestionId(record.getQuestionId());
            vo.setQuestionTitle(question != null ? question.getTitle() : null);
            vo.setInputType(record.getInputType());
            vo.setTargetScore(record.getTargetScore());
            vo.setAiScore(record.getAiScore());
            vo.setAiStatus(record.getAiStatus());
            vo.setCreatedTime(record.getCreatedTime());
            result.add(vo);
        }

        return result;
    }

    @Override
    public WritingRecordDetailVO getRecord(Long recordId, Long userId) {
        WritingRecord record = writingRecordMapper.findByIdForUser(recordId,userId);
        if (record == null) {
            throw new RuntimeException("Writing record not found");
        }

        WritingQuestion question = writingQuestionMapper.findById(record.getQuestionId());
        List<WritingRecordAttachment> attachments = writingRecordAttachmentMapper.findByRecordId(recordId);
        if (attachments == null) {
            attachments = new ArrayList<>();
        }

        return buildDetailVO(record, question, attachments);
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

    @Override
    public PageResult<WritingRecordVO> pageActiveRecords(Long userId, UserWritingRecordPageQuery query) {
        UserWritingRecordPageQuery safeQuery = query == null ? new UserWritingRecordPageQuery() : query;

        int pageNum = normalizePageNum(safeQuery.getPageNum());
        int pageSize = normalizePageSize(safeQuery.getPageSize());
        int offset = (pageNum - 1) * pageSize;

        Long total = writingRecordMapper.countUserActive(userId, safeQuery);
        if (total == null || total == 0L) {
            return new PageResult<>(new ArrayList<>(), 0L, pageNum, pageSize);
        }

        List<WritingRecord> records = writingRecordMapper.pageUserActive(userId, safeQuery, offset, pageSize);
        List<WritingRecordVO> voList = new ArrayList<>();

        if (records != null) {
            for (WritingRecord record : records) {
                voList.add(toRecordVO(record));
            }
        }

        return new PageResult<>(voList, total, pageNum, pageSize);
    }

    @Override
    public PageResult<WritingRecordVO> pageDeletedRecords(Long userId, UserWritingDeletedRecordPageQuery query) {
        UserWritingDeletedRecordPageQuery safeQuery =
                query == null ? new UserWritingDeletedRecordPageQuery() : query;

        int pageNum = normalizePageNum(safeQuery.getPageNum());
        int pageSize = normalizePageSize(safeQuery.getPageSize());
        int offset = (pageNum - 1) * pageSize;

        Long total = writingRecordMapper.countUserDeleted(userId, safeQuery);
        if (total == null || total == 0L) {
            return new PageResult<>(new ArrayList<>(), 0L, pageNum, pageSize);
        }

        List<WritingRecord> records = writingRecordMapper.pageUserDeleted(userId, safeQuery, offset, pageSize);
        List<WritingRecordVO> voList = new ArrayList<>();

        if (records != null) {
            for (WritingRecord record : records) {
                voList.add(toRecordVO(record));
            }
        }

        return new PageResult<>(voList, total, pageNum, pageSize);
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

        WritingQuestion question = writingQuestionMapper.findById(record.getQuestionId());
        vo.setQuestionTitle(question != null ? question.getTitle() : null);
        return vo;
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

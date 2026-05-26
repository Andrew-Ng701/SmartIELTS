package com.andrew.smartielts.writing.service.admin.impl;

import com.andrew.smartielts.common.image.domain.pojo.BizImageResource;
import com.andrew.smartielts.common.image.service.BizImageResourceService;
import com.andrew.smartielts.common.storage.BucketType;
import com.andrew.smartielts.writing.ai.service.WritingQuestionImageDescriptionService;
import com.andrew.smartielts.writing.domain.dto.WritingQuestionDTO;
import com.andrew.smartielts.writing.domain.pojo.WritingQuestion;
import com.andrew.smartielts.writing.domain.pojo.WritingRecord;
import com.andrew.smartielts.writing.domain.pojo.WritingRecordAttachment;
import com.andrew.smartielts.writing.domain.vo.WritingAttachmentVO;
import com.andrew.smartielts.writing.domain.vo.WritingPreviewAssetVO;
import com.andrew.smartielts.writing.domain.vo.WritingQuestionVO;
import com.andrew.smartielts.writing.domain.vo.WritingRecordDetailVO;
import com.andrew.smartielts.writing.mapper.WritingQuestionMapper;
import com.andrew.smartielts.writing.mapper.WritingRecordAttachmentMapper;
import com.andrew.smartielts.writing.mapper.WritingRecordMapper;
import com.andrew.smartielts.writing.service.admin.AdminWritingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.andrew.smartielts.common.constants.StorageBizConstants.BIZ_PATH_WRITING_QUESTION_IMAGE;
import static com.andrew.smartielts.common.constants.StorageBizConstants.BUCKET_KEY_WRITING_QUESTION;
import static com.andrew.smartielts.common.constants.StorageBizConstants.TARGET_TYPE_WRITING_QUESTION;

@Service
public class AdminWritingServiceImpl implements AdminWritingService {

    private static final String INPUT_TYPE_TEXT = "TEXT";
    private static final String FILE_TYPE_IMAGE = "IMAGE";
    private static final String PREVIEW_SOURCE_QUESTION_IMAGE = "QUESTION_IMAGE";
    private static final String PREVIEW_SOURCE_ANSWER_ATTACHMENT = "ANSWER_ATTACHMENT";
    private static final int ANSWER_PREVIEW_LENGTH = 160;
    private static final int DEFAULT_PREP_SECONDS = 0;
    private static final int DEFAULT_TOTAL_SECONDS = 3600;

    private final WritingQuestionMapper writingQuestionMapper;
    private final WritingRecordMapper writingRecordMapper;
    private final WritingRecordAttachmentMapper writingRecordAttachmentMapper;
    private final BizImageResourceService bizImageResourceService;
    private final WritingQuestionImageDescriptionService writingQuestionImageDescriptionService;

    public AdminWritingServiceImpl(WritingQuestionMapper writingQuestionMapper,
                                   WritingRecordMapper writingRecordMapper,
                                   WritingRecordAttachmentMapper writingRecordAttachmentMapper,
                                   BizImageResourceService bizImageResourceService,
                                   WritingQuestionImageDescriptionService writingQuestionImageDescriptionService) {
        this.writingQuestionMapper = writingQuestionMapper;
        this.writingRecordMapper = writingRecordMapper;
        this.writingRecordAttachmentMapper = writingRecordAttachmentMapper;
        this.bizImageResourceService = bizImageResourceService;
        this.writingQuestionImageDescriptionService = writingQuestionImageDescriptionService;
    }

    @Override
    @Transactional
    public WritingQuestionVO createQuestion(WritingQuestionDTO dto) {
        validateQuestionInput(dto);

        WritingQuestion question = new WritingQuestion();
        question.setTaskType(trimToNull(dto.getTaskType()));
        question.setChartType(trimToNull(dto.getChartType()));
        question.setTitle(trimToNull(dto.getTitle()));
        question.setDescription(trimToNull(dto.getDescription()));
        question.setImageDetailDescription(trimToNull(dto.getImageDetailDescription()));
        question.setPrepSeconds(requiredPrepSeconds(dto));
        question.setTotalSeconds(requiredTotalSeconds(dto));
        question.setIsDeleted(0);
        question.setDeletedTime(null);
        question.setCreatedTime(LocalDateTime.now());

        writingQuestionMapper.insert(question);
        List<BizImageResource> images = replaceQuestionImages(question.getId(), dto);
        updateQuestionImageDetailDescription(question, images);

        return enrichToVO(question);
    }

    @Override
    public List<WritingQuestionVO> listQuestions() {
        List<WritingQuestion> list = writingQuestionMapper.findAll();
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> question_ids = list.stream()
                .filter(Objects::nonNull)
                .map(WritingQuestion::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, List<BizImageResource>> image_map = bizImageResourceService.listByTargets(
                TARGET_TYPE_WRITING_QUESTION,
                question_ids
        );
        if (image_map == null) {
            image_map = Collections.emptyMap();
        }

        List<WritingQuestionVO> result = new ArrayList<>();
        for (WritingQuestion item : list) {
            if (item == null) {
                continue;
            }
            result.add(toQuestionVO(item, image_map.get(item.getId())));
        }
        return result;
    }

    @Override
    public WritingQuestionVO getQuestion(Long id) {
        WritingQuestion question = writingQuestionMapper.findByIdForAdmin(id);
        if (question == null) {
            throw new RuntimeException("Writing question not found");
        }
        return enrichToVO(question);
    }

    @Override
    @Transactional
    public WritingQuestionVO updateQuestion(Long id, WritingQuestionDTO dto) {
        validateQuestionInput(dto);

        WritingQuestion existing = writingQuestionMapper.findByIdForAdmin(id);
        if (existing == null) {
            throw new RuntimeException("Writing question not found");
        }

        existing.setTaskType(trimToNull(dto.getTaskType()));
        existing.setChartType(trimToNull(dto.getChartType()));
        existing.setTitle(trimToNull(dto.getTitle()));
        existing.setDescription(trimToNull(dto.getDescription()));
        existing.setImageDetailDescription(trimToNull(dto.getImageDetailDescription()));
        existing.setPrepSeconds(requiredPrepSeconds(dto));
        existing.setTotalSeconds(requiredTotalSeconds(dto));

        writingQuestionMapper.update(existing);
        List<BizImageResource> images = replaceQuestionImages(existing.getId(), dto);
        updateQuestionImageDetailDescription(existing, images);

        return enrichToVO(existing);
    }

    @Override
    @Transactional
    public List<BizImageResource> replaceQuestionImages(Long id, MultipartFile[] images) {
        WritingQuestion existing = writingQuestionMapper.findByIdForAdmin(id);
        if (existing == null) {
            throw new RuntimeException("Writing question not found");
        }
        List<BizImageResource> savedImages = bizImageResourceService.replaceByTargetFromUploads(
                TARGET_TYPE_WRITING_QUESTION,
                id,
                BucketType.WRITING_QUESTION,
                BIZ_PATH_WRITING_QUESTION_IMAGE,
                images
        );
        updateQuestionImageDetailDescription(existing, savedImages);
        if (savedImages == null || savedImages.isEmpty()) {
            existing.setImageDetailDescription(null);
            writingQuestionMapper.updateImageDetailDescription(id, null);
        }
        return savedImages;
    }

    @Override
    @Transactional
    public void deleteQuestion(Long id) {
        WritingQuestion question = writingQuestionMapper.findByIdForAdmin(id);
        if (question == null) {
            throw new RuntimeException("Writing question not found");
        }
        if (question.getIsDeleted() != null && question.getIsDeleted() == 1) {
            return;
        }
        writingQuestionMapper.logicalDeleteById(id, LocalDateTime.now());
        bizImageResourceService.deleteByTarget(TARGET_TYPE_WRITING_QUESTION, id);
    }

    @Override
    @Transactional
    public void restoreQuestion(Long id) {
        WritingQuestion question = writingQuestionMapper.findByIdForAdmin(id);
        if (question == null) {
            throw new RuntimeException("Writing question not found");
        }
        if (question.getIsDeleted() == null || question.getIsDeleted() == 0) {
            return;
        }
        writingQuestionMapper.restoreById(id);
    }

    @Override
    public WritingRecordDetailVO getRecord(Long recordId) {
        WritingRecord record = writingRecordMapper.findByIdForAdmin(recordId);
        if (record == null) {
            throw new RuntimeException("Writing record not found");
        }

        WritingQuestion question = writingQuestionMapper.findByIdForAdmin(record.getQuestionId());
        List<BizImageResource> question_images = new ArrayList<>();
        if (question != null && question.getId() != null) {
            List<BizImageResource> fetched_images = bizImageResourceService.listByTarget(
                    TARGET_TYPE_WRITING_QUESTION,
                    question.getId()
            );
            if (fetched_images != null) {
                question_images = fetched_images;
            }
        }

        List<WritingRecordAttachment> attachments = writingRecordAttachmentMapper.findByRecordId(recordId);
        if (attachments == null) {
            attachments = new ArrayList<>();
        }

        return buildDetailVO(record, question, question_images, attachments);
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
        if (record.getIsDeleted() == null || record.getIsDeleted() == 0) {
            return;
        }
        writingRecordMapper.restoreByIdForAdmin(recordId);
    }

    private List<BizImageResource> replaceQuestionImages(Long questionId, WritingQuestionDTO dto) {
        return bizImageResourceService.replaceByTarget(
                TARGET_TYPE_WRITING_QUESTION,
                questionId,
                BUCKET_KEY_WRITING_QUESTION,
                BIZ_PATH_WRITING_QUESTION_IMAGE,
                dto.getImages()
        );
    }

    private WritingQuestionVO enrichToVO(WritingQuestion question) {
        if (question == null) {
            return null;
        }
        List<BizImageResource> images = bizImageResourceService.listByTarget(
                TARGET_TYPE_WRITING_QUESTION,
                question.getId()
        );
        return toQuestionVO(question, images);
    }

    private WritingQuestionVO toQuestionVO(WritingQuestion question, List<BizImageResource> images) {
        WritingQuestionVO vo = new WritingQuestionVO();
        vo.setId(question.getId());
        vo.setTaskType(question.getTaskType());
        vo.setChartType(question.getChartType());
        vo.setTitle(question.getTitle());
        vo.setDescription(question.getDescription());
        vo.setImageDetailDescription(question.getImageDetailDescription());
        vo.setCreatedTime(question.getCreatedTime());
        vo.setPrepSeconds(resolvePrepSeconds(question));
        vo.setTotalSeconds(resolveTotalSeconds(question));
        vo.setPrepMinutes(secondsToMinutes(vo.getPrepSeconds()));
        vo.setTotalMinutes(secondsToMinutes(vo.getTotalSeconds()));

        List<BizImageResource> sorted_images = sortImages(images);
        vo.setImages(sorted_images);

        BizImageResource primary = sorted_images.isEmpty() ? null : sorted_images.get(0);
        vo.setImageUrl(primary == null ? null : trimToNull(primary.getFileUrl()));
        vo.setImageObjectKey(primary == null ? null : trimToNull(primary.getObjectKey()));

        return vo;
    }

    private List<BizImageResource> sortImages(List<BizImageResource> images) {
        if (images == null || images.isEmpty()) {
            return new ArrayList<>();
        }
        return images.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(
                        BizImageResource::getSortOrder,
                        Comparator.nullsLast(Integer::compareTo)
                ).thenComparing(
                        BizImageResource::getId,
                        Comparator.nullsLast(Long::compareTo)
                ))
                .collect(Collectors.toList());
    }

    private WritingRecordDetailVO buildDetailVO(WritingRecord record,
                                                WritingQuestion question,
                                                List<BizImageResource> question_images,
                                                List<WritingRecordAttachment> attachments) {
        List<BizImageResource> sortedQuestionImages = sortImages(question_images);
        List<WritingRecordAttachment> sortedAttachments = sortAttachments(attachments);

        WritingRecordDetailVO vo = new WritingRecordDetailVO();
        vo.setRecordId(record.getId());
        vo.setQuestionId(record.getQuestionId());
        vo.setQuestionTitle(question == null ? null : question.getTitle());
        vo.setQuestionDescription(question == null ? null : question.getDescription());
        vo.setPrompt(question == null ? null : question.getDescription());
        vo.setImageDetailDescription(question == null ? null : question.getImageDetailDescription());
        vo.setQuestionImageUrl(resolveQuestionImageUrl(question, sortedQuestionImages));
        vo.setQuestionImages(sortedQuestionImages);
        vo.setPreviewAssets(buildPreviewAssets(sortedQuestionImages, sortedAttachments));
        vo.setTaskType(question == null ? null : question.getTaskType());
        vo.setChartType(question == null ? null : question.getChartType());
        vo.setInputType(record.getInputType());
        vo.setTextContent(record.getTextContent());
        vo.setExtractedText(record.getExtractedText());
        vo.setAnswerPreview(buildAnswerPreview(record));
        vo.setAttachmentCount(sortedAttachments.size());
        vo.setTargetScore(record.getTargetScore());
        vo.setAiScore(record.getAiScore());
        vo.setAiFeedback(record.getAiFeedback());
        vo.setAiStatus(record.getAiStatus());
        vo.setAiProvider(record.getAiProvider());
        vo.setAiModel(record.getAiModel());
        vo.setIsDeleted(record.getIsDeleted());
        vo.setDeletedTime(record.getDeletedTime());
        vo.setCreatedTime(record.getCreatedTime());

        List<WritingAttachmentVO> attachment_vo_list = new ArrayList<>();
        for (WritingRecordAttachment attachment : sortedAttachments) {
            WritingAttachmentVO attachment_vo = new WritingAttachmentVO();
            attachment_vo.setId(attachment.getId());
            attachment_vo.setFileType(attachment.getFileType());
            attachment_vo.setFileUrl(attachment.getFileUrl());
            attachment_vo.setSortOrder(attachment.getSortOrder());
            attachment_vo.setCreatedTime(attachment.getCreatedTime());
            attachment_vo.setOcrText(attachment.getOcrText());
            attachment_vo_list.add(attachment_vo);
        }
        vo.setAttachments(attachment_vo_list);

        return vo;
    }

    private List<WritingRecordAttachment> sortAttachments(List<WritingRecordAttachment> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return new ArrayList<>();
        }
        return attachments.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(
                        WritingRecordAttachment::getSortOrder,
                        Comparator.nullsLast(Integer::compareTo)
                ).thenComparing(
                        WritingRecordAttachment::getId,
                        Comparator.nullsLast(Long::compareTo)
                ))
                .collect(Collectors.toList());
    }

    private List<WritingPreviewAssetVO> buildPreviewAssets(List<BizImageResource> questionImages,
                                                           List<WritingRecordAttachment> attachments) {
        List<WritingPreviewAssetVO> previewAssets = new ArrayList<>();
        int displayOrder = 1;
        for (BizImageResource image : sortImages(questionImages)) {
            String fileUrl = trimToNull(image.getFileUrl());
            if (fileUrl == null) {
                continue;
            }
            WritingPreviewAssetVO asset = new WritingPreviewAssetVO();
            asset.setSourceType(PREVIEW_SOURCE_QUESTION_IMAGE);
            asset.setFileType(FILE_TYPE_IMAGE);
            asset.setFileUrl(fileUrl);
            asset.setSortOrder(displayOrder);
            asset.setLabel("Question image " + displayOrder);
            previewAssets.add(asset);
            displayOrder++;
        }

        for (WritingRecordAttachment attachment : sortAttachments(attachments)) {
            String fileUrl = trimToNull(attachment.getFileUrl());
            if (fileUrl == null) {
                continue;
            }
            WritingPreviewAssetVO asset = new WritingPreviewAssetVO();
            asset.setSourceType(PREVIEW_SOURCE_ANSWER_ATTACHMENT);
            asset.setFileType(attachment.getFileType());
            asset.setFileUrl(fileUrl);
            asset.setSortOrder(displayOrder);
            asset.setLabel("Answer attachment " + attachment.getSortOrder());
            previewAssets.add(asset);
            displayOrder++;
        }
        return previewAssets;
    }

    private void updateQuestionImageDetailDescription(WritingQuestion question, List<BizImageResource> images) {
        if (question == null || question.getId() == null) {
            return;
        }
        if (images == null || images.isEmpty()) {
            return;
        }
        String imageDetailDescription = writingQuestionImageDescriptionService.describeQuestionImages(question, images);
        question.setImageDetailDescription(imageDetailDescription);
        writingQuestionMapper.updateImageDetailDescription(question.getId(), imageDetailDescription);
    }

    private String resolveQuestionImageUrl(WritingQuestion question, List<BizImageResource> question_images) {
        if (question_images != null && !question_images.isEmpty()) {
            List<BizImageResource> sorted_images = sortImages(question_images);
            if (!sorted_images.isEmpty()) {
                return trimToNull(sorted_images.get(0).getFileUrl());
            }
        }
        return null;
    }

    private String buildAnswerPreview(WritingRecord record) {
        String source = INPUT_TYPE_TEXT.equals(record.getInputType())
                ? record.getTextContent()
                : record.getExtractedText();
        String trimmed = trimToNull(source);
        if (trimmed == null) {
            return null;
        }
        return trimmed.length() <= ANSWER_PREVIEW_LENGTH
                ? trimmed
                : trimmed.substring(0, ANSWER_PREVIEW_LENGTH);
    }

    private void validateQuestionInput(WritingQuestionDTO dto) {
        if (dto == null) {
            throw new RuntimeException("Request body is required");
        }
        if (trimToNull(dto.getTaskType()) == null) {
            throw new RuntimeException("Task type cannot be empty");
        }
        if (trimToNull(dto.getTitle()) == null) {
            throw new RuntimeException("Title cannot be empty");
        }
        requiredPrepSeconds(dto);
        requiredTotalSeconds(dto);
    }

    private Integer requiredPrepSeconds(WritingQuestionDTO dto) {
        Integer prepSeconds = dto.getPrepSeconds();
        if (prepSeconds != null) {
            if (prepSeconds < 0) {
                throw new RuntimeException("prepSeconds cannot be negative");
            }
            return prepSeconds;
        }
        return minutesToSeconds(requiredPrepMinutes(dto.getPrepMinutes()));
    }

    private Integer requiredTotalSeconds(WritingQuestionDTO dto) {
        Integer totalSeconds = dto.getTotalSeconds();
        if (totalSeconds != null) {
            if (totalSeconds <= 0) {
                throw new RuntimeException("totalSeconds must be greater than 0");
            }
            return totalSeconds;
        }
        return minutesToSeconds(requiredTotalMinutes(dto.getTotalMinutes()));
    }

    private Integer requiredPrepMinutes(Integer prepMinutes) {
        if (prepMinutes == null) {
            throw new RuntimeException("prepMinutes is required");
        }
        if (prepMinutes < 0) {
            throw new RuntimeException("prepMinutes cannot be negative");
        }
        return prepMinutes;
    }

    private Integer requiredTotalMinutes(Integer totalMinutes) {
        if (totalMinutes == null) {
            throw new RuntimeException("totalMinutes is required");
        }
        if (totalMinutes <= 0) {
            throw new RuntimeException("totalMinutes must be greater than 0");
        }
        return totalMinutes;
    }

    private Integer minutesToSeconds(Integer minutes) {
        return minutes == null ? null : minutes * 60;
    }

    private Integer secondsToMinutes(Integer seconds) {
        return seconds == null ? null : seconds / 60;
    }

    private Integer resolvePrepSeconds(WritingQuestion question) {
        if (question == null || question.getPrepSeconds() == null || question.getPrepSeconds() < 0) {
            return DEFAULT_PREP_SECONDS;
        }
        return question.getPrepSeconds();
    }

    private Integer resolveTotalSeconds(WritingQuestion question) {
        if (question == null || question.getTotalSeconds() == null || question.getTotalSeconds() <= 0) {
            return DEFAULT_TOTAL_SECONDS;
        }
        return question.getTotalSeconds();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

}

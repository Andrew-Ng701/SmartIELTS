package com.andrew.smartielts.common.image.service.impl;

import com.andrew.smartielts.common.image.domain.dto.BizImageResourceDTO;
import com.andrew.smartielts.common.image.domain.pojo.BizImageResource;
import com.andrew.smartielts.common.image.mapper.BizImageResourceMapper;
import com.andrew.smartielts.common.image.service.BizImageResourceService;
import com.andrew.smartielts.common.storage.BucketType;
import com.andrew.smartielts.common.storage.UploadResult;
import com.andrew.smartielts.common.storage.service.OssStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class BizImageResourceServiceImpl implements BizImageResourceService {

    private static final Logger log = LoggerFactory.getLogger(BizImageResourceServiceImpl.class);
    private static final List<String> ALLOWED_IMAGE_CONTENT_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    private final BizImageResourceMapper bizImageResourceMapper;
    private final OssStorageService ossStorageService;

    public BizImageResourceServiceImpl(BizImageResourceMapper bizImageResourceMapper,
                                       OssStorageService ossStorageService) {
        this.bizImageResourceMapper = bizImageResourceMapper;
        this.ossStorageService = ossStorageService;
    }

    @Override
    public List<BizImageResource> listByTarget(String targetType, Long targetId) {
        if (isBlank(targetType)) {
            throw new RuntimeException("targetType is required");
        }
        if (targetId == null) {
            throw new RuntimeException("targetId is required");
        }
        return bizImageResourceMapper.findActiveByTarget(targetType.trim(), targetId);
    }

    @Override
    public Map<Long, List<BizImageResource>> listByTargets(String targetType, List<Long> targetIds) {
        if (isBlank(targetType)) {
            throw new RuntimeException("targetType is required");
        }
        if (targetIds == null || targetIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> filteredIds = targetIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (filteredIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<BizImageResource> list = bizImageResourceMapper.findActiveByTargets(targetType.trim(), filteredIds);
        Map<Long, List<BizImageResource>> result = new LinkedHashMap<>();
        for (BizImageResource item : list) {
            if (item == null || item.getTargetId() == null) {
                continue;
            }
            result.computeIfAbsent(item.getTargetId(), k -> new ArrayList<>()).add(item);
        }
        return result;
    }

    @Override
    @Transactional
    public List<BizImageResource> replaceByTarget(String targetType,
                                                  Long targetId,
                                                  String bucketType,
                                                  String bizPath,
                                                  List<BizImageResourceDTO> images) {
        if (isBlank(targetType)) {
            throw new RuntimeException("targetType is required");
        }
        if (targetId == null) {
            throw new RuntimeException("targetId is required");
        }
        if (isBlank(bucketType)) {
            throw new RuntimeException("bucketType is required");
        }
        if (isBlank(bizPath)) {
            throw new RuntimeException("bizPath is required");
        }

        bizImageResourceMapper.deleteByTarget(targetType.trim(), targetId);

        List<BizImageResource> saved = new ArrayList<>();
        if (images == null || images.isEmpty()) {
            return saved;
        }

        int index = 1;
        for (BizImageResourceDTO dto : images) {
            if (dto == null) {
                continue;
            }

            String objectKey = trimToNull(dto.getObjectKey());
            String fileUrl = trimToNull(dto.getFileUrl());
            if (objectKey == null && fileUrl == null) {
                continue;
            }

            BizImageResource entity = new BizImageResource();
            entity.setTargetType(targetType.trim());
            entity.setTargetId(targetId);
            entity.setBucketType(bucketType.trim());
            entity.setBizPath(bizPath.trim());
            entity.setObjectKey(objectKey);
            entity.setFileUrl(fileUrl);
            entity.setOriginalName(trimToNull(dto.getOriginalName()));
            entity.setContentType(trimToNull(dto.getContentType()));
            entity.setFileSize(dto.getFileSize());
            entity.setWidth(dto.getWidth());
            entity.setHeight(dto.getHeight());
            entity.setSortOrder(dto.getSortOrder() == null ? index : dto.getSortOrder());
            entity.setCreatedTime(LocalDateTime.now());
            entity.setIsDeleted(0);

            bizImageResourceMapper.insert(entity);
            saved.add(entity);
            index++;
        }

        return saved;
    }

    @Override
    @Transactional
    public List<BizImageResource> replaceByTargetFromUploads(String targetType,
                                                             Long targetId,
                                                             BucketType bucketType,
                                                             String bizPath,
                                                             MultipartFile[] images) {
        validateTarget(targetType, targetId);
        if (bucketType == null) {
            throw new RuntimeException("bucketType is required");
        }
        if (isBlank(bizPath)) {
            throw new RuntimeException("bizPath is required");
        }

        List<BizImageResource> existing = listByTarget(targetType, targetId);
        List<MultipartFile> uploadFiles = normalizeUploadFiles(images);
        if (uploadFiles.isEmpty()) {
            bizImageResourceMapper.deleteByTarget(targetType.trim(), targetId);
            deleteObjectsQuietly(bucketType, existing);
            return new ArrayList<>();
        }

        List<BizImageResource> uploaded = new ArrayList<>();
        int sortOrder = 1;
        for (MultipartFile file : uploadFiles) {
            validateImageFile(file);
            UploadResult upload = ossStorageService.upload(file, bucketType, bizPath);

            BizImageResource entity = new BizImageResource();
            entity.setTargetType(targetType.trim());
            entity.setTargetId(targetId);
            entity.setBucketType(bucketType.getKey());
            entity.setBizPath(bizPath.trim());
            entity.setObjectKey(trimToNull(upload.getFileKey()));
            entity.setFileUrl(trimToNull(upload.getFileUrl()));
            entity.setOriginalName(trimToNull(file.getOriginalFilename()));
            entity.setContentType(trimToNull(file.getContentType()));
            entity.setFileSize(file.getSize());
            entity.setSortOrder(sortOrder++);
            entity.setCreatedTime(LocalDateTime.now());
            entity.setIsDeleted(0);
            uploaded.add(entity);
        }

        bizImageResourceMapper.deleteByTarget(targetType.trim(), targetId);
        for (BizImageResource entity : uploaded) {
            bizImageResourceMapper.insert(entity);
        }
        deleteObjectsQuietly(bucketType, existing);
        return uploaded;
    }

    @Override
    @Transactional
    public void deleteByTarget(String targetType, Long targetId) {
        validateTarget(targetType, targetId);
        bizImageResourceMapper.deleteByTarget(targetType.trim(), targetId);
    }

    @Override
    @Transactional
    public void deleteByTargetAndObjects(String targetType, Long targetId, BucketType bucketType) {
        validateTarget(targetType, targetId);
        if (bucketType == null) {
            throw new RuntimeException("bucketType is required");
        }
        List<BizImageResource> existing = listByTarget(targetType, targetId);
        bizImageResourceMapper.deleteByTarget(targetType.trim(), targetId);
        deleteObjectsQuietly(bucketType, existing);
    }

    private void validateTarget(String targetType, Long targetId) {
        if (isBlank(targetType)) {
            throw new RuntimeException("targetType is required");
        }
        if (targetId == null) {
            throw new RuntimeException("targetId is required");
        }
    }

    private List<MultipartFile> normalizeUploadFiles(MultipartFile[] images) {
        if (images == null || images.length == 0) {
            return new ArrayList<>();
        }
        List<MultipartFile> files = new ArrayList<>();
        for (MultipartFile image : images) {
            if (image == null || image.isEmpty()) {
                continue;
            }
            files.add(image);
        }
        return files;
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("image_file_is_required");
        }
        String contentType = trimToNull(file.getContentType());
        if (contentType == null || !ALLOWED_IMAGE_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new RuntimeException("invalid_image_content_type");
        }
    }

    private void deleteObjectsQuietly(BucketType bucketType, List<BizImageResource> images) {
        if (images == null || images.isEmpty()) {
            return;
        }
        for (BizImageResource image : images) {
            String objectKey = image == null ? null : trimToNull(image.getObjectKey());
            if (objectKey == null) {
                continue;
            }
            try {
                ossStorageService.delete(bucketType, objectKey);
            } catch (Exception e) {
                log.warn("Failed to delete image object, bucketType={}, objectKey={}", bucketType.getKey(), objectKey, e);
            }
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return trimToNull(value) == null;
    }
}

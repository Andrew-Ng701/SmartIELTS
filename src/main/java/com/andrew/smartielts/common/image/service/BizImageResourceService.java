package com.andrew.smartielts.common.image.service;

import com.andrew.smartielts.common.image.domain.dto.BizImageResourceDTO;
import com.andrew.smartielts.common.image.domain.pojo.BizImageResource;
import com.andrew.smartielts.common.storage.BucketType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface BizImageResourceService {

    List<BizImageResource> listByTarget(String targetType, Long targetId);

    Map<Long, List<BizImageResource>> listByTargets(String targetType, List<Long> targetIds);

    List<BizImageResource> replaceByTarget(String targetType, Long targetId, String bucketType, String bizPath,
                                           List<BizImageResourceDTO> images);

    List<BizImageResource> replaceByTargetFromUploads(String targetType, Long targetId, BucketType bucketType,
                                                      String bizPath, MultipartFile[] images);

    void deleteByTarget(String targetType, Long targetId);

    void deleteByTargetAndObjects(String targetType, Long targetId, BucketType bucketType);
}

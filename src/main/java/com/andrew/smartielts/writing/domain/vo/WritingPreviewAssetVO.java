package com.andrew.smartielts.writing.domain.vo;

import lombok.Data;

@Data
public class WritingPreviewAssetVO {

    private String sourceType;

    private String fileType;

    private String fileUrl;

    private Integer sortOrder;

    private String label;
}

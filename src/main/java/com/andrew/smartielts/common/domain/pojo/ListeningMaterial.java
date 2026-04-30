package com.andrew.smartielts.common.domain.pojo;

import lombok.Data;

@Data
public class ListeningMaterial {
    private Long id;
    private Long testId;
    private Long partGroupId;
    private String title;
    private String audioUrl;
    private String audioObjectKey;
    private String transcriptText;
    private Integer displayOrder;
    private Integer isDeleted;
}
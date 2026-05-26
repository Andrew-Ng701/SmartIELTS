package com.andrew.smartielts.reading.domain.dto;

import lombok.Data;

@Data
public class ReadingPassageDTO {
    private Long id;
    private String clientKey;
    private Long testId;
    private Long partGroupId;
    private Integer passageNo;
    private String title;
    private String content;
    private String materialType;
    private Integer displayOrder;
}

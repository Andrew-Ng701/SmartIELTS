package com.andrew.smartielts.reading.domain.pojo;

import lombok.Data;

@Data
public class ReadingPassage {
    private Long id;
    private Long testId;
    private Long partGroupId;
    private Integer passageNo;
    private String title;
    private String content;
    private String materialType;
    private Integer displayOrder;
    private Integer isDeleted;
}
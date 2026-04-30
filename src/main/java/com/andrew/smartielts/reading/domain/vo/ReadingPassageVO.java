package com.andrew.smartielts.reading.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class ReadingPassageVO {
    private Long id;
    private Long partGroupId;
    private Integer passageNo;
    private String title;
    private String content;
    private String materialType;
    private Integer displayOrder;
    private List<ReadingQuestionVO> questions;
}
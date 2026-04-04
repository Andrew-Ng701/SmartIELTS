package com.andrew.smartielts.reading.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReadingRecordVO {
    private Long id;
    private Long userId;
    private Long testId;
    private String testTitle;
    private Integer totalScore;
    private LocalDateTime createdTime;
    private Integer isDeleted;
}

package com.andrew.smartielts.writing.domain.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WritingQuestion {
    private Long id;
    private String taskType;
    private String chartType;
    private String title;
    private String description;
    private String imageDetailDescription;
    private Integer prepSeconds;
    private Integer totalSeconds;
    private Integer isDeleted;
    private LocalDateTime deletedTime;
    private LocalDateTime createdTime;
}

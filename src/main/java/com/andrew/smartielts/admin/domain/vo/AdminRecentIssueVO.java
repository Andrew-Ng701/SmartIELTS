package com.andrew.smartielts.admin.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminRecentIssueVO {
    private String module;
    private String type;
    private Long recordId;
    private String sessionId;
    private Long questionId;
    private String aiStatus;
    private String aiProvider;
    private String aiModel;
    private String message;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
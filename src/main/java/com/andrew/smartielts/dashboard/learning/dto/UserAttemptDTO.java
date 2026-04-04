package com.andrew.smartielts.dashboard.learning.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserAttemptDTO {

    private String module;
    private Long userId;
    private Long recordId;
    private Long questionId;

    private String userAnswer;
    private Boolean correct;
    private Object score;
    private String feedback;

    private String createdTime;
}
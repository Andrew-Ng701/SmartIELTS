package com.andrew.smartielts.speaking.domain.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SpeakingTalk {
    private Long id;
    private String talkId;
    private Long userId;
    private String sessionId;
    private Long questionId;
    private String talkStatus;
    private String videoUrl;
    private String errorMessage;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}

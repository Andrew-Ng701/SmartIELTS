package com.andrew.smartielts.speaking.domain.vo;

import lombok.Data;

@Data
public class SpeakingTalkStatusVO {
    private String talkId;
    private String talkStatus;
    private String videoUrl;
    private String errorMessage;
}

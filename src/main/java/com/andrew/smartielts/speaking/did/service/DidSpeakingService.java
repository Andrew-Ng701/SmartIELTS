package com.andrew.smartielts.speaking.did.service;

import com.andrew.smartielts.speaking.domain.vo.SpeakingTalkStatusVO;

public interface DidSpeakingService {
    String createTalk(String scriptText);

    SpeakingTalkStatusVO getTalkStatus(String talkId);
}

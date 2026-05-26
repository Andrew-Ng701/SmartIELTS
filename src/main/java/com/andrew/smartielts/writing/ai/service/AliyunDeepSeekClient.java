package com.andrew.smartielts.writing.ai.service;

public interface AliyunDeepSeekClient {
    String chat(String prompt);

    String chatWithImage(String prompt, String imageUrl);
}

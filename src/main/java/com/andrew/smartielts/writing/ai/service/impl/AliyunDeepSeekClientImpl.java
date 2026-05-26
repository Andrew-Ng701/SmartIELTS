package com.andrew.smartielts.writing.ai.service.impl;

import com.andrew.smartielts.writing.ai.AiProperties;
import com.andrew.smartielts.writing.ai.service.AliyunDeepSeekClient;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AliyunDeepSeekClientImpl implements AliyunDeepSeekClient {

    @Autowired
    private AiProperties aiProperties;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(300, TimeUnit.SECONDS)
            .build();

    @Override
    public String chat(String prompt) {
        String requestJson = """
                {
                  "model": "%s",
                  "messages": [
                    {
                      "role": "user",
                      "content": "%s"
                    }
                  ],
                  "enable_thinking": false,
                  "temperature": 0.2
                }
                """.formatted(
                escapeJson(requireModel()),
                escapeJson(prompt)
        );
        return executeChatRequest(requestJson);
    }

    @Override
    public String chatWithImage(String prompt, String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new RuntimeException("imageUrl is required");
        }
        String requestJson = """
                {
                  "model": "%s",
                  "messages": [
                    {
                      "role": "user",
                      "content": [
                        {
                          "type": "text",
                          "text": "%s"
                        },
                        {
                          "type": "image_url",
                          "image_url": {
                            "url": "%s"
                          }
                        }
                      ]
                    }
                  ],
                  "enable_thinking": false,
                  "temperature": 0.1
                }
                """.formatted(
                escapeJson(requireModel()),
                escapeJson(prompt),
                escapeJson(imageUrl)
        );
        return executeChatRequest(requestJson);
    }

    private String executeChatRequest(String requestJson) {
        String baseUrl = aiProperties.getBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new RuntimeException("AI baseUrl is required");
        }
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        String apiKey = aiProperties.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("AI apiKey is required");
        }

        String url = baseUrl + "/chat/completions";
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(requestJson, MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                log.error("AI request failed, code={}, body={}", response.code(), body);
                throw new RuntimeException("AI request failed: HTTP code = " + response.code() + ", body = " + body);
            }
            if (body == null || body.isBlank()) {
                throw new RuntimeException("AI response body is empty");
            }
            return body;
        } catch (IOException e) {
            log.error("AI request exception", e);
            throw new RuntimeException("AI request exception: " + e.getMessage(), e);
        }
    }

    private String requireModel() {
        String model = aiProperties.getModel();
        if (model == null || model.isBlank()) {
            throw new RuntimeException("AI model is required");
        }
        return model;
    }

    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}

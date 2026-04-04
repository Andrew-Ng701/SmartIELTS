package com.andrew.smartielts.dashboard.agent.intent;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "aliyun.dashboard.intent.llm")
public class DashboardIntentLlmProperties {

    /**
     * OpenAI-compatible base url
     * Example: https://dashscope.aliyuncs.com/compatible-mode/v1
     */
    private String baseUrl;

    /**
     * DashScope API Key
     */
    private String apiKey;

    /**
     * Model name
     * Example: qwen-max / qwen-plus / qwen-turbo
     */
    private String model;

    /**
     * Connect timeout milliseconds
     */
    private Integer connectTimeoutMs = 10000;

    /**
     * Read timeout milliseconds
     */
    private Integer readTimeoutMs = 30000;
}
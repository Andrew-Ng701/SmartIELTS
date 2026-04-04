package com.andrew.smartielts.dashboard.agent.intent.config;

import com.andrew.smartielts.dashboard.agent.intent.DashboardIntentLlmProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class DashboardIntentHttpConfig {

    @Bean
    public RestTemplate dashboardIntentRestTemplate(DashboardIntentLlmProperties llmProperties) {
        int connectTimeoutMs = llmProperties.getConnectTimeoutMs() == null
                ? 10000
                : llmProperties.getConnectTimeoutMs();

        int readTimeoutMs = llmProperties.getReadTimeoutMs() == null
                ? 30000
                : llmProperties.getReadTimeoutMs();

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);

        return new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofMillis(connectTimeoutMs))
                .setReadTimeout(Duration.ofMillis(readTimeoutMs))
                .requestFactory(() -> requestFactory)
                .build();
    }
}
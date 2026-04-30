package com.andrew.smartielts.dashboard.agent.answer.llm;

import com.andrew.smartielts.dashboard.agent.answer.DashboardAnswerReviewPromptConstants;
import com.andrew.smartielts.dashboard.agent.answer.dto.DashboardAnswerReviewRequest;
import com.andrew.smartielts.dashboard.agent.answer.dto.DashboardAnswerReviewResult;
import com.andrew.smartielts.dashboard.agent.intent.DashboardIntentLlmProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DashScopeDashboardAnswerReviewClient implements DashboardAnswerReviewLlmClient {

    private static final String RESPONSE_FORMAT_TYPE = "json_schema";
    private static final String JSON_SCHEMA_NAME = "dashboard_answer_review";

    private final RestTemplate dashboardIntentRestTemplate;
    private final ObjectMapper objectMapper;
    private final DashboardIntentLlmProperties llmProperties;

    @Override
    public DashboardAnswerReviewResult review(DashboardAnswerReviewRequest request) {
        String url = normalize_base_url(llmProperties.getBaseUrl()) + "/chat/completions";

        ChatCompletionsRequest payload = new ChatCompletionsRequest();
        payload.setModel(llmProperties.getModel());
        payload.setTemperature(0.0D);
        payload.setEnableThinking(Boolean.FALSE);
        payload.setMessages(List.of(
                new ChatMessage("system", DashboardAnswerReviewPromptConstants.SYSTEM_PROMPT),
                new ChatMessage("user", build_user_prompt(request))
        ));
        payload.setResponseFormat(build_response_format());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(llmProperties.getApiKey());

        HttpEntity<ChatCompletionsRequest> entity = new HttpEntity<>(payload, headers);
        ResponseEntity<ChatCompletionsResponse> response = dashboardIntentRestTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                ChatCompletionsResponse.class
        );

        String content = extract_content(response.getBody());

        try {
            DashboardAnswerReviewResult result = objectMapper.readValue(content, DashboardAnswerReviewResult.class);
            normalize_result(result);
            return result;
        } catch (JsonProcessingException e) {
            log.error("failed to parse dashboard_answer_review_json: {}", content, e);
            throw new IllegalStateException("invalid dashboard_answer_review_json returned by dashscope", e);
        }
    }

    private String build_user_prompt(DashboardAnswerReviewRequest request) {
        try {
            return """
                    Review the current dashboard query result.

                    role=%s
                    operator_user_id=%s
                    target_user_id=%s
                    original_query=%s
                    capability=%s
                    filters=%s
                    data=%s
                    """.formatted(
                    safe_string(request.getRole()),
                    String.valueOf(request.getOperatorUserId()),
                    String.valueOf(request.getTargetUserId()),
                    safe_string(request.getOriginalQuery()),
                    safe_string(request.getCapability()),
                    objectMapper.writeValueAsString(request.getFilters() == null ? Map.of() : request.getFilters()),
                    objectMapper.writeValueAsString(request.getData())
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to serialize dashboard_answer_review_request", e);
        }
    }

    private ResponseFormat build_response_format() {
        JsonSchema jsonSchema = new JsonSchema();
        jsonSchema.setName(JSON_SCHEMA_NAME);
        jsonSchema.setSchema(Map.of(
                "type", "object",
                "additionalProperties", false,
                "required", List.of("action", "reviewSummary", "retryFilters", "exitMessage", "suggestions"),
                "properties", Map.of(
                        "action", Map.of(
                                "type", "string",
                                "enum", List.of("PROCEED", "RETRY_QUERY", "EXIT")
                        ),
                        "reviewSummary", Map.of("type", "string"),
                        "retryFilters", Map.of("type", "object"),
                        "exitMessage", Map.of("type", List.of("string", "null")),
                        "suggestions", Map.of(
                                "type", "array",
                                "items", Map.of("type", "string")
                        )
                )
        ));

        ResponseFormat responseFormat = new ResponseFormat();
        responseFormat.setType(RESPONSE_FORMAT_TYPE);
        responseFormat.setJsonSchema(jsonSchema);
        return responseFormat;
    }

    private String extract_content(ChatCompletionsResponse body) {
        if (body == null || body.getChoices() == null || body.getChoices().isEmpty()) {
            throw new IllegalStateException("dashscope review returned empty response");
        }
        if (body.getChoices().get(0).getMessage() == null) {
            throw new IllegalStateException("dashscope review returned empty message");
        }
        String content = body.getChoices().get(0).getMessage().getContent();
        if (content == null || content.isBlank()) {
            throw new IllegalStateException("dashscope review returned empty content");
        }
        return content;
    }

    private void normalize_result(DashboardAnswerReviewResult result) {
        if (result == null) {
            throw new IllegalStateException("dashboard_answer_review_result is null");
        }
        if (result.getRetryFilters() == null) {
            result.setRetryFilters(Map.of());
        }
        if (result.getSuggestions() == null) {
            result.setSuggestions(List.of());
        }
    }

    private String normalize_base_url(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("smartielts.dashboard.intent.llm.base_url is not configured");
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private String safe_string(String value) {
        return value == null ? "" : value;
    }

    @Data
    static class ChatCompletionsRequest {
        private String model;
        private Double temperature;
        private Boolean enableThinking;
        private List<ChatMessage> messages;
        private ResponseFormat responseFormat;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class ChatMessage {
        private String role;
        private String content;
    }

    @Data
    static class ResponseFormat {
        private String type;
        private JsonSchema jsonSchema;
    }

    @Data
    static class JsonSchema {
        private String name;
        private Map<String, Object> schema;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ChatCompletionsResponse {
        private List<Choice> choices;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Choice {
        private Message message;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Message {
        private String role;
        private String content;
    }
}
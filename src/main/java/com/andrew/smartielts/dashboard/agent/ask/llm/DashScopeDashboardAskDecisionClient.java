package com.andrew.smartielts.dashboard.agent.ask.llm;

import com.andrew.smartielts.dashboard.agent.ask.DashboardAskDecisionPromptConstants;
import com.andrew.smartielts.dashboard.agent.ask.DashboardAskDecisionPromptTemplates;
import com.andrew.smartielts.dashboard.agent.ask.DashboardAskDecisionResponseFormatConstants;
import com.andrew.smartielts.dashboard.agent.ask.DashboardAskDecisionSchemaConstants;
import com.andrew.smartielts.dashboard.agent.ask.dto.DashboardAskDecisionRequest;
import com.andrew.smartielts.dashboard.agent.ask.dto.DashboardAskDecisionResult;
import com.andrew.smartielts.dashboard.agent.intent.DashboardIntentLlmProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
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
public class DashScopeDashboardAskDecisionClient implements DashboardAskDecisionLlmClient {

    private final RestTemplate dashboardIntentRestTemplate;
    private final ObjectMapper objectMapper;
    private final DashboardIntentLlmProperties llmProperties;

    @Override
    public DashboardAskDecisionResult decide(DashboardAskDecisionRequest request) {
        String url = normalizeBaseUrl(llmProperties.getBaseUrl()) + "/chat/completions";

        ChatCompletionsRequest payload = new ChatCompletionsRequest();
        payload.setModel(llmProperties.getModel());
        payload.setTemperature(0.0);
        payload.setMessages(List.of(
                new ChatMessage("system", DashboardAskDecisionPromptConstants.SYSTEM_PROMPT),
                new ChatMessage("user", buildUserPrompt(request))
        ));
        payload.setResponseFormat(buildResponseFormat());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(llmProperties.getApiKey());

        HttpEntity<ChatCompletionsRequest> entity = new HttpEntity<>(payload, headers);
        ResponseEntity<ChatCompletionsResponse> response = dashboardIntentRestTemplate.exchange(
                url, HttpMethod.POST, entity, ChatCompletionsResponse.class);

        ChatCompletionsResponse body = response.getBody();
        if (body == null || body.getChoices() == null || body.getChoices().isEmpty()) {
            throw new IllegalStateException("DashScope ask decision returned empty response");
        }

        String content = body.getChoices().get(0).getMessage() == null
                ? null
                : body.getChoices().get(0).getMessage().getContent();

        if (content == null || content.isBlank()) {
            throw new IllegalStateException("DashScope ask decision returned empty content");
        }

        try {
            DashboardAskDecisionResult result = objectMapper.readValue(content, DashboardAskDecisionResult.class);
            normalizeResult(result);
            return result;
        } catch (JsonProcessingException e) {
            log.error("Failed to parse dashboard ask decision JSON: {}", content, e);
            throw new IllegalStateException("Invalid ask decision JSON returned by DashScope", e);
        }
    }

    private String buildUserPrompt(DashboardAskDecisionRequest request) {
        try {
            return DashboardAskDecisionPromptTemplates.USER_PROMPT_TEMPLATE.formatted(
                    safeString(request.getRole()),
                    String.valueOf(request.getOperatorUserId()),
                    String.valueOf(request.getTargetUserId()),
                    safeString(request.getResponseLanguage()),
                    safeString(request.getAskScene()),
                    safeString(request.getResponseMode()),
                    safeString(request.getQuery()),
                    objectMapper.writeValueAsString(request.getObjectRef() == null ? Map.of() : request.getObjectRef()),
                    objectMapper.writeValueAsString(request.getPreloadedPayload() == null ? Map.of() : request.getPreloadedPayload()),
                    objectMapper.writeValueAsString(request.getClientContext() == null ? Map.of() : request.getClientContext()),
                    objectMapper.writeValueAsString(request.getContext() == null ? Map.of() : request.getContext()),
                    objectMapper.writeValueAsString(request.getLearningContext() == null ? Map.of() : request.getLearningContext())
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize ask decision request", e);
        }
    }

    @SuppressWarnings("unchecked")
    private ResponseFormat buildResponseFormat() {
        try {
            JsonSchema jsonSchema = new JsonSchema();
            jsonSchema.setName(DashboardAskDecisionResponseFormatConstants.RESPONSE_FORMAT_SCHEMA_NAME);
            jsonSchema.setSchema(objectMapper.readValue(
                    DashboardAskDecisionSchemaConstants.DASHSCOPE_ASK_DECISION_JSON_SCHEMA, Map.class));

            ResponseFormat responseFormat = new ResponseFormat();
            responseFormat.setType(DashboardAskDecisionResponseFormatConstants.RESPONSE_FORMAT_TYPE);
            responseFormat.setJsonSchema(jsonSchema);
            return responseFormat;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Invalid dashboard ask decision schema constant", e);
        }
    }

    private void normalizeResult(DashboardAskDecisionResult result) {
        if (result.getAction() == null) {
            result.setAction("GENERATE_SQL");
        }
        if (result.getSufficient() == null) {
            result.setSufficient(Boolean.FALSE);
        }
        if (result.getFilters() == null) {
            result.setFilters(Map.of());
        }
        if (result.getRequiredDataScopes() == null) {
            result.setRequiredDataScopes(List.of());
        }
        if (result.getSuggestions() == null) {
            result.setSuggestions(List.of());
        }
        if (result.getMeta() == null) {
            result.setMeta(Map.of());
        }
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("aliyun.dashboard.intent.llm.base-url is not configured");
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }

    @Data
    static class ChatCompletionsRequest {
        private String model;
        private Double temperature;
        private List<ChatMessage> messages;
        @JsonProperty("response_format")
        private ResponseFormat responseFormat;
    }

    @Data
    @RequiredArgsConstructor
    static class ChatMessage {
        private final String role;
        private final String content;
    }

    @Data
    static class ResponseFormat {
        private String type;
        @JsonProperty("json_schema")
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
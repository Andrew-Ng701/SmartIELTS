package com.andrew.smartielts.speaking.did.service.impl;

import com.andrew.smartielts.speaking.did.DidProperties;
import com.andrew.smartielts.speaking.did.dto.DidCreateTalkResponseDTO;
import com.andrew.smartielts.speaking.did.service.DidSpeakingService;
import com.andrew.smartielts.speaking.domain.vo.SpeakingTalkStatusVO;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class DidSpeakingServiceImpl implements DidSpeakingService {

    private final DidProperties didProperties;
    private final RestTemplate restTemplate;

    public DidSpeakingServiceImpl(DidProperties didProperties, RestTemplate restTemplate) {
        this.didProperties = didProperties;
        this.restTemplate = restTemplate;
    }

    @Override
    public String createTalk(String scriptText) {
        String url = didProperties.getBaseUrl() + "/talks";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", buildAuthHeader());

        Map<String, Object> script = new HashMap<>();
        script.put("type", "text");
        script.put("input", scriptText);
        script.put("provider", Map.of(
                "type", "microsoft",
                "voice_id", didProperties.getVoiceId()
        ));

        Map<String, Object> config = new HashMap<>();
        config.put("stitch", true);

        Map<String, Object> body = new HashMap<>();
        body.put("presenter_id", didProperties.getPresenterId());
        body.put("script", script);
        body.put("config", config);

        ResponseEntity<DidCreateTalkResponseDTO> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                DidCreateTalkResponseDTO.class
        );

        if (response.getBody() == null || response.getBody().getId() == null) {
            throw new RuntimeException("Failed to create D-ID talk");
        }

        return response.getBody().getId();
    }

    @Override
    public SpeakingTalkStatusVO getTalkStatus(String talkId) {
        if (talkId == null || talkId.isBlank()) {
            throw new RuntimeException("talkId is required");
        }

        String url = didProperties.getBaseUrl() + "/talks/" + talkId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", buildAuthHeader());

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Failed to get D-ID talk status");
        }

        Map<?, ?> body = response.getBody();
        SpeakingTalkStatusVO vo = new SpeakingTalkStatusVO();
        vo.setTalkId(asString(body.get("id"), talkId));
        vo.setTalkStatus(asString(body.get("status"), null));
        vo.setVideoUrl(firstNonBlank(
                asString(body.get("result_url"), null),
                asString(body.get("video_url"), null)
        ));
        vo.setErrorMessage(extractErrorMessage(body.get("error")));
        return vo;
    }

    private String buildAuthHeader() {
        String rawAuth = didProperties.getApiKey() + ":";
        String encodedAuth = Base64.getEncoder()
                .encodeToString(rawAuth.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encodedAuth;
    }

    private String extractErrorMessage(Object error) {
        if (error == null) {
            return null;
        }
        if (error instanceof Map<?, ?> map) {
            String message = asString(map.get("message"), null);
            return message == null ? map.toString() : message;
        }
        return String.valueOf(error);
    }

    private String asString(Object value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String text = String.valueOf(value);
        return text.isBlank() ? fallback : text;
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second;
    }
}

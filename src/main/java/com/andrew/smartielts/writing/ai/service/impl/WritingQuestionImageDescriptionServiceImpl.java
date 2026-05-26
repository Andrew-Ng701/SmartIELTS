package com.andrew.smartielts.writing.ai.service.impl;

import com.andrew.smartielts.common.image.domain.pojo.BizImageResource;
import com.andrew.smartielts.writing.ai.service.AliyunDeepSeekClient;
import com.andrew.smartielts.writing.ai.service.WritingQuestionImageDescriptionService;
import com.andrew.smartielts.writing.domain.pojo.WritingQuestion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class WritingQuestionImageDescriptionServiceImpl implements WritingQuestionImageDescriptionService {

    private final AliyunDeepSeekClient aliyunDeepSeekClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WritingQuestionImageDescriptionServiceImpl(AliyunDeepSeekClient aliyunDeepSeekClient) {
        this.aliyunDeepSeekClient = aliyunDeepSeekClient;
    }

    @Override
    public String describeQuestionImages(WritingQuestion question, List<BizImageResource> images) {
        List<BizImageResource> sortedImages = sortImages(images);
        if (sortedImages.isEmpty()) {
            return null;
        }

        List<String> descriptions = new ArrayList<>();
        for (int i = 0; i < sortedImages.size(); i++) {
            BizImageResource image = sortedImages.get(i);
            String imageUrl = trimToNull(image.getFileUrl());
            if (imageUrl == null) {
                continue;
            }
            String raw = aliyunDeepSeekClient.chatWithImage(buildPrompt(question), imageUrl);
            String description = trimToNull(extractContent(raw));
            if (description != null) {
                descriptions.add("Image " + (i + 1) + ":\n" + description);
            }
        }
        return descriptions.isEmpty() ? null : String.join("\n\n", descriptions);
    }

    private String buildPrompt(WritingQuestion question) {
        String taskType = question == null ? "" : safeText(question.getTaskType());
        String chartType = question == null ? "" : safeText(question.getChartType());
        String title = question == null ? "" : safeText(question.getTitle());
        String prompt = question == null ? "" : safeText(question.getDescription());
        return """
                You are preparing internal context for IELTS Writing scoring.
                Describe the writing question image in English with enough detail for another examiner
                to judge whether a candidate accurately described the visual information.

                Focus on useful scoring information:
                - chart/table/map/process type and structure
                - labels, axes, units, time periods, categories, stages, locations, and legends
                - key values, rankings, comparisons, trends, changes, peaks, lows, exceptions, and relationships
                - any task-specific constraints or notable details needed to assess Task Achievement

                Do not include irrelevant visual quality comments such as colour, resolution, blur,
                image clarity, font style, or decorative appearance unless they change the data meaning.
                Do not invent values that are not visible. If something is unreadable, say exactly what is unreadable.
                Return plain English text only, not JSON or markdown.

                Existing question metadata:
                Task type: %s
                Chart type: %s
                Title: %s
                Prompt: %s
                """.formatted(taskType, chartType, title, prompt);
    }

    private String extractContent(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(raw);
            JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
            if (!contentNode.isMissingNode() && !contentNode.isNull()) {
                return contentNode.asText("");
            }
        } catch (Exception ignored) {
            // fall back to raw response
        }
        return raw;
    }

    private List<BizImageResource> sortImages(List<BizImageResource> images) {
        if (images == null || images.isEmpty()) {
            return new ArrayList<>();
        }
        return images.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(
                        BizImageResource::getSortOrder,
                        Comparator.nullsLast(Integer::compareTo)
                ).thenComparing(
                        BizImageResource::getId,
                        Comparator.nullsLast(Long::compareTo)
                ))
                .toList();
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

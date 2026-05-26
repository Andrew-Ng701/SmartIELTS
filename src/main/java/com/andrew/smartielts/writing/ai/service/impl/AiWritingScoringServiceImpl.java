package com.andrew.smartielts.writing.ai.service.impl;

import com.andrew.smartielts.writing.ai.AiWritingScore;
import com.andrew.smartielts.writing.ai.service.AiWritingScoringService;
import com.andrew.smartielts.writing.ai.service.AliyunDeepSeekClient;
import com.andrew.smartielts.writing.domain.pojo.WritingQuestion;
import com.andrew.smartielts.writing.domain.pojo.WritingRecord;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AiWritingScoringServiceImpl implements AiWritingScoringService {

    @Autowired
    private AliyunDeepSeekClient aliyunDeepSeekClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public AiWritingScore score(WritingQuestion question, WritingRecord record, String finalText) {
        String prompt = buildPrompt(question, record, finalText);
        String raw = aliyunDeepSeekClient.chat(prompt);
        String content = extractContent(raw);

        AiWritingScore result = new AiWritingScore();
        result.setRawResponse(raw);
        result.setAiScore(parseScore(content));
        result.setAiFeedback(parseFeedback(content));
        if (result.getAiScore() == null || result.getAiFeedback() == null || result.getAiFeedback().isBlank()) {
            throw new RuntimeException("AI scoring response parsing failed");
        }
        return result;
    }

    private String extractContent(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        try {
            JsonNode root = objectMapper.readTree(raw);
            JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
            if (!contentNode.isMissingNode() && !contentNode.isNull()) {
                return contentNode.asText("");
            }
        } catch (Exception ignored) {
            // fall back to regex parsing on raw
        }
        return raw;
    }

    private String buildPrompt(WritingQuestion question, WritingRecord record, String finalText) {
        String questionText = question == null ? "" : question.getDescription();
        String imageDetailDescription = question == null ? "" : nullToEmpty(question.getImageDetailDescription());
        String targetScore = record.getTargetScore() == null ? "" : record.getTargetScore().toPlainString();

        return """
                You are a strict but helpful IELTS Writing examiner.
                Score the submission using IELTS Writing band descriptors.
                Evaluate Task Achievement/Task Response, Coherence and Cohesion, Lexical Resource,
                and Grammatical Range and Accuracy. Penalize memorized, irrelevant, too short,
                incomplete, or off-topic answers.

                Requirements:
                - Return valid JSON only. Do not include markdown fences.
                - aiScore must be a number from 0.0 to 9.0, rounded to one decimal place.
                - aiFeedback must be detailed enough for frontend display.
                - In aiFeedback, wrap important phrases with double asterisks for bold emphasis, for example **weak thesis**.
                - In aiFeedback, separate every paragraph with one blank line.
                - In aiFeedback, include:
                  1. Overall judgement and reason for the score.
                  2. Criterion-by-criterion comments for all four IELTS criteria.
                  3. Two to four concrete improvement actions.
                  4. If useful, mention missing word count, weak structure, unsupported ideas,
                     vocabulary repetition, grammar patterns, or relevance issues.
                - Keep aiFeedback in English only.
                - Do not invent facts not supported by the prompt or answer.

                Writing prompt:
                %s

                Detailed visual/task context from question image:
                %s

                Candidate target score:
                %s

                Input type:
                %s

                Candidate answer:
                %s

                Return JSON in this exact shape:
                {
                  "aiScore": 0.0,
                  "aiFeedback": ""
                }
                """.formatted(
                questionText,
                imageDetailDescription,
                targetScore,
                record.getInputType(),
                finalText == null ? "" : finalText
        );
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private BigDecimal parseScore(String raw) {
        if (raw == null) {
            return null;
        }
        // Prefer strict JSON parsing if possible
        try {
            JsonNode node = objectMapper.readTree(raw);
            JsonNode aiScore = node.get("aiScore");
            if (aiScore != null && aiScore.isNumber()) {
                return aiScore.decimalValue();
            }
            if (aiScore != null && aiScore.isTextual()) {
                String s = aiScore.asText().trim();
                if (!s.isEmpty()) {
                    return new BigDecimal(s);
                }
            }
        } catch (Exception ignored) {
            // fall back to regex
        }
        Pattern pattern = Pattern.compile("\"aiScore\"\\s*:\\s*(\\d+(\\.\\d+)?)");
        Matcher matcher = pattern.matcher(raw);
        if (matcher.find()) {
            return new BigDecimal(matcher.group(1));
        }
        return null;
    }

    private String parseFeedback(String raw) {
        if (raw == null) {
            return null;
        }
        // Prefer strict JSON parsing if possible
        try {
            JsonNode node = objectMapper.readTree(raw);
            JsonNode aiFeedback = node.get("aiFeedback");
            if (aiFeedback != null && aiFeedback.isTextual()) {
                return normalizeFeedbackFormat(aiFeedback.asText());
            }
        } catch (Exception ignored) {
            // fall back to regex
        }
        Pattern pattern = Pattern.compile("\"aiFeedback\"\\s*:\\s*\"(.*?)\"", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(raw);
        if (matcher.find()) {
            String feedback = matcher.group(1)
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"")
                    .trim();
            return normalizeFeedbackFormat(feedback);
        }
        return null;
    }

    private String normalizeFeedbackFormat(String feedback) {
        if (feedback == null) {
            return null;
        }
        String normalized = feedback
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .trim();
        if (normalized.isEmpty()) {
            return null;
        }

        StringBuilder result = new StringBuilder();
        String[] paragraphs = normalized.split("\\n+");
        for (String paragraph : paragraphs) {
            String trimmed = paragraph.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (!result.isEmpty()) {
                result.append("\n\n");
            }
            result.append(formatFeedbackParagraph(trimmed));
        }
        return result.isEmpty() ? null : result.toString();
    }

    private String formatFeedbackParagraph(String paragraph) {
        if (paragraph.startsWith("**")) {
            return paragraph;
        }

        String formatted = paragraph
                .replaceFirst("^(Overall Judgement|Overall Judgment):", "**$1**:")
                .replaceFirst("^(Task Achievement|Task Response):", "**$1**:")
                .replaceFirst("^(Coherence and Cohesion):", "**$1**:")
                .replaceFirst("^(Lexical Resource):", "**$1**:")
                .replaceFirst("^(Grammatical Range and Accuracy):", "**$1**:")
                .replaceFirst("^(Improvement Actions):", "**$1**:");
        if (!formatted.equals(paragraph)) {
            return formatted;
        }

        return paragraph.replaceFirst("^(\\d+\\.\\s+)([^:*\\n]{1,80}):", "$1**$2**:");
    }
}

package com.andrew.smartielts.writing.ai.service.impl;

import com.andrew.smartielts.writing.ai.AiWritingScore;
import com.andrew.smartielts.writing.ai.service.AliyunDeepSeekClient;
import com.andrew.smartielts.writing.domain.pojo.WritingQuestion;
import com.andrew.smartielts.writing.domain.pojo.WritingRecord;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiWritingScoringServiceImplTest {

    @Test
    void score_shouldAskForEnglishFeedback() {
        AiWritingScoringServiceImpl service = new AiWritingScoringServiceImpl();
        AtomicReference<String> promptRef = new AtomicReference<>();
        AliyunDeepSeekClient client = new AliyunDeepSeekClient() {
            @Override
            public String chat(String prompt) {
                promptRef.set(prompt);
                return "{\"choices\":[{\"message\":{\"content\":\"{\\\"aiScore\\\":6.5,\\\"aiFeedback\\\":\\\"**Overall**: Clear English feedback.\\\\n**Action**: Improve paragraphing.\\\"}\"}}]}";
            }

            @Override
            public String chatWithImage(String prompt, String imageUrl) {
                throw new UnsupportedOperationException();
            }
        };
        ReflectionTestUtils.setField(service, "aliyunDeepSeekClient", client);

        WritingQuestion question = new WritingQuestion();
        question.setDescription("Describe the chart.");
        question.setImageDetailDescription("The line chart shows sales increasing from 10 to 30 units.");
        WritingRecord record = new WritingRecord();
        record.setInputType("TEXT");

        AiWritingScore result = service.score(question, record, "This is my essay.");

        assertEquals("6.5", result.getAiScore().toPlainString());
        assertEquals("**Overall**: Clear English feedback.\n\n**Action**: Improve paragraphing.", result.getAiFeedback());
        assertTrue(promptRef.get().contains("Keep aiFeedback in English only."));
        assertTrue(promptRef.get().contains("wrap important phrases with double asterisks"));
        assertTrue(promptRef.get().contains("separate every paragraph with one blank line"));
        assertTrue(promptRef.get().contains("The line chart shows sales increasing from 10 to 30 units."));
    }

    @Test
    void score_whenFeedbackHeadingsArePlain_shouldApplyBoldMarkdown() {
        AiWritingScoringServiceImpl service = new AiWritingScoringServiceImpl();
        AliyunDeepSeekClient client = new AliyunDeepSeekClient() {
            @Override
            public String chat(String prompt) {
                return "{\"choices\":[{\"message\":{\"content\":\"{\\\"aiScore\\\":5.5,\\\"aiFeedback\\\":\\\"Overall Judgement: Mostly clear.\\\\nTask Achievement: Key data is covered.\\\\n1. Master Irregular Verbs: Use grew, not growed.\\\"}\"}}]}";
            }

            @Override
            public String chatWithImage(String prompt, String imageUrl) {
                throw new UnsupportedOperationException();
            }
        };
        ReflectionTestUtils.setField(service, "aliyunDeepSeekClient", client);

        WritingRecord record = new WritingRecord();
        record.setInputType("TEXT");

        AiWritingScore result = service.score(new WritingQuestion(), record, "This is my essay.");

        assertEquals("**Overall Judgement**: Mostly clear.\n\n**Task Achievement**: Key data is covered.\n\n1. **Master Irregular Verbs**: Use grew, not growed.", result.getAiFeedback());
    }
}

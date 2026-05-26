package com.andrew.smartielts.writing.ai.service.impl;

import com.andrew.smartielts.common.image.domain.pojo.BizImageResource;
import com.andrew.smartielts.writing.ai.service.AliyunDeepSeekClient;
import com.andrew.smartielts.writing.domain.pojo.WritingQuestion;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WritingQuestionImageDescriptionServiceImplTest {

    @Test
    void describeQuestionImages_shouldUseImageUrlAndRequestUsefulScoringDescription() {
        AtomicReference<String> promptRef = new AtomicReference<>();
        AtomicReference<String> imageUrlRef = new AtomicReference<>();
        AliyunDeepSeekClient client = new AliyunDeepSeekClient() {
            @Override
            public String chat(String prompt) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String chatWithImage(String prompt, String imageUrl) {
                promptRef.set(prompt);
                imageUrlRef.set(imageUrl);
                return "{\"choices\":[{\"message\":{\"content\":\"The chart compares sales by year.\"}}]}";
            }
        };
        WritingQuestionImageDescriptionServiceImpl service = new WritingQuestionImageDescriptionServiceImpl(client);

        WritingQuestion question = new WritingQuestion();
        question.setTaskType("TASK1");
        question.setChartType("Line graph");
        question.setTitle("Sales chart");
        question.setDescription("Describe the chart.");
        BizImageResource image = new BizImageResource();
        image.setFileUrl("https://oss.test/chart.png");
        image.setSortOrder(1);

        String result = service.describeQuestionImages(question, List.of(image));

        assertEquals("Image 1:\nThe chart compares sales by year.", result);
        assertEquals("https://oss.test/chart.png", imageUrlRef.get());
        assertTrue(promptRef.get().contains("Do not include irrelevant visual quality comments"));
        assertTrue(promptRef.get().contains("Task type: TASK1"));
    }
}

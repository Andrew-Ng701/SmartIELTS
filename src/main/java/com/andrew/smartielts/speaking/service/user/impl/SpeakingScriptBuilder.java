package com.andrew.smartielts.speaking.service.user.impl;

import com.andrew.smartielts.speaking.domain.model.ExamStep;
import com.andrew.smartielts.speaking.domain.pojo.SpeakingQuestion;
import org.springframework.stereotype.Component;

@Component
public class SpeakingScriptBuilder {

    public String buildSpokenScript(ExamStep current, ExamStep previous, SpeakingQuestion question) {
        if (current == null || question == null) {
            throw new RuntimeException("Current step or question is null");
        }

        if ("OPENING".equals(current.getStepType())) {
            return question.getQuestionText();
        }

        if ("PART1".equals(current.getStepType())) {
            return question.getQuestionText();
        }

        if ("PART2".equals(current.getStepType())) {
            StringBuilder sb = new StringBuilder();
            sb.append("Now Part 2. You have one minute to prepare, then speak for one to two minutes.\n\n");
            sb.append("Topic:\n\n");
            sb.append(question.getQuestionText()).append("\n\n");
            if (question.getCueCard() != null && !question.getCueCard().isBlank()) {
                sb.append(question.getCueCard()).append("\n\n");
            }
            sb.append("Your preparation time starts now.");
            return sb.toString();
        }

        if ("PART3".equals(current.getStepType())) {
            StringBuilder sb = new StringBuilder();
            if (previous != null && "PART2".equals(previous.getStepType())) {
                sb.append("Now Part 3. Let's discuss some related questions.\n\n");
            }
            sb.append(question.getQuestionText());
            return sb.toString();
        }

        return question.getQuestionText();
    }

    public String buildDisplayScript(ExamStep current, SpeakingQuestion question) {
        if (current == null || question == null) {
            return null;
        }

        if (!"PART2".equals(current.getStepType())) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Part 2\n\n");
        sb.append("You should spend about 1 minute preparing and up to 2 minutes speaking.\n\n");
        sb.append(question.getQuestionText()).append("\n\n");
        if (question.getCueCard() != null && !question.getCueCard().isBlank()) {
            sb.append(question.getCueCard());
        }
        return sb.toString();
    }
}

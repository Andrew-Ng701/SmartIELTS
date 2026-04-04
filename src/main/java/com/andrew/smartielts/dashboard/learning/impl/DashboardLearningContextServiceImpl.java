package com.andrew.smartielts.dashboard.learning.impl;

import com.andrew.smartielts.dashboard.controller.dto.DashboardAskObjectRef;
import com.andrew.smartielts.dashboard.learning.LearningObjectQueryService;
import com.andrew.smartielts.dashboard.learning.DashboardLearningContextService;
import com.andrew.smartielts.dashboard.learning.dto.LearningObjectDTO;
import com.andrew.smartielts.dashboard.learning.dto.UserAttemptDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardLearningContextServiceImpl implements DashboardLearningContextService {

    private final LearningObjectQueryService learningObjectQueryService;

    @Override
    public Map<String, Object> buildLearningContext(
            String role,
            Long operatorUserId,
            Long targetUserId,
            String askScene,
            DashboardAskObjectRef objectRef) {

        Map<String, Object> result = new LinkedHashMap<>();
        if (objectRef == null || objectRef.getModule() == null) {
            return result;
        }

        String module = objectRef.getModule();

        if (objectRef.getQuestionId() != null) {
            LearningObjectDTO question = learningObjectQueryService.getQuestion(module, objectRef.getQuestionId());
            result.put("question", question);
        }

        if (objectRef.getPassageId() != null) {
            LearningObjectDTO passage = learningObjectQueryService.getPassage(module, objectRef.getPassageId());
            result.put("passage", passage);
        }

        if (objectRef.getTestId() != null) {
            LearningObjectDTO test = learningObjectQueryService.getTest(module, objectRef.getTestId());
            result.put("test", test);
        }

        if (objectRef.getRecordId() != null && objectRef.getQuestionId() != null) {
            UserAttemptDTO attempt = learningObjectQueryService.getUserAttempt(
                    module,
                    targetUserId != null ? targetUserId : operatorUserId,
                    objectRef.getRecordId(),
                    objectRef.getQuestionId()
            );
            result.put("userAttempt", attempt);
        }

        return result;
    }
}
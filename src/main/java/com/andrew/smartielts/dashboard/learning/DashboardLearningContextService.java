package com.andrew.smartielts.dashboard.learning;

import com.andrew.smartielts.dashboard.controller.dto.DashboardAskObjectRef;

import java.util.Map;

public interface DashboardLearningContextService {

    Map<String, Object> buildLearningContext(
            String role,
            Long operatorUserId,
            Long targetUserId,
            String askScene,
            DashboardAskObjectRef objectRef
    );
}
package com.andrew.smartielts.dashboard.controller;

import com.andrew.smartielts.common.resultDTO.Result;
import com.andrew.smartielts.dashboard.agent.DashboardIntentExecutionFacade;
import com.andrew.smartielts.dashboard.controller.dto.DashboardAskRequest;
import com.andrew.smartielts.dashboard.controller.dto.DashboardAssistantResponse;
import com.andrew.smartielts.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/smartielts/dashboard/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardIntentExecutionFacade executionFacade;

    @PostMapping("/ask")
    public Result<DashboardAssistantResponse> ask(@RequestBody DashboardAskRequest request) {
        Long operatorUserId = currentUserId();
        DashboardAssistantResponse response = executionFacade.ask(
                "ADMIN",
                operatorUserId,
                request.getTargetUserId(),
                request
        );
        return Result.success(response);
    }

    private Long currentUserId() {
        return SecurityUtils.getCurrentUserId();
    }
}
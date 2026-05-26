package com.andrew.smartielts.reading.controller.user;

import com.andrew.smartielts.common.resultDTO.Result;
import com.andrew.smartielts.reading.domain.dto.ReadingSessionActionDTO;
import com.andrew.smartielts.reading.domain.dto.ReadingSubmitDTO;
import com.andrew.smartielts.reading.service.user.UserReadingService;
import com.andrew.smartielts.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Reading API")
@RestController
@RequestMapping("/user/reading")
@SecurityRequirement(name = "bearerAuth")
public class UserReadingController {

    private final UserReadingService userReadingService;

    public UserReadingController(UserReadingService userReadingService) {
        this.userReadingService = userReadingService;
    }

    @Operation(summary = "List tests")
    @GetMapping("/tests")
    public Result<?> listTests() {
        return Result.success(userReadingService.listTests());
    }

    @Operation(summary = "Start reading test")
    @PostMapping("/tests/{testId}/start")
    public Result<?> start(@PathVariable Long testId) {
        return Result.success(userReadingService.start(testId));
    }

    @Operation(summary = "Get reading session")
    @GetMapping("/sessions/{sessionId}")
    public Result<?> getSession(@PathVariable String sessionId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userReadingService.getSession(sessionId, userId));
    }

    @Operation(summary = "Pause reading session")
    @PostMapping("/sessions/{sessionId}/pause")
    public Result<?> pause(@PathVariable String sessionId,
                           @RequestBody(required = false) ReadingSessionActionDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userReadingService.pause(sessionId, userId, dto));
    }

    @Operation(summary = "Resume reading session")
    @PostMapping("/sessions/{sessionId}/resume")
    public Result<?> resume(@PathVariable String sessionId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userReadingService.resume(sessionId, userId));
    }

    @Operation(summary = "Submit reading test")
    @PostMapping("/tests/{testId}/submit")
    public Result<?> submit(@PathVariable Long testId, @Valid @RequestBody ReadingSubmitDTO dto) {
        return Result.success(userReadingService.submit(testId, dto));
    }

}

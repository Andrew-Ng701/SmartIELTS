package com.andrew.smartielts.listening.controller.user;

import com.andrew.smartielts.common.resultDTO.Result;
import com.andrew.smartielts.listening.domain.dto.ListeningSessionActionDTO;
import com.andrew.smartielts.listening.domain.dto.ListeningSubmitDTO;
import com.andrew.smartielts.listening.domain.query.user.UserListeningDeletedRecordPageQuery;
import com.andrew.smartielts.listening.domain.query.user.UserListeningRecordPageQuery;
import com.andrew.smartielts.listening.service.user.UserListeningService;
import com.andrew.smartielts.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Listening API")
@RestController
@RequestMapping("/user/listening")
@SecurityRequirement(name = "bearerAuth")
public class UserListeningController {

    private final UserListeningService userListeningService;

    public UserListeningController(UserListeningService userListeningService) {
        this.userListeningService = userListeningService;
    }

    @Operation(summary = "List listening tests")
    @GetMapping("/tests")
    public Result<?> listTests() {
        return Result.success(userListeningService.listTests());
    }

    @Operation(summary = "Get listening test detail")
    @GetMapping("/tests/{testId}")
    public Result<?> getTestDetail(@PathVariable Long testId) {
        return Result.success(userListeningService.getTestDetail(testId));
    }

    @Operation(summary = "Start listening test")
    @PostMapping("/tests/{testId}/start")
    public Result<?> start(@PathVariable Long testId) {
        return Result.success(userListeningService.start(testId));
    }

    @Operation(summary = "Get listening session")
    @GetMapping("/sessions/{sessionId}")
    public Result<?> getSession(@PathVariable String sessionId) {
        Long user_id = SecurityUtils.getCurrentUserId();
        return Result.success(userListeningService.getSession(sessionId, user_id));
    }

    @Operation(summary = "Pause listening session")
    @PostMapping("/sessions/{sessionId}/pause")
    public Result<?> pause(@PathVariable String sessionId,
                           @RequestBody(required = false) ListeningSessionActionDTO dto) {
        Long user_id = SecurityUtils.getCurrentUserId();
        return Result.success(userListeningService.pause(sessionId, user_id, dto));
    }

    @Operation(summary = "Resume listening session")
    @PostMapping("/sessions/{sessionId}/resume")
    public Result<?> resume(@PathVariable String sessionId) {
        Long user_id = SecurityUtils.getCurrentUserId();
        return Result.success(userListeningService.resume(sessionId, user_id));
    }

    @Operation(summary = "Submit listening test")
    @PostMapping("/tests/{testId}/submit")
    public Result<?> submit(@PathVariable Long testId, @RequestBody ListeningSubmitDTO dto) {
        return Result.success(userListeningService.submit(testId, dto));
    }

    @Operation(summary = "User listening active records overview")
    @PostMapping("/records/overview")
    public Result<?> pageActiveRecords(@Valid @RequestBody UserListeningRecordPageQuery query) {
        Long user_id = SecurityUtils.getCurrentUserId();
        return Result.success(userListeningService.pageActiveRecords(user_id, query));
    }

    @Operation(summary = "User listening deleted records overview")
    @PostMapping("/records/deleted/overview")
    public Result<?> pageDeletedRecords(@Valid @RequestBody UserListeningDeletedRecordPageQuery query) {
        Long user_id = SecurityUtils.getCurrentUserId();
        return Result.success(userListeningService.pageDeletedRecords(user_id, query));
    }

    @Operation(summary = "Get listening record detail")
    @GetMapping("/records/{recordId}")
    public Result<?> getRecord(@PathVariable Long recordId) {
        Long user_id = SecurityUtils.getCurrentUserId();
        return Result.success(userListeningService.getRecord(recordId, user_id));
    }

    @Operation(summary = "Delete my listening record")
    @DeleteMapping("/records/{recordId}")
    public Result<?> deleteRecord(@PathVariable Long recordId) {
        Long user_id = SecurityUtils.getCurrentUserId();
        userListeningService.deleteRecord(recordId, user_id);
        return Result.success();
    }

    @Operation(summary = "Restore my listening record")
    @PutMapping("/records/{recordId}/restore")
    public Result<?> restoreRecord(@PathVariable Long recordId) {
        Long user_id = SecurityUtils.getCurrentUserId();
        userListeningService.restoreRecord(recordId, user_id);
        return Result.success();
    }
}
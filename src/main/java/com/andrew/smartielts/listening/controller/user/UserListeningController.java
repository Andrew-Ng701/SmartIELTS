package com.andrew.smartielts.listening.controller.user;

import com.andrew.smartielts.common.resultDTO.Result;
import com.andrew.smartielts.listening.domain.dto.ListeningSubmitDTO;
import com.andrew.smartielts.listening.domain.query.user.UserListeningDeletedRecordPageQuery;
import com.andrew.smartielts.listening.domain.query.user.UserListeningRecordPageQuery;
import com.andrew.smartielts.listening.service.user.UserListeningService;
import com.andrew.smartielts.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Listening API")
@RestController
@RequestMapping("/user/listening")
@PreAuthorize("hasRole('USER')")
@SecurityRequirement(name = "bearerAuth")
public class UserListeningController {

    @Autowired
    private UserListeningService userListeningService;

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

    @Operation(summary = "Submit listening test")
    @PostMapping("/tests/{testId}/submit")
    public Result<?> submit(@PathVariable Long testId, @RequestBody ListeningSubmitDTO dto) {
        return Result.success(userListeningService.submit(testId, dto));
    }

    @Operation(summary = "User listening active records overview")
    @PostMapping("/records/overview")
    public Result<?> pageActiveRecords(@Valid @RequestBody UserListeningRecordPageQuery query) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userListeningService.pageActiveRecords(userId, query));
    }

    @Operation(summary = "User listening deleted records overview")
    @PostMapping("/records/deleted/overview")
    public Result<?> pageDeletedRecords(@Valid @RequestBody UserListeningDeletedRecordPageQuery query) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userListeningService.pageDeletedRecords(userId, query));
    }

    @Operation(summary = "Get listening record detail")
    @GetMapping("/records/{recordId}")
    public Result<?> getRecord(@PathVariable Long recordId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userListeningService.getRecord(recordId, userId));
    }

    @Operation(summary = "Delete my listening record")
    @DeleteMapping("/records/{recordId}")
    public Result<?> deleteRecord(@PathVariable Long recordId) {
        Long userId = SecurityUtils.getCurrentUserId();
        userListeningService.deleteRecord(recordId, userId);
        return Result.success();
    }

    @Operation(summary = "Restore my listening record")
    @PutMapping("/records/{recordId}/restore")
    public Result<?> restoreRecord(@PathVariable Long recordId) {
        Long userId = SecurityUtils.getCurrentUserId();
        userListeningService.restoreRecord(recordId, userId);
        return Result.success();
    }
}

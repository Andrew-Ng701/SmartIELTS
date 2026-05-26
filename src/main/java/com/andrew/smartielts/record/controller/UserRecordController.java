package com.andrew.smartielts.record.controller;

import com.andrew.smartielts.common.resultDTO.Result;
import com.andrew.smartielts.record.domain.query.UserRecordListQuery;
import com.andrew.smartielts.record.domain.query.UserRecordPageQuery;
import com.andrew.smartielts.record.service.UserRecordService;
import com.andrew.smartielts.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User Record API")
@RestController
@RequestMapping("/user/records")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('USER')")
public class UserRecordController {

    private final UserRecordService userRecordService;

    public UserRecordController(UserRecordService userRecordService) {
        this.userRecordService = userRecordService;
    }

    @Operation(summary = "List current user records across all modules")
    @GetMapping
    public Result<?> listRecords(UserRecordListQuery query) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userRecordService.listRecords(userId, query));
    }

    @Operation(summary = "User records overview")
    @PostMapping("/overview")
    public Result<?> pageRecords(@RequestBody(required = false) UserRecordPageQuery query) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userRecordService.pageRecords(userId, query));
    }

    @Operation(summary = "Get user record detail")
    @GetMapping("/{moduleType}/{recordId}")
    public Result<?> getRecord(@PathVariable String moduleType, @PathVariable Long recordId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userRecordService.getRecord(userId, moduleType, recordId));
    }

    @Operation(summary = "Get listening record section script")
    @GetMapping("/listening/{recordId}/sections/{sectionNumber}/script")
    public Result<?> getListeningSectionScript(@PathVariable Long recordId,
                                               @PathVariable Integer sectionNumber) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userRecordService.getListeningSectionScript(userId, recordId, sectionNumber));
    }

    @Operation(summary = "Delete user record")
    @DeleteMapping("/{moduleType}/{recordId}")
    public Result<?> deleteRecord(@PathVariable String moduleType, @PathVariable Long recordId) {
        Long userId = SecurityUtils.getCurrentUserId();
        userRecordService.deleteRecord(userId, moduleType, recordId);
        return Result.success();
    }

    @Operation(summary = "Restore user record")
    @PutMapping("/{moduleType}/{recordId}/restore")
    public Result<?> restoreRecord(@PathVariable String moduleType, @PathVariable Long recordId) {
        Long userId = SecurityUtils.getCurrentUserId();
        userRecordService.restoreRecord(userId, moduleType, recordId);
        return Result.success();
    }

    @Operation(summary = "Get speaking session record summary")
    @GetMapping("/speaking/sessions/{sessionId}")
    public Result<?> getSpeakingSessionSummary(@PathVariable String sessionId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userRecordService.getSpeakingSessionSummary(userId, sessionId));
    }
}

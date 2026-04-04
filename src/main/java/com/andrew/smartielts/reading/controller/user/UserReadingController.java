package com.andrew.smartielts.reading.controller.user;

import com.andrew.smartielts.common.resultDTO.Result;
import com.andrew.smartielts.reading.domain.dto.ReadingSubmitDTO;
import com.andrew.smartielts.reading.domain.query.user.UserReadingDeletedRecordPageQuery;
import com.andrew.smartielts.reading.domain.query.user.UserReadingRecordPageQuery;
import com.andrew.smartielts.reading.service.user.UserReadingService;
import com.andrew.smartielts.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Reading API")
@RestController
@RequestMapping("/user/reading")
@PreAuthorize("hasRole('USER')")
@SecurityRequirement(name = "bearerAuth")
public class UserReadingController {

    @Autowired
    private UserReadingService userReadingService;

    @Operation(summary = "List tests")
    @GetMapping("/tests")
    public Result<?> listTests() {
        return Result.success(userReadingService.listTests());
    }

    @Operation(summary = "Get test detail")
    @GetMapping("/tests/{testId}")
    public Result<?> getTestDetail(@PathVariable Long testId) {
        return Result.success(userReadingService.getTestDetail(testId));
    }

    @Operation(summary = "Submit reading test")
    @PostMapping("/tests/{testId}/submit")
    public Result<?> submit(@PathVariable Long testId, @RequestBody ReadingSubmitDTO dto) {
        return Result.success(userReadingService.submit(testId, dto));
    }

    @Operation(summary = "User reading active records overview")
    @PostMapping("/records/overview")
    public Result<?> pageActiveRecords(@Valid @RequestBody UserReadingRecordPageQuery query) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userReadingService.pageActiveRecords(userId, query));
    }

    @Operation(summary = "User reading deleted records overview")
    @PostMapping("/records/deleted/overview")
    public Result<?> pageDeletedRecords(@Valid @RequestBody UserReadingDeletedRecordPageQuery query) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userReadingService.pageDeletedRecords(userId, query));
    }

    @Operation(summary = "Reading record detail")
    @GetMapping("/records/{recordId}")
    public Result<?> getRecord(@PathVariable Long recordId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userReadingService.getRecord(recordId, userId));
    }

    @Operation(summary = "Delete my reading record")
    @DeleteMapping("/records/{recordId}")
    public Result<?> deleteRecord(@PathVariable Long recordId) {
        Long userId = SecurityUtils.getCurrentUserId();
        userReadingService.deleteRecord(recordId, userId);
        return Result.success();
    }

    @Operation(summary = "Restore my reading record")
    @PutMapping("/records/{recordId}/restore")
    public Result<?> restoreRecord(@PathVariable Long recordId) {
        Long userId = SecurityUtils.getCurrentUserId();
        userReadingService.restoreRecord(recordId, userId);
        return Result.success();
    }
}

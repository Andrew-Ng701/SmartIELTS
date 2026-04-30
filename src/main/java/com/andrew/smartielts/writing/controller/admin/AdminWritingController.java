package com.andrew.smartielts.writing.controller.admin;

import com.andrew.smartielts.common.resultDTO.Result;
import com.andrew.smartielts.writing.domain.dto.WritingQuestionDTO;
import com.andrew.smartielts.writing.domain.query.admin.AdminWritingDeletedRecordPageQuery;
import com.andrew.smartielts.writing.domain.query.admin.AdminWritingRecordPageQuery;
import com.andrew.smartielts.writing.service.admin.AdminWritingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Writing API")
@RestController
@RequestMapping("/admin/writing")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class AdminWritingController {

    private final AdminWritingService adminWritingService;

    public AdminWritingController(AdminWritingService adminWritingService) {
        this.adminWritingService = adminWritingService;
    }

    @Operation(summary = "Create writing question")
    @PostMapping("/questions")
    public Result<?> createQuestion(@RequestBody WritingQuestionDTO dto) {
        return Result.success(adminWritingService.createQuestion(dto));
    }

    @Operation(summary = "List writing questions")
    @GetMapping("/questions")
    public Result<?> listQuestions() {
        return Result.success(adminWritingService.listQuestions());
    }

    @Operation(summary = "Get writing question detail")
    @GetMapping("/questions/{id}")
    public Result<?> getQuestion(@PathVariable Long id) {
        return Result.success(adminWritingService.getQuestion(id));
    }

    @Operation(summary = "Update writing question")
    @PutMapping("/questions/{id}")
    public Result<?> updateQuestion(@PathVariable Long id, @RequestBody WritingQuestionDTO dto) {
        return Result.success(adminWritingService.updateQuestion(id, dto));
    }

    @Operation(summary = "Delete writing question")
    @DeleteMapping("/questions/{id}")
    public Result<?> deleteQuestion(@PathVariable Long id) {
        adminWritingService.deleteQuestion(id);
        return Result.success();
    }

    @Operation(summary = "Restore writing question")
    @PutMapping("/questions/{id}/restore")
    public Result<?> restoreQuestion(@PathVariable Long id) {
        adminWritingService.restoreQuestion(id);
        return Result.success();
    }

    @Operation(summary = "Admin writing active records overview")
    @PostMapping("/records/overview")
    public Result<?> pageActiveRecords(@Valid @RequestBody AdminWritingRecordPageQuery query) {
        return Result.success(adminWritingService.pageActiveRecords(query));
    }

    @Operation(summary = "Admin writing deleted records overview")
    @PostMapping("/records/deleted/overview")
    public Result<?> pageDeletedRecords(@Valid @RequestBody AdminWritingDeletedRecordPageQuery query) {
        return Result.success(adminWritingService.pageDeletedRecords(query));
    }

    @Operation(summary = "Get writing record detail")
    @GetMapping("/records/{recordId}")
    public Result<?> getRecord(@PathVariable Long recordId) {
        return Result.success(adminWritingService.getRecord(recordId));
    }

    @Operation(summary = "Delete writing record")
    @DeleteMapping("/records/{recordId}")
    public Result<?> deleteRecord(@PathVariable Long recordId) {
        adminWritingService.deleteRecord(recordId);
        return Result.success();
    }

    @Operation(summary = "Restore writing record")
    @PutMapping("/records/{recordId}/restore")
    public Result<?> restoreRecord(@PathVariable Long recordId) {
        adminWritingService.restoreRecord(recordId);
        return Result.success();
    }
}
package com.andrew.smartielts.reading.controller.admin;

import com.andrew.smartielts.common.resultDTO.Result;
import com.andrew.smartielts.reading.domain.dto.ReadingPassageDTO;
import com.andrew.smartielts.reading.domain.dto.ReadingQuestionDTO;
import com.andrew.smartielts.reading.domain.dto.ReadingTestDTO;
import com.andrew.smartielts.reading.domain.query.admin.AdminReadingDeletedRecordPageQuery;
import com.andrew.smartielts.reading.domain.query.admin.AdminReadingRecordPageQuery;
import com.andrew.smartielts.reading.service.admin.AdminReadingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Reading API")
@RestController
@RequestMapping("/admin/reading")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class AdminReadingController {

    private final AdminReadingService adminReadingService;

    public AdminReadingController(AdminReadingService adminReadingService) {
        this.adminReadingService = adminReadingService;
    }

    @Operation(summary = "Create reading test")
    @PostMapping("/tests")
    public Result<?> createTest(@RequestBody ReadingTestDTO dto) {
        return Result.success(adminReadingService.createTest(dto));
    }

    @Operation(summary = "List reading tests")
    @GetMapping("/tests")
    public Result<?> listTests() {
        return Result.success(adminReadingService.listTests());
    }

    @Operation(summary = "Get reading test detail")
    @GetMapping("/tests/{testId}")
    public Result<?> getTestDetail(@PathVariable Long testId) {
        return Result.success(adminReadingService.getTestDetail(testId));
    }

    @Operation(summary = "Update reading test")
    @PutMapping("/tests/{id}")
    public Result<?> updateTest(@PathVariable Long id, @RequestBody ReadingTestDTO dto) {
        return Result.success(adminReadingService.updateTest(id, dto));
    }

    @Operation(summary = "Delete reading test")
    @DeleteMapping("/tests/{id}")
    public Result<?> deleteTest(@PathVariable Long id) {
        adminReadingService.deleteTest(id);
        return Result.success();
    }

    @Operation(summary = "Restore reading test")
    @PutMapping("/tests/{id}/restore")
    public Result<?> restoreTest(@PathVariable Long id) {
        adminReadingService.restoreTest(id);
        return Result.success();
    }

    @Operation(summary = "Create passage")
    @PostMapping("/tests/{testId}/passages")
    public Result<?> createPassage(@PathVariable Long testId, @RequestBody ReadingPassageDTO dto) {
        adminReadingService.createPassage(testId, dto);
        return Result.success();
    }

    @Operation(summary = "Update passage")
    @PutMapping("/passages/{passageId}")
    public Result<?> updatePassage(@PathVariable Long passageId, @RequestBody ReadingPassageDTO dto) {
        adminReadingService.updatePassage(passageId, dto);
        return Result.success();
    }

    @Operation(summary = "Delete passage")
    @DeleteMapping("/passages/{passageId}")
    public Result<?> deletePassage(@PathVariable Long passageId) {
        adminReadingService.deletePassage(passageId);
        return Result.success();
    }

    @Operation(summary = "Restore passage")
    @PutMapping("/passages/{passageId}/restore")
    public Result<?> restorePassage(@PathVariable Long passageId) {
        adminReadingService.restorePassage(passageId);
        return Result.success();
    }

    @Operation(summary = "Create question")
    @PostMapping("/passages/{passageId}/questions")
    public Result<?> createQuestion(@PathVariable Long passageId, @RequestBody ReadingQuestionDTO dto) {
        adminReadingService.createQuestion(passageId, dto);
        return Result.success();
    }

    @Operation(summary = "Update question")
    @PutMapping("/questions/{questionId}")
    public Result<?> updateQuestion(@PathVariable Long questionId, @RequestBody ReadingQuestionDTO dto) {
        adminReadingService.updateQuestion(questionId, dto);
        return Result.success();
    }

    @Operation(summary = "Delete question")
    @DeleteMapping("/questions/{questionId}")
    public Result<?> deleteQuestion(@PathVariable Long questionId) {
        adminReadingService.deleteQuestion(questionId);
        return Result.success();
    }

    @Operation(summary = "Restore question")
    @PutMapping("/questions/{questionId}/restore")
    public Result<?> restoreQuestion(@PathVariable Long questionId) {
        adminReadingService.restoreQuestion(questionId);
        return Result.success();
    }

    @Operation(summary = "Admin reading active records overview")
    @PostMapping("/records/overview")
    public Result<?> pageActiveRecords(@Valid @RequestBody AdminReadingRecordPageQuery query) {
        return Result.success(adminReadingService.pageActiveRecords(query));
    }

    @Operation(summary = "Admin reading deleted records overview")
    @PostMapping("/records/deleted/overview")
    public Result<?> pageDeletedRecords(@Valid @RequestBody AdminReadingDeletedRecordPageQuery query) {
        return Result.success(adminReadingService.pageDeletedRecords(query));
    }

    @Operation(summary = "Get reading record detail")
    @GetMapping("/records/{recordId}")
    public Result<?> getRecord(@PathVariable Long recordId) {
        return Result.success(adminReadingService.getRecord(recordId));
    }

    @Operation(summary = "Delete reading record")
    @DeleteMapping("/records/{recordId}")
    public Result<?> deleteRecord(@PathVariable Long recordId) {
        adminReadingService.deleteRecord(recordId);
        return Result.success();
    }

    @Operation(summary = "Restore reading record")
    @PutMapping("/records/{recordId}/restore")
    public Result<?> restoreRecord(@PathVariable Long recordId) {
        adminReadingService.restoreRecord(recordId);
        return Result.success();
    }
}
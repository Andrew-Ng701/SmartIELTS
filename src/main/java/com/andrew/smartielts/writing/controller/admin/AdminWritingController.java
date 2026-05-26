package com.andrew.smartielts.writing.controller.admin;

import com.andrew.smartielts.common.resultDTO.Result;
import com.andrew.smartielts.writing.domain.dto.WritingQuestionDTO;
import com.andrew.smartielts.writing.service.admin.AdminWritingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @Operation(summary = "Get writing question")
    @GetMapping("/questions/{id}")
    public Result<?> getQuestion(@PathVariable Long id) {
        return Result.success(adminWritingService.getQuestion(id));
    }

    @Operation(summary = "Update writing question")
    @PutMapping("/questions/{id}")
    public Result<?> updateQuestion(@PathVariable Long id, @RequestBody WritingQuestionDTO dto) {
        return Result.success(adminWritingService.updateQuestion(id, dto));
    }

    @Operation(summary = "Replace writing question images")
    @PostMapping(value = "/questions/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<?> replaceQuestionImages(
            @PathVariable Long id,
            @RequestPart(value = "images", required = false) MultipartFile[] images) {
        return Result.success(adminWritingService.replaceQuestionImages(id, images));
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

}

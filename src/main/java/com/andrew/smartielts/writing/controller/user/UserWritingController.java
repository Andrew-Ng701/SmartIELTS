package com.andrew.smartielts.writing.controller.user;

import com.andrew.smartielts.common.resultDTO.Result;
import com.andrew.smartielts.utils.SecurityUtils;
import com.andrew.smartielts.writing.domain.query.user.UserWritingDeletedRecordPageQuery;
import com.andrew.smartielts.writing.domain.query.user.UserWritingRecordPageQuery;
import com.andrew.smartielts.writing.service.user.UserWritingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Tag(name = "User Writing API")
@RestController
@RequestMapping("/user/writing")
@PreAuthorize("hasRole('USER')")
@SecurityRequirement(name = "bearerAuth")
public class UserWritingController {

    @Autowired
    private UserWritingService userWritingService;

    @Operation(summary = "List all writing questions")
    @GetMapping("/questions")
    public Result<?> listQuestions() {
        return Result.success(userWritingService.listAllWritingPaper());
    }

    @Operation(summary = "Get writing question detail")
    @GetMapping("/questions/{questionId}")
    public Result<?> getQuestion(@PathVariable Long questionId) {
        return Result.success(userWritingService.getQuestion(questionId));
    }

    @Operation(summary = "Submit writing")
    @PostMapping(
            value = "/questions/{questionId}/submit",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Result<?> submit(@PathVariable Long questionId,
                            @RequestParam(value = "targetScore", required = false) BigDecimal targetScore,
                            @RequestParam(value = "textContent", required = false) String textContent,
                            @RequestParam(value = "images", required = false) MultipartFile[] images,
                            @RequestParam(value = "pdf", required = false) MultipartFile pdf) {
        return Result.success(userWritingService.submitRecord(questionId, targetScore, textContent, images, pdf));
    }

    @Operation(summary = "Get writing record detail")
    @GetMapping("/records/{recordId}")
    public Result<?> getRecord(@PathVariable Long recordId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userWritingService.getRecord(recordId, userId));
    }

    @Operation(summary = "User writing active records overview")
    @PostMapping("/records/overview")
    public Result<?> pageActiveRecords(@Valid @RequestBody UserWritingRecordPageQuery query) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userWritingService.pageActiveRecords(userId, query));
    }

    @Operation(summary = "User writing deleted records overview")
    @PostMapping("/records/deleted/overview")
    public Result<?> pageDeletedRecords(@Valid @RequestBody UserWritingDeletedRecordPageQuery query) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userWritingService.pageDeletedRecords(userId, query));
    }
}

package com.andrew.smartielts.writing.controller.user;

import com.andrew.smartielts.common.resultDTO.Result;
import com.andrew.smartielts.writing.service.user.UserWritingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    private final UserWritingService userWritingService;

    public UserWritingController(UserWritingService userWritingService) {
        this.userWritingService = userWritingService;
    }

    @Operation(summary = "List all writing questions")
    @GetMapping("/questions")
    public Result<?> listQuestions(@RequestParam(value = "taskType", required = false) String taskType) {
        return Result.success(userWritingService.listAllWritingPaper(taskType));
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
        return Result.success(
                userWritingService.submitRecord(questionId, targetScore, textContent, images, pdf)
        );
    }

}

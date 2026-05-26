package com.andrew.smartielts.speaking.controller.user;

import com.andrew.smartielts.common.resultDTO.Result;
import com.andrew.smartielts.speaking.domain.dto.NextQuestionRequestDTO;
import com.andrew.smartielts.speaking.domain.dto.StartExamRequestDTO;
import com.andrew.smartielts.speaking.domain.vo.UploadSpeakingAudioVO;
import com.andrew.smartielts.speaking.oss.service.SpeakingAudioStorageService;
import com.andrew.smartielts.speaking.service.user.UserSpeakingService;
import com.andrew.smartielts.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "User Speaking API")
@RestController
@RequestMapping("/user/speaking")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('USER')")
public class UserSpeakingController {

    private final UserSpeakingService speakingService;
    private final SpeakingAudioStorageService speakingAudioStorageService;

    public UserSpeakingController(
            UserSpeakingService speakingService,
            SpeakingAudioStorageService speakingAudioStorageService
    ) {
        this.speakingService = speakingService;
        this.speakingAudioStorageService = speakingAudioStorageService;
    }

    @Operation(summary = "List all speaking questions")
    @GetMapping("/questions")
    public Result<?> listAllSpeakingQuestion() {
        return Result.success(speakingService.listAllSpeakingQuestion());
    }

    @Operation(summary = "Start speaking exam")
    @PostMapping("/start-exam")
    public Result<?> startExam(@RequestBody(required = false) StartExamRequestDTO dto) {
        return Result.success(speakingService.startExam(dto));
    }

    @Operation(summary = "Get next speaking question")
    @PostMapping("/next-question")
    public Result<?> nextQuestion(@RequestBody NextQuestionRequestDTO dto) {
        return Result.success(speakingService.nextQuestion(dto));
    }

    @Operation(summary = "Submit speaking answer mp3 and score immediately")
    @PostMapping(value = "/submit-answer", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<?> submitAnswer(
            @RequestParam String sessionId,
            @RequestParam Long questionId,
            @RequestPart("file") MultipartFile file
    ) {
        return Result.success(speakingService.submitAnswer(sessionId, questionId, file));
    }

    @Operation(summary = "Get speaking session summary")
    @GetMapping("/sessions/{sessionId}/summary")
    public Result<?> getSessionSummary(@PathVariable String sessionId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(speakingService.getSessionSummary(sessionId, userId));
    }

    @Operation(summary = "Get D-ID speaking talk status")
    @GetMapping("/talks/{talkId}")
    public Result<?> getTalkStatus(@PathVariable String talkId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(speakingService.getTalkStatus(talkId, userId));
    }

    @Operation(summary = "Upload speaking audio to OSS")
    @PostMapping("/upload-audio")
    public Result<?> uploadAudio(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "sessionId", required = false) String sessionId,
            @RequestParam(value = "questionId", required = false) Long questionId
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        UploadSpeakingAudioVO vo = speakingAudioStorageService.uploadAudio(file, userId, sessionId, questionId);
        return Result.success(vo);
    }
}

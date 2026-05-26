package com.andrew.smartielts.listening.controller.admin;

import com.andrew.smartielts.common.domain.pojo.TestPartGroup;
import com.andrew.smartielts.common.image.service.BizImageResourceService;
import com.andrew.smartielts.common.resultDTO.Result;
import com.andrew.smartielts.common.storage.BucketType;
import com.andrew.smartielts.listening.constants.ListeningAudioConstants;
import com.andrew.smartielts.listening.domain.dto.AdminListeningTestFullSaveDTO;
import com.andrew.smartielts.listening.domain.dto.ListeningTestDTO;
import com.andrew.smartielts.listening.domain.pojo.ListeningAudio;
import com.andrew.smartielts.listening.service.admin.AdminListeningService;
import com.andrew.smartielts.listening.service.admin.impl.ListeningAudioServiceImpl;
import com.andrew.smartielts.listening.service.admin.impl.ListeningPartGroupServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Objects;

@Tag(name = "Admin Listening API")
@RestController
@RequestMapping("/admin/listening")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class AdminListeningController {

    private final AdminListeningService adminListeningService;
    private final ListeningAudioServiceImpl listeningAudioService;
    private final ListeningPartGroupServiceImpl listeningPartGroupService;
    private final BizImageResourceService bizImageResourceService;

    public AdminListeningController(
            AdminListeningService adminListeningService,
            ListeningAudioServiceImpl listeningAudioService,
            ListeningPartGroupServiceImpl listeningPartGroupService,
            BizImageResourceService bizImageResourceService) {
        this.adminListeningService = adminListeningService;
        this.listeningAudioService = listeningAudioService;
        this.listeningPartGroupService = listeningPartGroupService;
        this.bizImageResourceService = bizImageResourceService;
    }

    @Operation(summary = "Create listening test")
    @PostMapping("/tests")
    public Result<?> createTest(@Valid @RequestBody ListeningTestDTO dto) {
        return Result.success(adminListeningService.createTest(dto));
    }

    @Operation(summary = "Update listening test")
    @PutMapping("/tests/{id}")
    public Result<?> updateTest(@PathVariable Long id, @Valid @RequestBody ListeningTestDTO dto) {
        return Result.success(adminListeningService.updateTest(id, dto));
    }

    @Operation(summary = "Save full listening test")
    @PutMapping("/tests/{testId}/full")
    public Result<?> saveFullTest(@PathVariable Long testId,
                                  @Valid @RequestBody AdminListeningTestFullSaveDTO dto) {
        return Result.success(adminListeningService.saveFullTest(testId, dto));
    }

    @Operation(summary = "List listening tests")
    @GetMapping("/tests")
    public Result<?> listTests() {
        return Result.success(adminListeningService.listTests());
    }

    @Operation(summary = "Get listening test detail")
    @GetMapping("/tests/{testId}")
    public Result<?> getTestDetail(@PathVariable Long testId) {
        return Result.success(adminListeningService.getTestDetail(testId));
    }

    @Operation(summary = "Delete listening test")
    @DeleteMapping("/tests/{id}")
    public Result<?> deleteTest(@PathVariable Long id) {
        adminListeningService.deleteTest(id);
        return Result.success();
    }

    @Operation(summary = "Restore listening test")
    @PutMapping("/tests/{id}/restore")
    public Result<?> restoreTest(@PathVariable Long id) {
        adminListeningService.restoreTest(id);
        return Result.success();
    }

    @Operation(summary = "Create listening audio")
    @PostMapping(value = "/tests/{testId}/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<?> createTestAudio(
            @PathVariable Long testId,
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "title", required = false) String title,
            @RequestPart(value = "transcriptText", required = false) String transcriptText) {
        return Result.success(listeningAudioService.createTestAudioFromUpload(testId, title, transcriptText, file));
    }

    @Operation(summary = "Get listening test audio")
    @GetMapping("/tests/{testId}/audio")
    public Result<?> getTestAudio(@PathVariable Long testId) {
        return Result.success(listeningAudioService.getTestAudioByTestId(testId));
    }

    @Operation(summary = "Update listening audio")
    @PutMapping(value = "/tests/{testId}/audio/{audioId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<?> updateTestAudio(
            @PathVariable Long testId,
            @PathVariable Long audioId,
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "title", required = false) String title,
            @RequestPart(value = "transcriptText", required = false) String transcriptText) {
        ListeningAudio existing = requireAudio(audioId);
        assertTestAudioBelongsToTest(existing, testId);
        return Result.success(listeningAudioService.updateTestAudioFromUpload(audioId, testId, title, transcriptText, file));
    }

    @Operation(summary = "Delete listening audio")
    @DeleteMapping("/tests/{testId}/audio/{audioId}")
    public Result<?> deleteTestAudio(@PathVariable Long testId, @PathVariable Long audioId) {
        ListeningAudio existing = requireAudio(audioId);
        assertTestAudioBelongsToTest(existing, testId);
        listeningAudioService.deleteById(audioId);
        return Result.success();
    }

    @Operation(summary = "Create part-group listening audio")
    @PostMapping(value = "/tests/{testId}/part-groups/{partGroupId}/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<?> createPartGroupAudio(
            @PathVariable Long testId,
            @PathVariable Long partGroupId,
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "title", required = false) String title,
            @RequestPart(value = "transcriptText", required = false) String transcriptText) {
        assertPartGroupBelongsToTest(testId, partGroupId);
        return Result.success(listeningAudioService.createPartGroupAudioFromUpload(testId, partGroupId, title, transcriptText, file));
    }

    @Operation(summary = "List part-group listening audios")
    @GetMapping("/part-groups/{partGroupId}/audio")
    public Result<?> listPartGroupAudios(@PathVariable Long partGroupId) {
        return Result.success(listeningAudioService.listByPartGroupId(partGroupId));
    }

    @Operation(summary = "Update part-group listening audio")
    @PutMapping(value = "/tests/{testId}/part-groups/{partGroupId}/audio/{audioId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<?> updatePartGroupAudio(
            @PathVariable Long testId,
            @PathVariable Long partGroupId,
            @PathVariable Long audioId,
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "title", required = false) String title,
            @RequestPart(value = "transcriptText", required = false) String transcriptText) {
        assertPartGroupBelongsToTest(testId, partGroupId);
        ListeningAudio existing = requireAudio(audioId);
        assertPartGroupAudioBelongs(existing, testId, partGroupId);
        return Result.success(listeningAudioService.updatePartGroupAudioFromUpload(audioId, testId, partGroupId, title, transcriptText, file));
    }

    @Operation(summary = "Delete part-group listening audio")
    @DeleteMapping("/tests/{testId}/part-groups/{partGroupId}/audio/{audioId}")
    public Result<?> deletePartGroupAudio(
            @PathVariable Long testId,
            @PathVariable Long partGroupId,
            @PathVariable Long audioId) {
        assertPartGroupBelongsToTest(testId, partGroupId);
        ListeningAudio existing = requireAudio(audioId);
        assertPartGroupAudioBelongs(existing, testId, partGroupId);
        listeningAudioService.deleteById(audioId);
        return Result.success();
    }

    @Operation(summary = "Replace listening part group images")
    @PostMapping(value = "/part-groups/{partGroupId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<?> replacePartGroupImagesFromUpload(
            @PathVariable Long partGroupId,
            @RequestPart(value = "images", required = false) MultipartFile[] images) {
        requirePartGroup(partGroupId);
        return Result.success(bizImageResourceService.replaceByTargetFromUploads(
                ListeningAudioConstants.TARGET_TYPE_LISTENING_PART_GROUP,
                partGroupId,
                BucketType.QUESTION_GROUP_IMAGE,
                ListeningAudioConstants.BIZ_PATH_LISTENING_PART_GROUP_IMAGE,
                images
        ));
    }

    @Operation(summary = "Replace listening question images")
    @PostMapping(value = "/questions/{questionId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<?> replaceQuestionImages(
            @PathVariable Long questionId,
            @RequestPart(value = "images", required = false) MultipartFile[] images) {
        adminListeningService.replaceQuestionImages(questionId, images);
        return Result.success(bizImageResourceService.listByTarget(
                ListeningAudioConstants.TARGET_TYPE_LISTENING_QUESTION,
                questionId
        ));
    }

    private ListeningAudio requireAudio(Long audioId) {
        ListeningAudio audio = listeningAudioService.getById(audioId);
        if (audio == null) {
            throw new RuntimeException("listening_audio_not_found");
        }
        return audio;
    }

    private TestPartGroup requirePartGroup(Long partGroupId) {
        TestPartGroup partGroup = listeningPartGroupService.getActiveById(partGroupId);
        if (partGroup == null) {
            throw new RuntimeException("listening_part_group_not_found");
        }
        return partGroup;
    }

    private void assertPartGroupBelongsToTest(Long testId, Long partGroupId) {
        TestPartGroup partGroup = requirePartGroup(partGroupId);
        if (!Objects.equals(partGroup.getTestId(), testId)) {
            throw new RuntimeException("listening_part_group_not_found");
        }
    }

    private void assertTestAudioBelongsToTest(ListeningAudio audio, Long testId) {
        if (!Objects.equals(audio.getTestId(), testId) || audio.getPartGroupId() != null) {
            throw new RuntimeException("listening_test_audio_not_found");
        }
    }

    private void assertPartGroupAudioBelongs(ListeningAudio audio, Long testId, Long partGroupId) {
        if (!Objects.equals(audio.getTestId(), testId) || !Objects.equals(audio.getPartGroupId(), partGroupId)) {
            throw new RuntimeException("listening_part_group_audio_not_found");
        }
    }

}

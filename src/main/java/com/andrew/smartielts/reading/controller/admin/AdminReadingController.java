package com.andrew.smartielts.reading.controller.admin;

import com.andrew.smartielts.common.domain.pojo.TestPartGroup;
import com.andrew.smartielts.common.image.service.BizImageResourceService;
import com.andrew.smartielts.common.resultDTO.Result;
import com.andrew.smartielts.common.storage.BucketType;
import com.andrew.smartielts.reading.constant.ReadingStorageConstants;
import com.andrew.smartielts.reading.domain.dto.AdminReadingTestFullSaveDTO;
import com.andrew.smartielts.reading.domain.dto.ReadingTestDTO;
import com.andrew.smartielts.reading.service.admin.AdminReadingService;
import com.andrew.smartielts.reading.service.admin.impl.ReadingPartGroupServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Admin Reading API")
@RestController
@RequestMapping("/admin/reading")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class AdminReadingController {

    private final AdminReadingService adminReadingService;
    private final ReadingPartGroupServiceImpl readingPartGroupService;
    private final BizImageResourceService bizImageResourceService;

    public AdminReadingController(AdminReadingService adminReadingService,
                                  ReadingPartGroupServiceImpl readingPartGroupService,
                                  BizImageResourceService bizImageResourceService) {
        this.adminReadingService = adminReadingService;
        this.readingPartGroupService = readingPartGroupService;
        this.bizImageResourceService = bizImageResourceService;
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

    @Operation(summary = "Save full reading test")
    @PutMapping("/tests/{testId}/full")
    public Result<?> saveFullTest(@PathVariable Long testId, @Valid @RequestBody AdminReadingTestFullSaveDTO dto) {
        return Result.success(adminReadingService.saveFullTest(testId, dto));
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

    @Operation(summary = "Replace reading part group images")
    @PostMapping(value = "/part-groups/{partGroupId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<?> replacePartGroupImagesFromUpload(
            @PathVariable Long partGroupId,
            @RequestPart(value = "images", required = false) MultipartFile[] images) {
        requirePartGroup(partGroupId);
        return Result.success(bizImageResourceService.replaceByTargetFromUploads(
                ReadingStorageConstants.TARGET_TYPE_READING_PART_GROUP,
                partGroupId,
                BucketType.QUESTION_GROUP_IMAGE,
                ReadingStorageConstants.BIZ_PATH_READING_PART_GROUP_IMAGE,
                images
        ));
    }

    @Operation(summary = "Replace reading question images")
    @PostMapping(value = "/questions/{questionId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<?> replaceQuestionImages(
            @PathVariable Long questionId,
            @RequestPart(value = "images", required = false) MultipartFile[] images) {
        adminReadingService.replaceQuestionImages(questionId, images);
        return Result.success(bizImageResourceService.listByTarget(
                ReadingStorageConstants.TARGET_TYPE_READING_QUESTION,
                questionId
        ));
    }

    private TestPartGroup requirePartGroup(Long partGroupId) {
        TestPartGroup partGroup = readingPartGroupService.getActiveById(partGroupId);
        if (partGroup == null) {
            throw new RuntimeException("reading_part_group_not_found");
        }
        return partGroup;
    }
}

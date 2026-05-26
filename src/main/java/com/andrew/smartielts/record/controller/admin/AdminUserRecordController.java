package com.andrew.smartielts.record.controller.admin;

import com.andrew.smartielts.common.resultDTO.Result;
import com.andrew.smartielts.record.domain.query.admin.AdminUserRecordListQuery;
import com.andrew.smartielts.record.service.admin.AdminUserRecordService;
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

@Tag(name = "Admin Record API")
@RestController
@RequestMapping("/admin/records")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserRecordController {

    private final AdminUserRecordService adminUserRecordService;

    public AdminUserRecordController(AdminUserRecordService adminUserRecordService) {
        this.adminUserRecordService = adminUserRecordService;
    }

    @Operation(summary = "List records across users and modules")
    @PostMapping("/list")
    public Result<?> listRecords(@RequestBody(required = false) AdminUserRecordListQuery query) {
        return Result.success(adminUserRecordService.listRecords(query));
    }

    @Operation(summary = "Get admin record detail")
    @GetMapping("/{module}/{recordId}")
    public Result<?> getRecord(@PathVariable String module, @PathVariable Long recordId) {
        return Result.success(adminUserRecordService.getRecord(module, recordId));
    }

    @Operation(summary = "Delete admin record")
    @DeleteMapping("/{module}/{recordId}")
    public Result<?> deleteRecord(@PathVariable String module, @PathVariable Long recordId) {
        adminUserRecordService.deleteRecord(module, recordId);
        return Result.success();
    }

    @Operation(summary = "Restore admin record")
    @PutMapping("/{module}/{recordId}/restore")
    public Result<?> restoreRecord(@PathVariable String module, @PathVariable Long recordId) {
        adminUserRecordService.restoreRecord(module, recordId);
        return Result.success();
    }
}

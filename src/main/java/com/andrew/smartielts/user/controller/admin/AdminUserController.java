package com.andrew.smartielts.user.controller.admin;

import com.andrew.smartielts.common.resultDTO.Result;
import com.andrew.smartielts.user.domain.query.admin.AdminDeletedUserPageQuery;
import com.andrew.smartielts.user.domain.query.admin.AdminUserPageQuery;
import com.andrew.smartielts.record.domain.query.admin.AdminUserScopedRecordListQuery;
import com.andrew.smartielts.user.service.admin.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin User Control API")
@RestController
@RequestMapping("/admin/users")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    @Operation(summary = "List users")
    @GetMapping("/list")
    public Result<?> listUsers(AdminUserPageQuery query) {
        return Result.success(adminUserService.listUsers(query));
    }

    @Operation(summary = "Admin deleted user overview")
    @PostMapping("/deleted/overview")
    public Result<?> pageDeletedUsers(@RequestBody(required = false) AdminDeletedUserPageQuery query) {
        return Result.success(adminUserService.pageDeletedUsers(query));
    }

    @Operation(summary = "Get user detail")
    @GetMapping("/{userId}")
    public Result<?> getUserDetail(@PathVariable Long userId) {
        return Result.success(adminUserService.getUserDetail(userId));
    }

    @Operation(summary = "Get user record detail")
    @GetMapping("/{userId}/records/{moduleType}/{recordId}")
    public Result<?> getUserRecordDetail(@PathVariable Long userId,
                                         @PathVariable String moduleType,
                                         @PathVariable Long recordId) {
        return Result.success(adminUserService.getUserRecordDetail(userId, moduleType, recordId));
    }

    @Operation(summary = "List records for admin-selected user")
    @PostMapping("/{userId}/records")
    public Result<?> listUserRecords(@PathVariable Long userId,
                                     @RequestBody(required = false) AdminUserScopedRecordListQuery query) {
        return Result.success(adminUserService.listUserRecords(userId, query));
    }

    @Operation(summary = "Delete user")
    @DeleteMapping("/{userId}")
    public Result<?> deleteUser(@PathVariable Long userId) {
        adminUserService.deleteUser(userId);
        return Result.success();
    }

    @Operation(summary = "Restore user")
    @PutMapping("/{userId}/restore")
    public Result<?> restoreUser(@PathVariable Long userId) {
        adminUserService.restoreUser(userId);
        return Result.success();
    }

}


package com.andrew.smartielts.admin.controller;

import com.andrew.smartielts.admin.service.AdminService;
import com.andrew.smartielts.common.resultDTO.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Console API")
@RestController
@RequestMapping("/admin")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @Operation(summary = "Admin overview")
    @GetMapping("/overview")
    public Result<?> overview() {
        return Result.success(adminService.overview());
    }

    @Operation(summary = "Admin recent issues")
    @GetMapping("/recent-issues")
    public Result<?> recentIssues() {
        return Result.success(adminService.recentIssues());
    }

    @Operation(summary = "Admin quick links")
    @GetMapping("/quick-links")
    public Result<?> quickLinks() {
        return Result.success(adminService.quickLinks());
    }

    @Operation(summary = "Admin module stats")
    @GetMapping("/module-stats")
    public Result<?> moduleStats() {
        return Result.success(adminService.moduleStats());
    }

    @Operation(summary = "Admin user console summary")
    @GetMapping("/users/{userId}/console-summary")
    public Result<?> userConsoleSummary(@PathVariable Long userId) {
        return Result.success(adminService.userConsoleSummary(userId));
    }
}
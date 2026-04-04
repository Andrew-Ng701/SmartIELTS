package com.andrew.smartielts.user.controller.user;

import com.andrew.smartielts.common.resultDTO.Result;
import com.andrew.smartielts.user.domain.dto.UserProfileUpdateDTO;
import com.andrew.smartielts.user.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User Control API")
@RestController
@RequestMapping("/user")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('USER')")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "Get current user profile")
    @GetMapping("/profile")
    public Result<?> profile() {
        return Result.success(userService.getProfile());
    }

    @Operation(summary = "Update current user profile")
    @PutMapping("/profile")
    public Result<?> updateProfile(@Valid @RequestBody UserProfileUpdateDTO dto) {
        return Result.success(userService.updateProfile(dto));
    }

    @Operation(summary = "Get current user overview")
    @GetMapping("/overview")
    public Result<?> overview() {
        return Result.success(userService.getOverview());
    }

    @Operation(summary = "Get current user counts")
    @GetMapping("/stats/count")
    public Result<?> counts() {
        return Result.success(userService.getStats());
    }
}
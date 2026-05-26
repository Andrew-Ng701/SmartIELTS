package com.andrew.smartielts.console.controller;

import com.andrew.smartielts.common.resultDTO.Result;
import com.andrew.smartielts.console.domain.vo.UserConsoleVO;
import com.andrew.smartielts.console.service.UserConsoleService;
import com.andrew.smartielts.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User Console API")
@RestController
@RequestMapping("/user/console")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class UserConsoleController {

    private final UserConsoleService userConsoleService;

    @Operation(summary = "Get current user full console")
    @GetMapping
    public Result<UserConsoleVO> console() {
        return Result.success(userConsoleService.console(SecurityUtils.getCurrentUserId()));
    }
}

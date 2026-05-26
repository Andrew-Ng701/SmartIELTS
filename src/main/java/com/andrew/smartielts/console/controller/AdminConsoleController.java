package com.andrew.smartielts.console.controller;

import com.andrew.smartielts.common.resultDTO.Result;
import com.andrew.smartielts.console.domain.vo.AdminConsoleVO;
import com.andrew.smartielts.console.service.AdminConsoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Console API")
@RestController
@RequestMapping("/admin/console")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminConsoleController {

    private final AdminConsoleService adminConsoleService;

    @Operation(summary = "Admin full console")
    @GetMapping
    public Result<AdminConsoleVO> console() {
        return Result.success(adminConsoleService.console());
    }
}

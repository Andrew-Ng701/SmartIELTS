package com.andrew.smartielts.auth.controller;

import com.andrew.smartielts.auth.domain.dto.AuthResponseDTO;
import com.andrew.smartielts.auth.domain.dto.UserDTO;
import com.andrew.smartielts.auth.service.LoginService;
import com.andrew.smartielts.auth.service.RegisterService;
import com.andrew.smartielts.common.resultDTO.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private RegisterService registerService;

    @Autowired
    private LoginService loginService;

    @PostMapping("/register")
    public Result<?> register(@RequestBody UserDTO dto) {
        AuthResponseDTO response = registerService.register(dto);
        return Result.success(response);
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody UserDTO dto) {
        return Result.success(loginService.login(dto));
    }
}
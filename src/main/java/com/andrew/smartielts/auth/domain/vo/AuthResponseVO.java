package com.andrew.smartielts.auth.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AuthResponseVO {

    private String token;
    private Long userId;
    private String role;

}
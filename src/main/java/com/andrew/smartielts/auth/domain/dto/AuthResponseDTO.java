package com.andrew.smartielts.auth.domain.dto;

import lombok.Getter;

@Getter
public class AuthResponseDTO {

    private final String token;
    private final Long userId;
    private final String role;

    public AuthResponseDTO(String token, Long userId, String role) {
        this.token = token;
        this.userId = userId;
        this.role = role;
    }

}
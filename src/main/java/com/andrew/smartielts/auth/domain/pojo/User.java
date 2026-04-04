package com.andrew.smartielts.auth.domain.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {

    private Long id;

    private String email;

    private String password;

    private String role;

    private Integer isDeleted;

    private LocalDateTime deletedTime;

    private LocalDateTime createdTime;

    private Long tokenVersion;
}
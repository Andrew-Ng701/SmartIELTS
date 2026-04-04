package com.andrew.smartielts.user.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserProfileVO {

    private Long id;

    private String email;

    private String role;

    private Integer isDeleted;

    private LocalDateTime deletedTime;

    private LocalDateTime createdTime;
}

package com.andrew.smartielts.dashboard.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserCountVO {

    private long totalUsers;
    private long activeUsers;
    private long deletedUsers;

    private LocalDateTime generatedAt;
}
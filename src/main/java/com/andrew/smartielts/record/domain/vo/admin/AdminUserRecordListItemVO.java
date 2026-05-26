package com.andrew.smartielts.record.domain.vo.admin;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminUserRecordListItemVO {

    private Long recordId;

    private Long userId;

    private String name;

    private String module;

    private String status;

    private BigDecimal score;

    private String scoreText;

    private LocalDateTime updatedTime;

    private LocalDateTime createdTime;

    private Integer isDeleted;

    private LocalDateTime deletedTime;
}

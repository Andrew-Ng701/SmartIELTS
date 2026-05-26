package com.andrew.smartielts.user.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserAdminDetailVO {

    private Long id;

    private String email;

    private String role;

    private Integer isDeleted;

    private LocalDateTime deletedTime;

    private LocalDateTime createdTime;

    private LocalDateTime lastLoginTime;

    private String profilePictureUrl;

    private String profilePictureObjectKey;

    private BigDecimal listeningTargetScore;

    private BigDecimal readingTargetScore;

    private BigDecimal writingTargetScore;

    private BigDecimal speakingTargetScore;

    private Long totalActiveRecordCount;

    private Long totalDeletedRecordCount;

    private List<UserRecordCountVO> recordCounts;
}

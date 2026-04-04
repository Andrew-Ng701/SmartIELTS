package com.andrew.smartielts.admin.domain.vo;

import lombok.Data;

@Data
public class AdminModuleStatVO {
    private String module;
    private Long activeCount;
    private Long deletedCount;
    private Long totalCount;
}
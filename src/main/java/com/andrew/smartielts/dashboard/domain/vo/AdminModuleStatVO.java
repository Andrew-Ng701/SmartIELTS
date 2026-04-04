package com.andrew.smartielts.dashboard.domain.vo;

import lombok.Data;

@Data
public class AdminModuleStatVO {

    /**
     * listening / reading / writing / speaking
     */
    private String module;

    private long activeCount;
    private long deletedCount;
}
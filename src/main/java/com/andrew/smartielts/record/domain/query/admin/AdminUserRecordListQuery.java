package com.andrew.smartielts.record.domain.query.admin;

import lombok.Data;

@Data
public class AdminUserRecordListQuery {

    private String recordState = "ACTIVE";

    private String module;

    private String status;

    private Long userId;

    private String sort = "UPDATED_DESC";

    private Integer pageNum = 1;

    private Integer pageSize = 20;
}

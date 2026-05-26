package com.andrew.smartielts.record.domain.query.admin;

import lombok.Data;

@Data
public class AdminUserScopedRecordListQuery {

    private String recordState = "ACTIVE";

    private String module;

    private String status;

    private String sort = "UPDATED_DESC";

    private Integer pageNum = 1;

    private Integer pageSize = 20;
}

package com.andrew.smartielts.record.domain.query;

import lombok.Data;

@Data
public class UserRecordListQuery {

    private String recordState = "ACTIVE";

    private String module;

    private String status;

    private String sort = "UPDATED_DESC";

    private Integer pageNum = 1;

    private Integer pageSize = 20;
}

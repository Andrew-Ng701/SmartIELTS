package com.andrew.smartielts.user.domain.query.admin;

import com.andrew.smartielts.common.page.SortDirectionEnum;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserPageQuery {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    private String role;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private SortDirectionEnum sortDirection = SortDirectionEnum.DESC;
}

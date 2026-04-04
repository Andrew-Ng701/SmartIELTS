package com.andrew.smartielts.user.domain.query.admin;

import com.andrew.smartielts.common.page.SortDirectionEnum;
import lombok.Data;

@Data
public class AdminDeletedUserPageQuery {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    private SortDirectionEnum sortDirection = SortDirectionEnum.DESC;
}

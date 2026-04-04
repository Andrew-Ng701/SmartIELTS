package com.andrew.smartielts.listening.domain.query.user;

import com.andrew.smartielts.common.page.SortDirectionEnum;
import lombok.Data;

@Data
public class UserListeningDeletedRecordPageQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private SortDirectionEnum sortDirection = SortDirectionEnum.DESC;
}

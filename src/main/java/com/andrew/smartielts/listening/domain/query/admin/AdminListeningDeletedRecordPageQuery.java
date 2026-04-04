package com.andrew.smartielts.listening.domain.query.admin;

import com.andrew.smartielts.common.page.SortDirectionEnum;
import com.andrew.smartielts.common.query.DeletedRecordPageQuery;
import com.andrew.smartielts.common.validator.ValidDeletedRecordPageQuery;
import lombok.Data;

@Data
@ValidDeletedRecordPageQuery
public class AdminListeningDeletedRecordPageQuery implements DeletedRecordPageQuery {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    private SortDirectionEnum sortDirection = SortDirectionEnum.DESC;
}
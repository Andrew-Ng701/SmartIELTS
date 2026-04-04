package com.andrew.smartielts.listening.domain.query.admin;

import com.andrew.smartielts.common.page.SortDirectionEnum;
import com.andrew.smartielts.common.query.RecordPageQuery;
import com.andrew.smartielts.common.validator.ValidRecordPageQuery;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ValidRecordPageQuery
public class AdminListeningRecordPageQuery implements RecordPageQuery {

    private Integer pageNum = 1;
    private Integer pageSize = 10;

    private Long userId;
    private Long testId;
    private Integer minScore;
    private Integer maxScore;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private SortDirectionEnum sortDirection = SortDirectionEnum.DESC;
}
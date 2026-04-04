package com.andrew.smartielts.writing.domain.query.admin;

import com.andrew.smartielts.common.page.SortDirectionEnum;
import com.andrew.smartielts.common.validator.writing.ValidWritingRecordPageQuery;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ValidWritingRecordPageQuery
public class AdminWritingRecordPageQuery {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    private Long userId;

    private Long questionId;

    private String inputType;

    private String aiStatus;

    private BigDecimal targetScore;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private SortDirectionEnum sortDirection = SortDirectionEnum.DESC;
}
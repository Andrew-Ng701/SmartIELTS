package com.andrew.smartielts.speaking.domain.query.admin;

import com.andrew.smartielts.common.page.SortDirectionEnum;
import com.andrew.smartielts.common.validator.speaking.ValidSpeakingRecordPageQuery;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ValidSpeakingRecordPageQuery
public class AdminSpeakingRecordPageQuery {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    private Long userId;

    private String sessionId;

    private String part;

    private String answerStatus;

    private String aiStatus;

    private Integer minOverallScore;

    private Integer maxOverallScore;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private SortDirectionEnum sortDirection = SortDirectionEnum.DESC;
}

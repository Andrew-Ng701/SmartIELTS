package com.andrew.smartielts.speaking.domain.query.user;

import com.andrew.smartielts.common.page.SortDirectionEnum;
import com.andrew.smartielts.common.validator.speaking.ValidSpeakingRecordPageQuery;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ValidSpeakingRecordPageQuery
public class UserSpeakingRecordPageQuery {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    private String sessionId;

    private String part;

    private String answerStatus;

    private Integer minOverallScore;

    private Integer maxOverallScore;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private SortDirectionEnum sortDirection = SortDirectionEnum.DESC;
}

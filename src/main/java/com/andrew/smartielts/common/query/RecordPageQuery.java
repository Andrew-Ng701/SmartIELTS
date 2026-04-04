package com.andrew.smartielts.common.query;

import java.time.LocalDateTime;

public interface RecordPageQuery {

    Integer getPageNum();

    Integer getPageSize();

    Long getUserId();

    Long getTestId();

    Integer getMinScore();

    Integer getMaxScore();

    LocalDateTime getStartTime();

    LocalDateTime getEndTime();

    Object getSortDirection();
}
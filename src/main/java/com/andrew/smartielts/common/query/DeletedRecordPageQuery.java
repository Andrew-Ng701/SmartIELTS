package com.andrew.smartielts.common.query;

public interface DeletedRecordPageQuery {

    Integer getPageNum();

    Integer getPageSize();

    Object getSortDirection();
}
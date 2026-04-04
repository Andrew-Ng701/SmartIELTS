package com.andrew.smartielts.dashboard.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class DashboardApiResponse<T> {

    private Integer code;
    private String msg;
    private T data;
    private Map<String, Object> meta;
}
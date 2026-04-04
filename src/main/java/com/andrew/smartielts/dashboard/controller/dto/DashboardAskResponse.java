package com.andrew.smartielts.dashboard.controller.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class DashboardAskResponse {

    private Integer code;
    private String msg;
    private Object data;
    private Map<String, Object> meta;
}
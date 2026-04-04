package com.andrew.smartielts.dashboard.controller.dto;

import lombok.Data;

import java.util.Map;

@Data
public class DashboardAskClientContext {

    private String pageName;
    private String route;
    private String tab;
    private String locale;
    private String clientTime;
    private Map<String, Object> ext;
}
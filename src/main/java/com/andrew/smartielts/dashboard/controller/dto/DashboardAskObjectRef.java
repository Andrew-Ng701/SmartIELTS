package com.andrew.smartielts.dashboard.controller.dto;

import lombok.Data;

@Data
public class DashboardAskObjectRef {

    private String module;       // listening / reading / writing / speaking
    private String objectType;   // test / passage / question / record / session
    private Long testId;
    private Long passageId;
    private Long questionId;
    private Long recordId;
    private Integer questionNumber;
    private String sessionId;
}
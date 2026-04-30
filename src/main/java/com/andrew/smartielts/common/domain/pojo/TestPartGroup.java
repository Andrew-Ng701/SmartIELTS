package com.andrew.smartielts.common.domain.pojo;

import lombok.Data;

import java.util.List;

@Data
public class TestPartGroup {
    private Long id;
    private Long testId;
    private Integer partNumber;
    private Integer groupNumber;
    private String title;
    private String instructionText;
    private String groupGuideText;
    private String groupRequirementText;
    private Integer questionNoStart;
    private Integer questionNoEnd;
    private Integer displayOrder;
    private Integer timeLimitSeconds;
    private Integer isDeleted;
    private List<BizImageResource> images;
}
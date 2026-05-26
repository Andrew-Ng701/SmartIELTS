package com.andrew.smartielts.writing.domain.dto;

import com.andrew.smartielts.common.image.domain.dto.BizImageResourceDTO;
import lombok.Data;

import java.util.List;

@Data
public class WritingQuestionDTO {

    private String taskType;

    private String chartType;

    private String title;

    private String description;

    private String imageDetailDescription;

    private Integer prepMinutes;

    private Integer totalMinutes;

    private Integer prepSeconds;

    private Integer totalSeconds;

    private List<BizImageResourceDTO> images;
}

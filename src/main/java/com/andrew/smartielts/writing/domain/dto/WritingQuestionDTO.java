package com.andrew.smartielts.writing.domain.dto;

import com.andrew.smartielts.common.domain.dto.BizImageResourceDTO;
import lombok.Data;

import java.util.List;

@Data
public class WritingQuestionDTO {

    private String taskType;

    private String title;

    private String description;

    private List<BizImageResourceDTO> images;
}
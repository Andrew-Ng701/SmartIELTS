package com.andrew.smartielts.reading.domain.dto;

import com.andrew.smartielts.common.domain.pojo.TestPartGroup;
import lombok.Data;

import java.util.List;

@Data
public class AdminReadingTestFullSaveDTO {
    private ReadingTestDTO test;
    private List<TestPartGroup> partGroups;
    private List<ReadingPassageDTO> passages;
    private List<ReadingQuestionDTO> questions;
}

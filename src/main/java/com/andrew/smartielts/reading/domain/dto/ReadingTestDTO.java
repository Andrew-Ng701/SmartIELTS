package com.andrew.smartielts.reading.domain.dto;

import com.andrew.smartielts.common.domain.pojo.TestPartGroup;
import com.andrew.smartielts.common.domain.pojo.TestTimerConfig;
import lombok.Data;

import java.util.List;

@Data
public class ReadingTestDTO {
    private String title;
    private Integer totalScore;
    private TestTimerConfig timerConfig;
    private List<TestPartGroup> partGroups;
}
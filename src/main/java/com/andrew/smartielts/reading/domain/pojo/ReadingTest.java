package com.andrew.smartielts.reading.domain.pojo;

import com.andrew.smartielts.common.domain.pojo.TestPartGroup;
import com.andrew.smartielts.common.domain.pojo.TestTimerConfig;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReadingTest {
    private Long id;
    private String title;
    private Integer totalScore;
    private LocalDateTime createdTime;
    private Integer isDeleted;

    private TestTimerConfig timerConfig;
    private List<TestPartGroup> partGroups;
}
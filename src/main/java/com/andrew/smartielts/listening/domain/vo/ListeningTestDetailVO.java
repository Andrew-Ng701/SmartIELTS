package com.andrew.smartielts.listening.domain.vo;

import com.andrew.smartielts.common.domain.pojo.TestPartGroup;
import com.andrew.smartielts.common.domain.pojo.TestTimerConfig;
import com.andrew.smartielts.listening.domain.pojo.ListeningMaterial;
import lombok.Data;

import java.util.List;

@Data
public class ListeningTestDetailVO {
    private Long id;
    private String title;
    private String audioUrl;
    private String transcriptText;
    private Integer totalScore;

    private TestTimerConfig timerConfig;
    private List<TestPartGroup> partGroups;
    private List<ListeningMaterial> materials;
    private List<ListeningQuestionVO> questions;
}
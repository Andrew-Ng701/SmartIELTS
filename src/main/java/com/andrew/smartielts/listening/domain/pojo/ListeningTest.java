package com.andrew.smartielts.listening.domain.pojo;

import com.andrew.smartielts.common.domain.pojo.TestPartGroup;
import com.andrew.smartielts.listening.domain.pojo.ListeningMaterial;
import com.andrew.smartielts.common.domain.pojo.TestTimerConfig;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ListeningTest {
    private Long id;
    private String title;
    private String audioUrl;
    private Integer totalScore;
    private String audioObjectKey;
    private String transcriptText;
    private LocalDateTime createdTime;
    private Integer isDeleted;

    private TestTimerConfig timerConfig;
    private List<TestPartGroup> partGroups;
    private List<ListeningMaterial> materials;
}
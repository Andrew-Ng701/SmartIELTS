package com.andrew.smartielts.listening.domain.dto;

import com.andrew.smartielts.common.domain.dto.BizImageResourceDTO;
import lombok.Data;

import java.util.List;

@Data
public class ListeningTestDTO {

    private String title;
    private Integer totalScore;
    private String transcriptText;
    private TimerConfigInput timerConfig;
    private List<PartGroupInput> partGroups;
    private List<MaterialInput> materials;

    @Data
    public static class TimerConfigInput {
        private String timerMode;
        private Integer totalSeconds;
        private Integer autoSubmit;
        private Integer allowPause;
    }

    @Data
    public static class PartGroupInput {
        private Long id;
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
        private List<BizImageResourceDTO> images;
    }

    @Data
    public static class MaterialInput {
        private Long id;
        private Long partGroupId;
        private String title;
        private String audioUrl;
        private String audioObjectKey;
        private String transcriptText;
        private Integer displayOrder;
    }
}
package com.andrew.smartielts.listening.domain.dto;

import com.andrew.smartielts.common.domain.pojo.TestPartGroup;
import lombok.Data;

import java.util.List;

@Data
public class AdminListeningTestFullSaveDTO {
    private ListeningTestDTO test;
    private List<TestPartGroup> partGroups;
    private List<ListeningQuestionDTO> questions;
    private List<ListeningAudioUpsertDTO> audios;
}

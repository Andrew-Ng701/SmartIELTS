package com.andrew.smartielts.listening.domain.vo;

import com.andrew.smartielts.listening.domain.pojo.ListeningAudio;
import lombok.Data;

import java.util.List;

@Data
public class ListeningSectionScriptVO {

    private Long recordId;
    private Long testId;
    private Integer sectionNumber;
    private String sectionTitle;
    private String transcriptText;
    private List<ListeningAudio> audios;
}

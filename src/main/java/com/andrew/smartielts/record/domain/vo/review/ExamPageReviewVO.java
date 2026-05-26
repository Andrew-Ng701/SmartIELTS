package com.andrew.smartielts.record.domain.vo.review;

import com.andrew.smartielts.listening.domain.pojo.ListeningAudio;
import lombok.Data;

import java.util.List;

@Data
public class ExamPageReviewVO {

    private Long testId;

    private String testTitle;

    private Integer totalScore;

    private ListeningAudio testAudio;

    private Integer allowAudioSeek;

    private List<?> parts;

    private List<ListeningAudio> partGroupAudios;

    private List<?> questions;

    private List<?> answers;

    private List<QuestionReviewVO> questionReviews;
}

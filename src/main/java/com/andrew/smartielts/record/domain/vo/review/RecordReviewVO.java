package com.andrew.smartielts.record.domain.vo.review;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RecordReviewVO {

    private String moduleType;

    private Long recordId;

    private Long userId;

    private String layoutType;

    private String title;

    private BigDecimal score;

    private String scoreText;

    private String status;

    private LocalDateTime createdTime;

    private ExamPageReviewVO examPageReview;

    private WritingReviewVO writingReview;

    private SpeakingSessionReviewVO speakingSessionReview;
}

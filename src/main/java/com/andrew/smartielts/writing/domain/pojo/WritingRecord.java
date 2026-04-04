package com.andrew.smartielts.writing.domain.pojo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WritingRecord {
    private Long id;
    private Long userId;
    private Long questionId;
    private String inputType;
    private String textContent;
    private String extractedText;
    private BigDecimal targetScore;
    private BigDecimal aiScore;
    private String aiFeedback;
    private String aiRawResponse;
    private String aiStatus;
    private String aiProvider;
    private String aiModel;
    private Integer isDeleted;
    private LocalDateTime deletedTime;
    private LocalDateTime createdTime;
}

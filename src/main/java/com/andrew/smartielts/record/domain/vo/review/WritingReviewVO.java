package com.andrew.smartielts.record.domain.vo.review;

import com.andrew.smartielts.common.image.domain.pojo.BizImageResource;
import com.andrew.smartielts.writing.domain.vo.WritingAttachmentVO;
import com.andrew.smartielts.writing.domain.vo.WritingPreviewAssetVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class WritingReviewVO {

    private Long questionId;

    private String questionTitle;

    private String questionDescription;

    private String prompt;

    private String imageDetailDescription;

    private String questionImageUrl;

    private List<BizImageResource> questionImages;

    private List<WritingPreviewAssetVO> previewAssets;

    private String taskType;

    private String inputType;

    private String answerText;

    private String answerSource;

    private String textContent;

    private String extractedText;

    private String answerPreview;

    private Integer attachmentCount;

    private List<WritingAttachmentVO> attachments;

    private BigDecimal targetScore;

    private BigDecimal aiScore;

    private String aiFeedback;

    private String aiStatus;

    private String aiProvider;

    private String aiModel;
}

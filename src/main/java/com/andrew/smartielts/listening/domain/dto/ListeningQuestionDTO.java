package com.andrew.smartielts.listening.domain.dto;

import com.andrew.smartielts.common.domain.dto.BizImageResourceDTO;
import com.andrew.smartielts.common.domain.pojo.QuestionAnswerRule;
import lombok.Data;

import java.util.List;

@Data
public class ListeningQuestionDTO {
    private Long testId;
    private Long partGroupId;
    private Long materialId;
    private Integer sectionNumber;
    private Integer questionNumber;
    private String questionType;
    private String answerMode;
    private String questionText;
    private String correctAnswer;
    private String optionsJson;
    private String acceptedAnswersJson;
    private Integer caseInsensitive;
    private Integer ignoreWhitespace;
    private Integer ignorePunctuation;
    private Integer displayOrder;
    private Integer score;
    private List<BizImageResourceDTO> groupImages;

    private List<QuestionAnswerRule> answerRules;
}
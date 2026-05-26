package com.andrew.smartielts.writing.ai.service;

import com.andrew.smartielts.common.image.domain.pojo.BizImageResource;
import com.andrew.smartielts.writing.domain.pojo.WritingQuestion;

import java.util.List;

public interface WritingQuestionImageDescriptionService {

    String describeQuestionImages(WritingQuestion question, List<BizImageResource> images);
}

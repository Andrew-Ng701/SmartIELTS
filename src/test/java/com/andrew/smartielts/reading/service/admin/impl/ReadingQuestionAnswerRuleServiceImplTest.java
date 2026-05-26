package com.andrew.smartielts.reading.service.admin.impl;

import com.andrew.smartielts.common.domain.pojo.QuestionAnswerRule;
import com.andrew.smartielts.reading.mapper.ReadingQuestionAnswerRuleMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReadingQuestionAnswerRuleServiceImplTest {

    @Mock
    private ReadingQuestionAnswerRuleMapper readingQuestionAnswerRuleMapper;

    @Test
    void replaceByQuestionId_shouldSplitCommaSeparatedAcceptedAnswers() {
        ReadingQuestionAnswerRuleServiceImpl service =
                new ReadingQuestionAnswerRuleServiceImpl(readingQuestionAnswerRuleMapper);
        QuestionAnswerRule rule = new QuestionAnswerRule();
        rule.setBlankNo(5);
        rule.setAnswerText("About 4 months,4 months,4");
        rule.setIsPrimary(1);

        List<QuestionAnswerRule> saved = service.replaceByQuestionId(31L, List.of(rule));

        ArgumentCaptor<QuestionAnswerRule> captor = ArgumentCaptor.forClass(QuestionAnswerRule.class);
        verify(readingQuestionAnswerRuleMapper).deleteByQuestionId(31L);
        verify(readingQuestionAnswerRuleMapper, times(3)).insertReadingQuestionAnswerRule(captor.capture());
        assertEquals(3, saved.size());
        assertEquals("About 4 months", captor.getAllValues().get(0).getAnswerText());
        assertEquals("4 months", captor.getAllValues().get(1).getAnswerText());
        assertEquals("4", captor.getAllValues().get(2).getAnswerText());
        assertEquals(1, captor.getAllValues().get(0).getIsPrimary());
        assertEquals(0, captor.getAllValues().get(1).getIsPrimary());
        assertEquals(0, captor.getAllValues().get(2).getIsPrimary());
    }
}

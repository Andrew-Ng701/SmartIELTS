package com.andrew.smartielts.common.support;

import com.andrew.smartielts.common.domain.pojo.QuestionAnswerRule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestionAnswerRuleJudgeSupportTest {

    private final QuestionAnswerRuleJudgeSupport support = new QuestionAnswerRuleJudgeSupport();

    @Test
    void grade_shouldMatchCommaSeparatedRuleAnswersIgnoringCaseAndWhitespace() {
        QuestionAnswerRule rule = new QuestionAnswerRule();
        rule.setBlankNo(1);
        rule.setAnswerText("About 4 months,4 months,4");

        QuestionAnswerRuleJudgeSupport.GradeResult result = support.grade(
                List.of(" ABOUT   4MONTHS "),
                "TEXT",
                null,
                null,
                1,
                1,
                0,
                List.of(rule),
                1
        );

        assertTrue(result.isCorrect());
        assertEquals(1, result.getEarnedScore());
    }

    @Test
    void grade_shouldMatchCommaSeparatedAcceptedAnswersTextIgnoringCaseAndWhitespace() {
        QuestionAnswerRuleJudgeSupport.GradeResult result = support.grade(
                List.of(" 5MONTHS "),
                "TEXT",
                null,
                "5 months,5",
                1,
                1,
                0,
                List.of(),
                1
        );

        assertTrue(result.isCorrect());
        assertEquals(1, result.getEarnedScore());
    }

    @Test
    void grade_shouldMatchCommaSeparatedCorrectAnswerIgnoringCaseAndWhitespace() {
        QuestionAnswerRuleJudgeSupport.GradeResult result = support.grade(
                List.of(" jo 6337 "),
                "TEXT",
                "JO6337,J O 6337",
                null,
                1,
                1,
                0,
                List.of(),
                1
        );

        assertTrue(result.isCorrect());
        assertEquals(1, result.getEarnedScore());
    }

    @Test
    void grade_singleChoice_shouldRequireAllSelectedAnswersToMatchOneQuestion() {
        QuestionAnswerRuleJudgeSupport.GradeResult result = support.grade(
                List.of("B", "D"),
                "SINGLE",
                "B,D",
                null,
                1,
                1,
                0,
                List.of(),
                1
        );

        assertTrue(result.isCorrect());
        assertEquals(1, result.getEarnedScore());
    }

    @Test
    void grade_singleChoice_shouldNotGivePartialScoreForMissingAnswer() {
        QuestionAnswerRuleJudgeSupport.GradeResult result = support.grade(
                List.of("B"),
                "SINGLE",
                "B,D",
                null,
                1,
                1,
                0,
                List.of(),
                1
        );

        assertFalse(result.isCorrect());
        assertEquals(0, result.getEarnedScore());
    }

    @Test
    void grade_multiChoice_shouldScoreEachCorrectAnswerSeparately() {
        QuestionAnswerRuleJudgeSupport.GradeResult result = support.grade(
                List.of("B", "C", "D"),
                "MULTI",
                "B,C,E",
                null,
                1,
                1,
                0,
                List.of(),
                1
        );

        assertFalse(result.isCorrect());
        assertEquals(2, result.getEarnedScore());
    }

    @Test
    void grade_multiChoice_shouldReturnFullScoreWhenAllAnswersMatch() {
        QuestionAnswerRuleJudgeSupport.GradeResult result = support.grade(
                List.of("E", "C", "B"),
                "MULTI",
                "B,C,E",
                null,
                1,
                1,
                0,
                List.of(),
                1
        );

        assertTrue(result.isCorrect());
        assertEquals(3, result.getEarnedScore());
    }
}

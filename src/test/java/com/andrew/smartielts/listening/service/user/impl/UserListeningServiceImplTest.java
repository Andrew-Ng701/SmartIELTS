package com.andrew.smartielts.listening.service.user.impl;

import com.andrew.smartielts.common.domain.pojo.TestPartGroup;
import com.andrew.smartielts.common.image.service.BizImageResourceService;
import com.andrew.smartielts.common.support.QuestionAnswerRuleJudgeSupport;
import com.andrew.smartielts.listening.constants.ListeningAudioConstants;
import com.andrew.smartielts.listening.domain.pojo.ListeningAnswerRecord;
import com.andrew.smartielts.listening.domain.pojo.ListeningAudio;
import com.andrew.smartielts.listening.domain.pojo.ListeningQuestion;
import com.andrew.smartielts.listening.domain.pojo.ListeningRecord;
import com.andrew.smartielts.listening.domain.pojo.ListeningTest;
import com.andrew.smartielts.listening.domain.vo.ListeningRecordDetailVO;
import com.andrew.smartielts.listening.mapper.ListeningAnswerRecordMapper;
import com.andrew.smartielts.listening.mapper.ListeningQuestionMapper;
import com.andrew.smartielts.listening.mapper.ListeningRecordMapper;
import com.andrew.smartielts.listening.mapper.ListeningTestMapper;
import com.andrew.smartielts.listening.service.admin.ListeningAudioService;
import com.andrew.smartielts.listening.service.admin.ListeningPartGroupService;
import com.andrew.smartielts.listening.support.ListeningGroupAnswerRuleSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserListeningServiceImplTest {

    @Mock
    private ListeningTestMapper listeningTestMapper;

    @Mock
    private ListeningQuestionMapper listeningQuestionMapper;

    @Mock
    private ListeningRecordMapper listeningRecordMapper;

    @Mock
    private ListeningAnswerRecordMapper listeningAnswerRecordMapper;

    @Mock
    private ListeningPartGroupService listeningPartGroupService;

    @Mock
    private ListeningAudioService listeningAudioService;

    @Mock
    private BizImageResourceService bizImageResourceService;

    @Test
    void getRecord_shouldReturnAudioFieldsForReplay() {
        UserListeningServiceImpl service = newService();
        ListeningTest test = test();
        ListeningRecord record = record();
        TestPartGroup group = group();
        String formattedQuestionText = "Challenges faced by RFDS dentists\n"
                + "      need to bring equipment including (1) for records\n\n"
                + "Products supplied by RFDS dentists if necessary RFDS provides:\n"
                + "      (4) and (5) for regular use";
        ListeningQuestion question = question();
        question.setQuestionText(formattedQuestionText);
        ListeningAnswerRecord answerRecord = answerRecord();
        ListeningAudio testAudio = audio(201L, ListeningAudioConstants.AUDIO_SCOPE_TEST, null, "https://oss.test/test.mp3");
        ListeningAudio groupAudio = audio(202L, ListeningAudioConstants.AUDIO_SCOPE_PART_GROUP, 11L, "https://oss.test/group.mp3");

        when(listeningRecordMapper.findAnyByIdForUser(101L, 9L)).thenReturn(record);
        when(listeningTestMapper.findAnyById(1L)).thenReturn(test);
        when(listeningQuestionMapper.findAnyByTestId(1L)).thenReturn(List.of(question));
        when(listeningPartGroupService.listAnyByTestId(1L)).thenReturn(List.of(group));
        when(listeningAnswerRecordMapper.findByRecordId(101L)).thenReturn(List.of(answerRecord));
        when(listeningAudioService.listByTestId(1L)).thenReturn(List.of(testAudio, groupAudio));
        when(bizImageResourceService.listByTargets("LISTENING_PART_GROUP", List.of(11L))).thenReturn(Map.of());
        when(bizImageResourceService.listByTargets("LISTENING_QUESTION", List.of(31L))).thenReturn(Map.of());

        ListeningRecordDetailVO result = service.getRecord(101L, 9L);

        assertSame(testAudio, result.getTestAudio());
        assertEquals(formattedQuestionText, result.getQuestions().get(0).getQuestionText());
        assertEquals(1, result.getAllowAudioSeek());
        assertEquals(1, result.getPartGroupAudios().size());
        assertSame(groupAudio, result.getPartGroupAudios().get(0));
        assertEquals(1, result.getParts().get(0).getGroups().get(0).getAudios().size());
        assertSame(groupAudio, result.getParts().get(0).getGroups().get(0).getAudios().get(0));
    }

    private UserListeningServiceImpl newService() {
        return new UserListeningServiceImpl(
                listeningTestMapper,
                listeningQuestionMapper,
                listeningRecordMapper,
                listeningAnswerRecordMapper,
                listeningPartGroupService,
                listeningAudioService,
                new QuestionAnswerRuleJudgeSupport(),
                new ListeningGroupAnswerRuleSupport(),
                bizImageResourceService
        );
    }

    private ListeningTest test() {
        ListeningTest test = new ListeningTest();
        test.setId(1L);
        test.setTitle("Listening Test");
        test.setTotalScore(40);
        test.setAllowAudioSeek(1);
        return test;
    }

    private TestPartGroup group() {
        TestPartGroup group = new TestPartGroup();
        group.setId(11L);
        group.setTestId(1L);
        group.setPartNumber(1);
        group.setGroupNumber(1);
        group.setTitle("Section 1");
        group.setDisplayOrder(1);
        return group;
    }

    private ListeningQuestion question() {
        ListeningQuestion question = new ListeningQuestion();
        question.setId(31L);
        question.setTestId(1L);
        question.setPartGroupId(11L);
        question.setSectionNumber(1);
        question.setQuestionNumber(1);
        question.setQuestionType("SHORT_ANSWER");
        question.setAnswerMode("TEXT");
        question.setQuestionText("Question 1");
        question.setCorrectAnswer("Paris");
        question.setDisplayOrder(1);
        question.setScore(1);
        return question;
    }

    private ListeningRecord record() {
        ListeningRecord record = new ListeningRecord();
        record.setId(101L);
        record.setUserId(9L);
        record.setTestId(1L);
        record.setSessionId("listening-session");
        record.setTotalScore(1);
        record.setCreatedTime(LocalDateTime.now());
        record.setIsDeleted(0);
        return record;
    }

    private ListeningAnswerRecord answerRecord() {
        ListeningAnswerRecord answerRecord = new ListeningAnswerRecord();
        answerRecord.setQuestionId(31L);
        answerRecord.setUserAnswer("Paris");
        answerRecord.setIsCorrect(1);
        answerRecord.setScore(1);
        return answerRecord;
    }

    private ListeningAudio audio(Long id, String scope, Long groupId, String url) {
        ListeningAudio audio = new ListeningAudio();
        audio.setId(id);
        audio.setTestId(1L);
        audio.setPartGroupId(groupId);
        audio.setAudioScope(scope);
        audio.setTitle("Audio " + id);
        audio.setAudioUrl(url);
        return audio;
    }
}

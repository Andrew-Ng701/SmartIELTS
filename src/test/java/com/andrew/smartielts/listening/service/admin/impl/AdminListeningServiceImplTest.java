package com.andrew.smartielts.listening.service.admin.impl;

import com.andrew.smartielts.common.domain.pojo.TestPartGroup;
import com.andrew.smartielts.common.image.service.BizImageResourceService;
import com.andrew.smartielts.listening.domain.dto.ListeningQuestionDTO;
import com.andrew.smartielts.listening.domain.pojo.ListeningQuestion;
import com.andrew.smartielts.listening.domain.pojo.ListeningTest;
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

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminListeningServiceImplTest {

    @Mock
    private ListeningTestMapper listeningTestMapper;

    @Mock
    private ListeningQuestionMapper listeningQuestionMapper;

    @Mock
    private ListeningRecordMapper listeningRecordMapper;

    @Mock
    private ListeningAnswerRecordMapper listeningAnswerRecordMapper;

    @Mock
    private ListeningAudioService listeningAudioService;

    @Mock
    private ListeningPartGroupService listeningPartGroupService;

    @Mock
    private BizImageResourceService bizImageResourceService;

    @Test
    void createQuestion_shouldPreserveFormattedQuestionText() {
        AdminListeningServiceImpl service = service();
        ListeningTest test = new ListeningTest();
        test.setId(1L);
        TestPartGroup group = new TestPartGroup();
        group.setId(11L);
        group.setTestId(1L);
        AtomicReference<ListeningQuestion> savedQuestion = new AtomicReference<>();

        when(listeningTestMapper.findActiveById(1L)).thenReturn(test);
        when(listeningPartGroupService.getActiveById(11L)).thenReturn(group);
        doAnswer(invocation -> {
            ListeningQuestion question = invocation.getArgument(0);
            savedQuestion.set(question);
            return null;
        }).when(listeningQuestionMapper).insertListeningQuestion(any(ListeningQuestion.class));

        String formattedQuestionText = "Challenges faced by RFDS dentists\n"
                + "      need to bring equipment including (1) for records\n\n"
                + "Products supplied by RFDS dentists if necessary RFDS provides:\n"
                + "      (4) and (5) for regular use\n";
        ListeningQuestionDTO dto = new ListeningQuestionDTO();
        dto.setPartGroupId(11L);
        dto.setQuestionNumber(1);
        dto.setQuestionType("NOTE_COMPLETION");
        dto.setQuestionText(formattedQuestionText);

        service.createQuestion(1L, dto);

        assertEquals(formattedQuestionText, savedQuestion.get().getQuestionText());
    }

    private AdminListeningServiceImpl service() {
        return new AdminListeningServiceImpl(
                listeningTestMapper,
                listeningQuestionMapper,
                listeningRecordMapper,
                listeningAnswerRecordMapper,
                listeningAudioService,
                listeningPartGroupService,
                new ListeningGroupAnswerRuleSupport(),
                bizImageResourceService
        );
    }
}

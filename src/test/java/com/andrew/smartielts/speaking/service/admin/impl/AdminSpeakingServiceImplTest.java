package com.andrew.smartielts.speaking.service.admin.impl;

import com.andrew.smartielts.speaking.domain.pojo.SpeakingQuestion;
import com.andrew.smartielts.speaking.domain.pojo.SpeakingRecord;
import com.andrew.smartielts.speaking.domain.vo.SpeakingRecordDetailVO;
import com.andrew.smartielts.speaking.mapper.SpeakingMapper;
import com.andrew.smartielts.speaking.mapper.SpeakingRecordMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminSpeakingServiceImplTest {

    @Mock
    private SpeakingMapper speakingMapper;

    @Mock
    private SpeakingRecordMapper speakingRecordMapper;

    @Test
    void getSpeakingQuestion_shouldReturnQuestionIncludingDeleted() {
        AdminSpeakingServiceImpl service = new AdminSpeakingServiceImpl(speakingMapper, speakingRecordMapper);
        SpeakingQuestion question = question(101L, "PART1", "Do you enjoy reading?");
        question.setActive(0);
        question.setIsDeleted(1);
        when(speakingMapper.findAnyById(101L)).thenReturn(question);

        SpeakingQuestion result = service.getSpeakingQuestion(101L);

        assertEquals(101L, result.getId());
        assertEquals(0, result.getActive());
        assertEquals(1, result.getIsDeleted());
    }

    @Test
    void getSpeakingQuestion_whenMissing_shouldThrow() {
        AdminSpeakingServiceImpl service = new AdminSpeakingServiceImpl(speakingMapper, speakingRecordMapper);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.getSpeakingQuestion(999L));

        assertEquals("Speaking question not found", ex.getMessage());
    }

    @Test
    void getRecord_shouldIncludeRecordsFromSameSession() {
        AdminSpeakingServiceImpl service = new AdminSpeakingServiceImpl(speakingMapper, speakingRecordMapper);
        SpeakingRecord current = record(11L, 101L, "https://oss.test/speaking/11.mp3");
        SpeakingRecord other = record(12L, 102L, "https://oss.test/speaking/12.mp3");
        SpeakingQuestion q1 = question(101L, "PART1", "Do you enjoy reading?");
        SpeakingQuestion q2 = question(102L, "PART3", "How does technology affect communication?");
        when(speakingRecordMapper.findAnyById(11L)).thenReturn(current);
        when(speakingRecordMapper.findBySessionId("sess-000001")).thenReturn(List.of(current, other));
        when(speakingMapper.findAnyById(101L)).thenReturn(q1);
        when(speakingMapper.findAnyById(102L)).thenReturn(q2);

        SpeakingRecordDetailVO result = service.getRecord(11L);

        assertEquals(11L, result.getRecordId());
        assertEquals("sess-000001", result.getSessionId());
        assertEquals(2, result.getSessionRecords().size());
        assertEquals(11L, result.getSessionRecords().get(0).getId());
        assertEquals("https://oss.test/speaking/12.mp3", result.getSessionRecords().get(1).getAudioUrl());
        assertEquals("How does technology affect communication?", result.getSessionRecords().get(1).getQuestionText());
    }

    private SpeakingRecord record(Long id, Long questionId, String audioUrl) {
        SpeakingRecord record = new SpeakingRecord();
        record.setId(id);
        record.setSessionId("sess-000001");
        record.setQuestionId(questionId);
        record.setAudioUrl(audioUrl);
        record.setTranscript("Answer " + id);
        record.setOverallScore(new BigDecimal("6.5"));
        record.setAnswerStatus("SCORED");
        return record;
    }

    private SpeakingQuestion question(Long id, String part, String text) {
        SpeakingQuestion question = new SpeakingQuestion();
        question.setId(id);
        question.setPart(part);
        question.setQuestionText(text);
        return question;
    }
}

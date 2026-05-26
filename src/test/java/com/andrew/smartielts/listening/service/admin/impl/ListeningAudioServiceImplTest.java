package com.andrew.smartielts.listening.service.admin.impl;

import com.andrew.smartielts.common.storage.BucketType;
import com.andrew.smartielts.common.storage.UploadResult;
import com.andrew.smartielts.common.storage.service.OssStorageService;
import com.andrew.smartielts.listening.ai.service.ListeningTranscriptService;
import com.andrew.smartielts.listening.constants.ListeningAudioConstants;
import com.andrew.smartielts.listening.domain.pojo.ListeningAudio;
import com.andrew.smartielts.listening.domain.pojo.ListeningTest;
import com.andrew.smartielts.listening.mapper.ListeningAudioMapper;
import com.andrew.smartielts.listening.mapper.ListeningPartGroupMapper;
import com.andrew.smartielts.listening.mapper.ListeningTestMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListeningAudioServiceImplTest {

    @Mock
    private ListeningAudioMapper listeningAudioMapper;

    @Mock
    private ListeningTestMapper listeningTestMapper;

    @Mock
    private ListeningPartGroupMapper listeningPartGroupMapper;

    @Mock
    private ListeningTranscriptService listeningTranscriptService;

    @Mock
    private OssStorageService ossStorageService;

    @Test
    void createTestAudioFromUpload_whenManualTranscriptProvided_shouldUseManualTranscript() {
        ListeningAudioServiceImpl service = service();
        ListeningTest test = new ListeningTest();
        test.setId(1L);
        when(listeningTestMapper.findActiveById(1L)).thenReturn(test);
        when(ossStorageService.upload(any(), eq(BucketType.LISTENING_AUDIO), eq(ListeningAudioConstants.BIZ_PATH_LISTENING_AUDIO)))
                .thenReturn(new UploadResult("https://oss.example/audio.mp3", "audio-key"));

        service.createTestAudioFromUpload(1L, "Tape A", " Manual transcript ", mp3());

        ArgumentCaptor<ListeningAudio> captor = ArgumentCaptor.forClass(ListeningAudio.class);
        verify(listeningAudioMapper).insertListeningAudio(captor.capture());
        ListeningAudio saved = captor.getValue();
        assertEquals(ListeningAudioConstants.AUDIO_SCOPE_TEST, saved.getAudioScope());
        assertEquals("Tape A", saved.getTitle());
        assertEquals("Manual transcript", saved.getTranscriptText());
        verify(listeningTranscriptService, never()).generateTranscript(any());
    }

    @Test
    void createTestAudioFromUpload_whenManualTranscriptMissing_shouldPersistAudioBeforeGeneratedTranscript() {
        ListeningAudioServiceImpl service = service();
        ListeningTest test = new ListeningTest();
        test.setId(1L);
        when(listeningTestMapper.findActiveById(1L)).thenReturn(test);
        when(ossStorageService.upload(any(), eq(BucketType.LISTENING_AUDIO), eq(ListeningAudioConstants.BIZ_PATH_LISTENING_AUDIO)))
                .thenReturn(new UploadResult("https://oss.example/audio.mp3", "audio-key"));

        service.createTestAudioFromUpload(1L, "Tape A", null, mp3());

        ArgumentCaptor<ListeningAudio> captor = ArgumentCaptor.forClass(ListeningAudio.class);
        verify(listeningAudioMapper).insertListeningAudio(captor.capture());
        assertNull(captor.getValue().getTranscriptText());
    }

    private ListeningAudioServiceImpl service() {
        return new ListeningAudioServiceImpl(
                listeningAudioMapper,
                listeningTestMapper,
                listeningPartGroupMapper,
                listeningTranscriptService,
                ossStorageService
        );
    }

    private MockMultipartFile mp3() {
        return new MockMultipartFile("file", "sample.mp3", "audio/mpeg", new byte[]{1, 2, 3});
    }
}

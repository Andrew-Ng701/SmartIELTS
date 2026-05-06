package com.andrew.smartielts.listening.service.admin.impl;

import com.andrew.smartielts.common.domain.pojo.TestPartGroup;
import com.andrew.smartielts.common.storage.BucketType;
import com.andrew.smartielts.common.storage.service.OssStorageService;
import com.andrew.smartielts.common.storage.UploadResult;
import com.andrew.smartielts.listening.ai.service.ListeningTranscriptService;
import com.andrew.smartielts.listening.constants.ListeningAudioConstants;
import com.andrew.smartielts.listening.constants.ListeningConstants;
import com.andrew.smartielts.listening.domain.pojo.ListeningAudio;
import com.andrew.smartielts.listening.domain.pojo.ListeningTest;
import com.andrew.smartielts.listening.mapper.ListeningAudioMapper;
import com.andrew.smartielts.listening.mapper.ListeningPartGroupMapper;
import com.andrew.smartielts.listening.mapper.ListeningTestMapper;
import com.andrew.smartielts.listening.service.admin.ListeningAudioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class ListeningAudioServiceImpl implements ListeningAudioService {

    private final ListeningAudioMapper listeningAudioMapper;
    private final ListeningTestMapper listeningTestMapper;
    private final ListeningPartGroupMapper listeningPartGroupMapper;
    private final ListeningTranscriptService listeningTranscriptService;
    private final OssStorageService ossStorageService;

    public ListeningAudioServiceImpl(
            ListeningAudioMapper listeningAudioMapper,
            ListeningTestMapper listeningTestMapper,
            ListeningPartGroupMapper listeningPartGroupMapper,
            ListeningTranscriptService listeningTranscriptService,
            OssStorageService ossStorageService) {
        this.listeningAudioMapper = listeningAudioMapper;
        this.listeningTestMapper = listeningTestMapper;
        this.listeningPartGroupMapper = listeningPartGroupMapper;
        this.listeningTranscriptService = listeningTranscriptService;
        this.ossStorageService = ossStorageService;
    }

    @Override
    @Transactional
    public ListeningAudio createTestAudioFromUpload(Long testId, String title, MultipartFile file) {
        requireActiveTest(testId);
        validateUploadFile(file);

        UploadResult uploaded = uploadAudio(file);

        ListeningAudio audio = new ListeningAudio();
        audio.setTestId(testId);
        audio.setPartGroupId(null);
        audio.setAudioScope(ListeningAudioConstants.AUDIO_SCOPE_TEST);
        audio.setTitle(trimToNull(title));
        audio.setAudioUrl(uploaded.getFileUrl());
        audio.setAudioObjectKey(uploaded.getFileKey());
        audio.setTranscriptText(resolveTranscript(uploaded.getFileUrl()));
        audio.setIsDeleted(ListeningConstants.NOT_DELETED);
        audio.setCreatedTime(LocalDateTime.now());
        audio.setUpdatedTime(LocalDateTime.now());

        listeningAudioMapper.insertListeningAudio(audio);
        return audio;
    }

    @Override
    @Transactional
    public ListeningAudio updateTestAudioFromUpload(Long audioId, Long testId, String title, MultipartFile file) {
        ListeningAudio existing = requireAudio(audioId);
        requireActiveTest(testId);
        if (existing.getPartGroupId() != null || !Objects.equals(existing.getTestId(), testId)) {
            throw new RuntimeException("listening_test_audio_not_found");
        }
        validateUploadFile(file);

        UploadResult uploaded = uploadAudio(file);
        String oldObjectKey = existing.getAudioObjectKey();

        existing.setTestId(testId);
        existing.setPartGroupId(null);
        existing.setAudioScope(ListeningAudioConstants.AUDIO_SCOPE_TEST);
        existing.setTitle(trimToNull(title));
        existing.setAudioUrl(uploaded.getFileUrl());
        existing.setAudioObjectKey(uploaded.getFileKey());
        existing.setTranscriptText(resolveTranscript(uploaded.getFileUrl()));
        existing.setUpdatedTime(LocalDateTime.now());

        listeningAudioMapper.updateListeningAudio(existing);
        deleteListeningAudioObjectQuietly(oldObjectKey);
        return existing;
    }

    @Override
    @Transactional
    public ListeningAudio createPartGroupAudioFromUpload(Long testId, Long partGroupId, String title, MultipartFile file) {
        requireActiveTest(testId);
        requireActivePartGroup(testId, partGroupId);
        validateUploadFile(file);

        UploadResult uploaded = uploadAudio(file);

        ListeningAudio audio = new ListeningAudio();
        audio.setTestId(testId);
        audio.setPartGroupId(partGroupId);
        audio.setAudioScope(ListeningAudioConstants.AUDIO_SCOPE_PART_GROUP);
        audio.setTitle(trimToNull(title));
        audio.setAudioUrl(uploaded.getFileUrl());
        audio.setAudioObjectKey(uploaded.getFileKey());
        audio.setTranscriptText(resolveTranscript(uploaded.getFileUrl()));
        audio.setIsDeleted(ListeningConstants.NOT_DELETED);
        audio.setCreatedTime(LocalDateTime.now());
        audio.setUpdatedTime(LocalDateTime.now());

        listeningAudioMapper.insertListeningAudio(audio);
        return audio;
    }

    @Override
    @Transactional
    public ListeningAudio updatePartGroupAudioFromUpload(Long audioId, Long testId, Long partGroupId, String title, MultipartFile file) {
        ListeningAudio existing = requireAudio(audioId);
        requireActiveTest(testId);
        requireActivePartGroup(testId, partGroupId);
        if (!Objects.equals(existing.getTestId(), testId) || !Objects.equals(existing.getPartGroupId(), partGroupId)) {
            throw new RuntimeException("listening_part_group_audio_not_found");
        }
        validateUploadFile(file);

        UploadResult uploaded = uploadAudio(file);
        String oldObjectKey = existing.getAudioObjectKey();

        existing.setTestId(testId);
        existing.setPartGroupId(partGroupId);
        existing.setAudioScope(ListeningAudioConstants.AUDIO_SCOPE_PART_GROUP);
        existing.setTitle(trimToNull(title));
        existing.setAudioUrl(uploaded.getFileUrl());
        existing.setAudioObjectKey(uploaded.getFileKey());
        existing.setTranscriptText(resolveTranscript(uploaded.getFileUrl()));
        existing.setUpdatedTime(LocalDateTime.now());

        listeningAudioMapper.updateListeningAudio(existing);
        deleteListeningAudioObjectQuietly(oldObjectKey);
        return existing;
    }

    @Override
    public ListeningAudio getById(Long id) {
        return id == null ? null : listeningAudioMapper.findById(id);
    }

    @Override
    public ListeningAudio getTestAudioByTestId(Long testId) {
        return testId == null ? null : listeningAudioMapper.findTestAudioByTestId(testId);
    }

    @Override
    public List<ListeningAudio> listByTestId(Long testId) {
        List<ListeningAudio> list = testId == null ? null : listeningAudioMapper.findByTestId(testId);
        return list == null ? new ArrayList<>() : list;
    }

    @Override
    public List<ListeningAudio> listByPartGroupId(Long partGroupId) {
        List<ListeningAudio> list = partGroupId == null ? null : listeningAudioMapper.findByPartGroupId(partGroupId);
        return list == null ? new ArrayList<>() : list;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        ListeningAudio existing = getById(id);
        listeningAudioMapper.deleteById(id);
        if (existing != null) {
            deleteListeningAudioObjectQuietly(existing.getAudioObjectKey());
        }
    }

    @Override
    @Transactional
    public void deleteByTestId(Long testId) {
        List<ListeningAudio> existing = listByTestId(testId);
        listeningAudioMapper.deleteByTestId(testId);
        deleteListeningAudioObjectsQuietly(existing);
    }

    @Override
    @Transactional
    public void deleteByPartGroupId(Long partGroupId) {
        List<ListeningAudio> existing = listByPartGroupId(partGroupId);
        listeningAudioMapper.deleteByPartGroupId(partGroupId);
        deleteListeningAudioObjectsQuietly(existing);
    }

    private UploadResult uploadAudio(MultipartFile file) {
        return ossStorageService.upload(
                file,
                BucketType.LISTENING_AUDIO,
                ListeningAudioConstants.BIZ_PATH_LISTENING_AUDIO
        );
    }

    private String resolveTranscript(String audioUrl) {
        return listeningTranscriptService.generateTranscript(audioUrl);
    }

    private void deleteListeningAudioObjectsQuietly(List<ListeningAudio> audios) {
        if (audios == null || audios.isEmpty()) {
            return;
        }
        for (ListeningAudio audio : audios) {
            if (audio != null) {
                deleteListeningAudioObjectQuietly(audio.getAudioObjectKey());
            }
        }
    }

    private void deleteListeningAudioObjectQuietly(String objectKey) {
        String trimmedObjectKey = trimToNull(objectKey);
        if (trimmedObjectKey == null) {
            return;
        }
        try {
            ossStorageService.delete(BucketType.LISTENING_AUDIO, trimmedObjectKey);
        } catch (Exception e) {
            log.warn("Failed to delete listening audio object, objectKey={}", trimmedObjectKey, e);
        }
    }

    private ListeningTest requireActiveTest(Long testId) {
        if (testId == null) {
            throw new RuntimeException("test_id_is_required");
        }
        ListeningTest test = listeningTestMapper.findActiveById(testId);
        if (test == null) {
            throw new RuntimeException("listening_test_not_found");
        }
        return test;
    }

    private TestPartGroup requireActivePartGroup(Long testId, Long partGroupId) {
        if (partGroupId == null) {
            throw new RuntimeException("part_group_id_is_required");
        }
        TestPartGroup partGroup = listeningPartGroupMapper.findActiveById(partGroupId);
        if (partGroup == null) {
            throw new RuntimeException("listening_part_group_not_found");
        }
        if (!Objects.equals(partGroup.getTestId(), testId)) {
            throw new RuntimeException("part_group_does_not_belong_to_test");
        }
        return partGroup;
    }

    private ListeningAudio requireAudio(Long audioId) {
        ListeningAudio audio = getById(audioId);
        if (audio == null) {
            throw new RuntimeException("listening_audio_not_found");
        }
        return audio;
    }

    private void validateUploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("audio_file_is_required");
        }
        String originalFilename = trimToNull(file.getOriginalFilename());
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".mp3")) {
            throw new RuntimeException("only_mp3_file_is_supported");
        }
        String contentType = trimToNull(file.getContentType());
        if (contentType != null && !contentType.equalsIgnoreCase("audio/mpeg") && !contentType.equalsIgnoreCase("audio/mp3")) {
            throw new RuntimeException("invalid_audio_content_type");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

package com.andrew.smartielts.listening.service.admin.impl;

import com.andrew.smartielts.common.domain.pojo.TestTimerConfig;
import com.andrew.smartielts.listening.mapper.ListeningTestTimerMapper;
import com.andrew.smartielts.listening.service.admin.ListeningTestTimerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ListeningTestTimerServiceImpl implements ListeningTestTimerService {

    private final ListeningTestTimerMapper listeningTestTimerMapper;

    public ListeningTestTimerServiceImpl(ListeningTestTimerMapper listeningTestTimerMapper) {
        this.listeningTestTimerMapper = listeningTestTimerMapper;
    }

    @Override
    @Transactional
    public TestTimerConfig saveOrUpdate(TestTimerConfig timerConfig) {
        if (timerConfig == null) {
            throw new RuntimeException("Timer config is required");
        }
        if (timerConfig.getTestId() == null) {
            throw new RuntimeException("Test id is required");
        }

        TestTimerConfig existing = listeningTestTimerMapper.findByTestId(timerConfig.getTestId());
        LocalDateTime now = LocalDateTime.now();

        if (existing == null) {
            timerConfig.setTimerMode(defaultTimerMode(timerConfig.getTimerMode()));
            timerConfig.setAutoSubmit(defaultFlag(timerConfig.getAutoSubmit(), 1));
            timerConfig.setAllowPause(defaultFlag(timerConfig.getAllowPause(), 0));
            timerConfig.setCreatedTime(now);
            timerConfig.setUpdatedTime(now);
            listeningTestTimerMapper.insertListeningTestTimer(timerConfig);
            return timerConfig;
        }

        existing.setTimerMode(defaultTimerMode(timerConfig.getTimerMode()));
        existing.setTotalSeconds(timerConfig.getTotalSeconds());
        existing.setAutoSubmit(defaultFlag(timerConfig.getAutoSubmit(), existing.getAutoSubmit() == null ? 1 : existing.getAutoSubmit()));
        existing.setAllowPause(defaultFlag(timerConfig.getAllowPause(), existing.getAllowPause() == null ? 0 : existing.getAllowPause()));
        existing.setUpdatedTime(now);
        listeningTestTimerMapper.updateListeningTestTimer(existing);
        return listeningTestTimerMapper.findByTestId(existing.getTestId());
    }

    @Override
    public TestTimerConfig getByTestId(Long testId) {
        if (testId == null) {
            throw new RuntimeException("Test id is required");
        }
        return listeningTestTimerMapper.findByTestId(testId);
    }

    @Override
    @Transactional
    public void deleteByTestId(Long testId) {
        if (testId == null) {
            throw new RuntimeException("Test id is required");
        }
        listeningTestTimerMapper.deleteByTestId(testId);
    }

    private String defaultTimerMode(String timerMode) {
        return timerMode == null || timerMode.isBlank() ? "NONE" : timerMode.trim();
    }

    private Integer defaultFlag(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
    }
}
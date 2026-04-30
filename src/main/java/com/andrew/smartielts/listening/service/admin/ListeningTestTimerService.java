package com.andrew.smartielts.listening.service.admin;

import com.andrew.smartielts.common.domain.pojo.TestTimerConfig;

public interface ListeningTestTimerService {

    TestTimerConfig saveOrUpdate(TestTimerConfig timerConfig);

    TestTimerConfig getByTestId(Long testId);

    void deleteByTestId(Long testId);
}
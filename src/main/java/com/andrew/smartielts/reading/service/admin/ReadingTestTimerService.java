package com.andrew.smartielts.reading.service.admin;

import com.andrew.smartielts.common.domain.pojo.TestTimerConfig;

public interface ReadingTestTimerService {

    TestTimerConfig getByTestId(Long testId);

    void saveOrUpdateTestTimerConfig(TestTimerConfig config);

    void deleteByTestId(Long testId);
}
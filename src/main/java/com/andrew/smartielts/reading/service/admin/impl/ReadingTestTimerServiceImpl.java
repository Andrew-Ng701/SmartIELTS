package com.andrew.smartielts.reading.service.admin.impl;

import com.andrew.smartielts.common.domain.pojo.TestTimerConfig;
import com.andrew.smartielts.reading.mapper.ReadingTestTimerMapper;
import com.andrew.smartielts.reading.service.admin.ReadingTestTimerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReadingTestTimerServiceImpl implements ReadingTestTimerService {

    private final ReadingTestTimerMapper readingTestTimerMapper;

    @Override
    public TestTimerConfig getByTestId(Long testId) {
        if (testId == null) {
            return null;
        }
        return readingTestTimerMapper.findByTestId(testId);
    }

    @Override
    public void saveOrUpdateTestTimerConfig(TestTimerConfig config) {
        if (config == null || config.getTestId() == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        TestTimerConfig existing = readingTestTimerMapper.findByTestId(config.getTestId());

        if (existing == null) {
            if (config.getCreatedTime() == null) {
                config.setCreatedTime(now);
            }
            config.setUpdatedTime(now);
            readingTestTimerMapper.insertReadingTestTimer(config);
            return;
        }

        config.setId(existing.getId());
        if (config.getCreatedTime() == null) {
            config.setCreatedTime(existing.getCreatedTime());
        }
        config.setUpdatedTime(now);
        readingTestTimerMapper.updateReadingTestTimer(config);
    }

    @Override
    public void deleteByTestId(Long testId) {
        if (testId == null) {
            return;
        }
        readingTestTimerMapper.deleteByTestId(testId);
    }
}
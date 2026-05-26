package com.andrew.smartielts.dashboard.service.impl;

import com.andrew.smartielts.dashboard.service.DashboardExecutiveSummaryCacheService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardExecutiveSummaryCacheServiceImpl implements DashboardExecutiveSummaryCacheService {

    private static final String REDIS_KEY_PREFIX = "smartielts:dashboard:executive-summary:";
    private static final long MIN_CACHE_TTL_MILLIS = 30_000L;

    @Qualifier("dashboardRedisTemplate")
    private final RedisTemplate<String, Object> dashboardRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public <T> T get(String key, Class<T> valueType) {
        if (key == null || key.isBlank() || valueType == null) {
            return null;
        }
        try {
            Object value = dashboardRedisTemplate.opsForValue().get(REDIS_KEY_PREFIX + key);
            if (!(value instanceof String json) || json.isBlank()) {
                return null;
            }
            return objectMapper.readValue(json, valueType);
        } catch (Exception e) {
            log.warn("Dashboard executive summary cache read failed, key={}, reason={}", key, e.getMessage());
            return null;
        }
    }

    @Override
    public void put(String key, Object value, long ttlMillis) {
        if (key == null || key.isBlank() || value == null) {
            return;
        }
        try {
            dashboardRedisTemplate.opsForValue().set(
                    REDIS_KEY_PREFIX + key,
                    objectMapper.writeValueAsString(value),
                    Duration.ofMillis(Math.max(ttlMillis, MIN_CACHE_TTL_MILLIS))
            );
        } catch (JsonProcessingException e) {
            log.warn("Dashboard executive summary cache serialize failed, key={}, reason={}", key, e.getMessage());
        } catch (Exception e) {
            log.warn("Dashboard executive summary cache write failed, key={}, reason={}", key, e.getMessage());
        }
    }
}

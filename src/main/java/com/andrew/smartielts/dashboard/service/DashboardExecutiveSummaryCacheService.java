package com.andrew.smartielts.dashboard.service;

public interface DashboardExecutiveSummaryCacheService {

    <T> T get(String key, Class<T> valueType);

    void put(String key, Object value, long ttlMillis);
}

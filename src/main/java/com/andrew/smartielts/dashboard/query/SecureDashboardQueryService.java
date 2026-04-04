package com.andrew.smartielts.dashboard.query;

import java.util.List;
import java.util.Map;

public interface SecureDashboardQueryService {

    List<Map<String, Object>> execute(SecureDashboardQueryRequest request);
}
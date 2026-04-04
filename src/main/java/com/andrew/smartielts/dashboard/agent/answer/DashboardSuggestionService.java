package com.andrew.smartielts.dashboard.agent.answer;

import com.andrew.smartielts.dashboard.agent.intent.dto.DashboardIntentParseResult;

import java.util.List;
import java.util.Map;

public interface DashboardSuggestionService {

    List<String> buildSuggestions(String role,
                                  String originalQuery,
                                  String answer,
                                  String capability,
                                  DashboardIntentParseResult intent,
                                  Map<String, Object> filters,
                                  Object data);
}
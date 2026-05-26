package com.andrew.smartielts.dashboard.agent.answer;

import java.util.List;
import java.util.regex.Pattern;

public final class DashboardSuggestionPerspectiveNormalizer {

    private static final Pattern YOUR_PATTERN = Pattern.compile("\\byour\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern YOURS_PATTERN = Pattern.compile("\\byours\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern YOU_ARE_PATTERN = Pattern.compile("\\byou are\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern YOU_PATTERN = Pattern.compile("\\byou\\b", Pattern.CASE_INSENSITIVE);

    private DashboardSuggestionPerspectiveNormalizer() {
    }

    public static List<String> normalize(List<String> suggestions) {
        if (suggestions == null || suggestions.isEmpty()) {
            return List.of();
        }
        return suggestions.stream()
                .filter(it -> it != null && !it.isBlank())
                .map(DashboardSuggestionPerspectiveNormalizer::normalizeOne)
                .filter(it -> it != null && !it.isBlank())
                .limit(3)
                .toList();
    }

    private static String normalizeOne(String suggestion) {
        String result = suggestion.trim();
        if (containsHan(result)) {
            return result;
        }
        result = YOU_ARE_PATTERN.matcher(result).replaceAll("I am");
        result = YOURS_PATTERN.matcher(result).replaceAll("mine");
        result = YOUR_PATTERN.matcher(result).replaceAll("my");
        result = YOU_PATTERN.matcher(result).replaceAll("I");
        return result;
    }

    private static boolean containsHan(String text) {
        for (char ch : text.toCharArray()) {
            if (Character.UnicodeScript.of(ch) == Character.UnicodeScript.HAN) {
                return true;
            }
        }
        return false;
    }
}

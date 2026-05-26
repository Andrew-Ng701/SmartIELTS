package com.andrew.smartielts.record.constants;

import java.util.Locale;
import java.util.Set;

public final class UserRecordListSortConstants {

    public static final String UPDATED_DESC = "UPDATED_DESC";
    public static final String UPDATED_ASC = "UPDATED_ASC";
    public static final String NAME_ASC = "NAME_ASC";
    public static final String NAME_DESC = "NAME_DESC";
    public static final String MODULE_ASC = "MODULE_ASC";
    public static final String STATUS_ASC = "STATUS_ASC";
    public static final String SCORE_DESC = "SCORE_DESC";
    public static final String SCORE_ASC = "SCORE_ASC";

    private static final Set<String> SUPPORTED_SORTS = Set.of(
            UPDATED_DESC,
            UPDATED_ASC,
            NAME_ASC,
            NAME_DESC,
            MODULE_ASC,
            STATUS_ASC,
            SCORE_DESC,
            SCORE_ASC
    );

    private UserRecordListSortConstants() {
    }

    public static String normalize(String sort) {
        if (sort == null || sort.isBlank()) {
            return UPDATED_DESC;
        }
        String normalized = sort.trim().toUpperCase(Locale.ROOT);
        if (!SUPPORTED_SORTS.contains(normalized)) {
            throw new IllegalArgumentException("Unsupported sort: " + sort);
        }
        return normalized;
    }
}

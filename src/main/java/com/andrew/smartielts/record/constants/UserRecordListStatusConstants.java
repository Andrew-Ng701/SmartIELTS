package com.andrew.smartielts.record.constants;

import java.util.Locale;
import java.util.Set;

public final class UserRecordListStatusConstants {

    public static final String COMPLETED = "COMPLETED";
    public static final String DELETED = "DELETED";
    public static final String PENDING = "PENDING";
    public static final String SUCCESS = "SUCCESS";
    public static final String FAILED = "FAILED";
    public static final String RECEIVED = "RECEIVED";
    public static final String PROCESSING = "PROCESSING";
    public static final String SCORED = "SCORED";
    public static final String IN_PROGRESS = "IN_PROGRESS";
    public static final String PAUSED = "PAUSED";
    public static final String SUBMITTED = "SUBMITTED";
    public static final String AUTO_SUBMITTED = "AUTO_SUBMITTED";

    private static final Set<String> SUPPORTED_STATUSES = Set.of(
            COMPLETED,
            DELETED,
            PENDING,
            SUCCESS,
            FAILED,
            RECEIVED,
            PROCESSING,
            SCORED,
            IN_PROGRESS,
            PAUSED,
            SUBMITTED,
            AUTO_SUBMITTED
    );

    private UserRecordListStatusConstants() {
    }

    public static String normalize(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        if (!SUPPORTED_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("Unsupported status: " + status);
        }
        return normalized;
    }
}

package com.andrew.smartielts.dashboard.query;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class DashboardQueryPermissionGuard {

    public void validate(SecureDashboardQueryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Query request cannot be null");
        }
        if (request.getRole() == null || request.getRole().isBlank()) {
            throw new IllegalArgumentException("Role is required");
        }

        if ("USER".equalsIgnoreCase(request.getRole())) {
            validateUserScope(request);
            return;
        }

        if ("ADMIN".equalsIgnoreCase(request.getRole())) {
            validateAdminScope(request);
            return;
        }

        throw new AccessDeniedException("Unsupported role");
    }

    private void validateUserScope(SecureDashboardQueryRequest request) {
        if (request.getTargetUserId() == null) {
            throw new AccessDeniedException("User query must specify targetUserId");
        }
        if (!request.getTargetUserId().equals(request.getOperatorUserId())) {
            throw new AccessDeniedException("User can only query self dashboard data");
        }
    }

    private void validateAdminScope(SecureDashboardQueryRequest request) {
        if (!request.isAiGenerated() && request.getTemplateCode() == null) {
            throw new AccessDeniedException("Missing query template");
        }
    }
}
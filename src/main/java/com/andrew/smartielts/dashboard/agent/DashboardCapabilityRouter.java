package com.andrew.smartielts.dashboard.agent;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DashboardCapabilityRouter {

    private final List<DashboardCapabilityHandler> handlers;

    public Object route(DashboardCapability capability, DashboardAgentContext context) {
        return handlers.stream()
                .filter(handler -> handler.support() == capability)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No handler found for capability: " + capability))
                .handle(context);
    }
}
package com.andrew.smartielts.dashboard.agent;

public interface DashboardCapabilityHandler {

    DashboardCapability support();

    Object handle(DashboardAgentContext context);
}
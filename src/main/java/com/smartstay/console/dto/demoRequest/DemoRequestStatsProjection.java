package com.smartstay.console.dto.demoRequest;

public interface DemoRequestStatsProjection {
    long getTotalLeads();
    long getTodayNewCount();
    long getNewCount();
    long getAssignedCount();
    long getContactedCount();
    long getDemoScheduledCount();
    long getDemoCompletedCount();
    long getTrialStartedCount();
    long getConvertedCount();
    long getDroppedCount();
}

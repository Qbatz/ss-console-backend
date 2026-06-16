package com.smartstay.console.dto.supportTicket;

public interface SupportTicketStatsProjection {
    long getTotalLeads();
    long getTodayNewCount();
    long getWaitingCount();
    long getAssignedCount();
    long getInProgressCount();
    long getResolvedCount();
    long getClosedCount();
}

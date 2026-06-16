package com.smartstay.console.responses.hostels;

import java.util.List;

public record Hostels(long totalHostels,
                      long activeHostels,
                      long inactiveHostels,
                      long usedTodayCount,
                      long used2To7DaysCount,
                      long used8To14DaysCount,
                      long used15To30DaysCount,
                      long used30DaysAgoCount,
                      long neverUsedCount,
                      long trialExpiringCount,
                      int currentPage,
                      int sizePerPage,
                      int totalPages,
                      long totalItems,
                      List<HostelFilterOptionsRes> hostelFilterOptions,
                      List<HostelList> hostels) {
}

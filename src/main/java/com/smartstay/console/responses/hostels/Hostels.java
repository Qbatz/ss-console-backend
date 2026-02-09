package com.smartstay.console.responses.hostels;

import java.util.List;

public record Hostels(long totalHostels,
                      long activeHostels,
                      long inactiveHostels,
                      int currentPage,
                      int sizePerPage,
                      List<HostelList> hostels) {
}

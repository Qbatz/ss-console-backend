package com.smartstay.console.responses.hostels;

import java.util.List;

public record HostelsGraphWrapperRes(HostelGraphCardDataRes hostelGraphCardData,
                                     List<HostelOnboardedDataRes> hostelOnboardedData,
                                     List<HostelGraphRegionDataRes> hostelGraphRegionData,
                                     List<HostelOnboardedDateFilterRes> onboardedDateFilters) {
}

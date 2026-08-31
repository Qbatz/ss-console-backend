package com.smartstay.console.responses.beds;

import java.util.List;

public record HostelsBedInfoRes(String hostelId,
                                String hostelName,
                                String mainImage,
                                String initials,
                                String mobile,
                                String emailId,
                                String fullAddress,
                                List<FloorsResponse> floors) {
}

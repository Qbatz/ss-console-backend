package com.smartstay.console.dto.demoRequest;

import java.util.Date;
import java.util.List;

public record DemoRequestSnapshot(Long requestId,
                                  String name,
                                  String emailId,
                                  String contactNo,
                                  String countryCode,
                                  String organization,
                                  Integer noOfHostels,
                                  Integer noOfTenant,
                                  String city,
                                  String state,
                                  String country,
                                  String demoRequestStatus,
                                  Boolean isDemoCompleted,
                                  Boolean isAssigned,
                                  String assignedTo,
                                  String assignedBy,
                                  String presentedBy,
                                  String comments,
                                  Date bookedFor,
                                  String requestedDate,
                                  String requestedTime,
                                  Date presentedAt,
                                  List<DemoRequestCommentsSnapshot> demoRequestComments) {
}

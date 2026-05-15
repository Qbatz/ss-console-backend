package com.smartstay.console.responses.demoRequest;

import java.util.List;

public record DemoRequestResponse(Long requestId,
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
                                  boolean canAssignStaff,
                                  Boolean isDemoCompleted,
                                  Boolean isAssigned,
                                  String assignedTo,
                                  String assignedBy,
                                  String presentedBy,
                                  String comments,
                                  String requestedDate,
                                  String requestedTime,
                                  String presentedAtDate,
                                  String presentedAtTime,
                                  List<DemoRequestCommentsResponse> demoRequestComments) {
}

package com.smartstay.console.responses.hostels;

public record OwnerHostelResponse(String hostelId,
                                  String hostelType,
                                  String hostelName,
                                  String initials,
                                  String mobile,
                                  String houseNo,
                                  String street,
                                  String landmark,
                                  String city,
                                  String state,
                                  int country,
                                  int pincode,
                                  String fullAddress,
                                  String mainImage,
                                  String createdAt,
                                  HostelPlanResponse hostelPlan) {
}

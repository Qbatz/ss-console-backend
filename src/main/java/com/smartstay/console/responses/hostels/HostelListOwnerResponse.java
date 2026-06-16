package com.smartstay.console.responses.hostels;

import com.smartstay.console.responses.users.UsersListHostelResponse;

import java.util.List;

public record HostelListOwnerResponse(String hostelId,
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
                                      List<UsersListHostelResponse> staffs) {
}

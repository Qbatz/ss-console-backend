package com.smartstay.console.responses.users;

import com.smartstay.console.responses.hostels.OwnerHostelResponse;

import java.util.List;

public record OwnerDetailsResponse(String ownerId,
                                   String parentId,
                                   String firstName,
                                   String lastName,
                                   String fullName,
                                   String initials,
                                   String mobileNo,
                                   String emailId,
                                   String profileUrl,
                                   String joinedDate,
                                   String lastActivityDate,
                                   String lastActivityTime,
                                   AddressResponse address,
                                   int noOfProperties,
                                   List<OwnerHostelResponse> properties,
                                   List<UserActivitiesResponse> activities) {
}

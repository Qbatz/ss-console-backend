package com.smartstay.console.responses.users;

import com.smartstay.console.responses.hostels.HostelListOwnerResponse;

import java.util.List;

public record OwnerHostelListResponse(String firstName,
                                      String lastName,
                                      String fullName,
                                      String initials,
                                      String profilePic,
                                      String ownerId,
                                      String parentId,
                                      String mobile,
                                      String emailId,
                                      List<HostelListOwnerResponse> hostels) {
}

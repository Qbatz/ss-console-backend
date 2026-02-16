package com.smartstay.console.responses.users;

import com.smartstay.console.responses.hostels.HostelResponse;

import java.util.List;

public record OwnerResponse(String ownerId,
                            String parentId,
                            String firstName,
                            String lastName,
                            String fullName,
                            String initials,
                            String mobileNo,
                            int noOfProperties,
                            AddressResponse address,
                            String joinedDate,
                            String lastActivityDate,
                            String lastActivityTime,
                            List<HostelResponse> properties) {
}

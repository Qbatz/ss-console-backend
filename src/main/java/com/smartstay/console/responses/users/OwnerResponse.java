package com.smartstay.console.responses.users;

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
                            String lastActivityTime
                            ) {
}

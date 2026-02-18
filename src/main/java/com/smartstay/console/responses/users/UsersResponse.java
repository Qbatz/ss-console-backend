package com.smartstay.console.responses.users;

public record UsersResponse(String userId,
                            String parentId,
                            String firstName,
                            String lastName,
                            String fullName,
                            String initials,
                            String mobileNo,
                            AddressResponse address) {
}

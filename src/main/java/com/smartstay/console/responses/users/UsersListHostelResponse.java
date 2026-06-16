package com.smartstay.console.responses.users;

public record UsersListHostelResponse(String userId,
                                      String parentId,
                                      String firstName,
                                      String lastName,
                                      String fullName,
                                      String initials,
                                      String profilePic,
                                      String mobile,
                                      String emailId) {
}

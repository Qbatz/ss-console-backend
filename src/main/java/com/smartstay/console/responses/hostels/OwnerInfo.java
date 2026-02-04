package com.smartstay.console.responses.hostels;

public record OwnerInfo(String firstName,
                        String lastName,
                        String fullName,
                        String initials,
                        String profilePic,
                        String ownerId,
                        String parentId) {
}

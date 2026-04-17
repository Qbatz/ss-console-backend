package com.smartstay.console.dto.users;

import java.util.Date;

public record UserSnapshot(String userId,
                           String parentId,
                           String firstName,
                           String lastName,
                           String mobileNo,
                           String emailId,
                           String profileUrl,
                           int roleId,
                           Long country,
                           String createdBy,

                           boolean twoStepVerificationStatus,
                           boolean emailAuthenticationStatus,
                           boolean smsAuthenticationStatus,
                           boolean isActive,
                           boolean isDeleted,

                           Date createdAt,
                           Date lastUpdate,

                           String description,

                           AddressSnapshot address,
                           UsersConfigSnapshot config) {
}

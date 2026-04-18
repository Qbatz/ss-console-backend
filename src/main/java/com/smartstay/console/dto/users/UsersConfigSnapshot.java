package com.smartstay.console.dto.users;

public record UsersConfigSnapshot(Long configId,
                                  String fcmToken,
                                  String fcmWebToken,
                                  String userId) {
}

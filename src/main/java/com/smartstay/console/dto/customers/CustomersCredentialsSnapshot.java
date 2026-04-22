package com.smartstay.console.dto.customers;

import java.util.Date;

public record CustomersCredentialsSnapshot(String xuid,
                                           String customerMobile,
                                           String customerPin,
                                           boolean isPinVerified,
                                           String defaultHostel,
                                           String fcmToken,
                                           Date createdAt) {
}

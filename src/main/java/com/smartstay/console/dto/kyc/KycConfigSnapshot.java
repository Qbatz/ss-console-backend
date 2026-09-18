package com.smartstay.console.dto.kyc;

import java.util.Date;

public record KycConfigSnapshot(Long configId,
                                String hostelId,
                                Integer limitPerMonth,
                                Boolean canRequest,
                                String createdBy,
                                String updatedBy,
                                Date createdAt,
                                Date updatedAt) {
}

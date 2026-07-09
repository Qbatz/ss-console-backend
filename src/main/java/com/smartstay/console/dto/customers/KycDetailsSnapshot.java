package com.smartstay.console.dto.customers;

import java.util.Date;

public record KycDetailsSnapshot(Long id,
                                 String currentStatus,
                                 String transactionId,
                                 String entityId,
                                 String templateId,
                                 String accessTokenId,
                                 String referenceId,
                                 Date createdAt,
                                 Date completedAt,
                                 String aadhaarNumber,
                                 String kycDocument,
                                 String kycDocumentType,
                                 String documentType,
                                 String gender,
                                 String idPic,
                                 String nameInDocument,
                                 String dateOfBirth,
                                 String permanentAddress,
                                 String createdBy,
                                 Date updatedAt,
                                 Date expireAt,
                                 KycAddressDetailsSnapshot addressDetails,
                                 String customerId) {
}

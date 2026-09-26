package com.smartstay.console.responses.hostels;

public record RecurringConfigurationResponse(Long configId,
                                             String hostelId,
                                             boolean shouldVerify,
                                             String requestedById,
                                             String requestedBy,
                                             String createdById,
                                             String createdBy,
                                             String createdAtDate,
                                             String createdAtTime) {
}

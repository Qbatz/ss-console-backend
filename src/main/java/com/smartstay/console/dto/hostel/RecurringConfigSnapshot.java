package com.smartstay.console.dto.hostel;

import java.util.Date;

public record RecurringConfigSnapshot(Long configId,
                                      String hostelId,
                                      Boolean shouldVerify,
                                      String requestedBy,
                                      String createdBy,
                                      Date createdAt) {
}

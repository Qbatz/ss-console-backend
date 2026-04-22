package com.smartstay.console.dto.customers;

import java.util.Date;

public record ReasonsSnapshot(String reasonId,
                              String reasonType,
                              String reasonText,
                              Date createdAt,
                              Date updatedAt,
                              String createdBy,
                              String customerId) {
}

package com.smartstay.console.responses.customers;

public record CustomerRecHistoryRes(Long trackerId,
                                    String customerName,
                                    String hostelName,
                                    Integer creationDay,
                                    Integer creationMonth,
                                    Integer creationYear,
                                    int cycleStartDay,
                                    int cycleEndDay,
                                    String cycleStartDate,
                                    String cycleEndDate,
                                    String recurringMode,
                                    String createdBy,
                                    String recurringCreatedAtDate,
                                    String recurringCreatedAtTime) {
}

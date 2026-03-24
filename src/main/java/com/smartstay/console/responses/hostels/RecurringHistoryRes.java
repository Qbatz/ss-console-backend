package com.smartstay.console.responses.hostels;

public record RecurringHistoryRes(Long trackerId,
                                  String hostelName,
                                  Integer creationDay,
                                  Integer creationMonth,
                                  Integer creationYear,
                                  int cycleStartDay,
                                  int cycleEndDay,
                                  String cycleStartDate,
                                  String cycleEndDate,
                                  long invoiceGeneratedCount,
                                  String recurringMode,
                                  String createdBy,
                                  String recurringCreatedAtDate,
                                  String recurringCreatedAtTime) {
}

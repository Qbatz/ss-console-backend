package com.smartstay.console.responses.hostels;

public record RecurringTrackerRes(Long trackerId,
                                  String hostelName,
                                  Integer creationDay,
                                  Integer creationMonth,
                                  Integer creationYear,
                                  String recurringMode,
                                  String createdBy,
                                  String recurringCreatedAtDate,
                                  String recurringCreatedAtTime) {
}

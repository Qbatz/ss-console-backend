package com.smartstay.console.responses.hostels;

public record MonthlyRecRes(int day,
                            int month,
                            int year,
                            long totalProperties,
                            long recurringPending,
                            long subscriptionExpired) {
}

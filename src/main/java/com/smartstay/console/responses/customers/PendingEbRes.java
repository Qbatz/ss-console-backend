package com.smartstay.console.responses.customers;

public record PendingEbRes(Integer floorId,
                           String floorName,
                           Integer roomId,
                           String roomName,
                           Double units,
                           Double amount,
                           String fromDate,
                           String toDate) {
}

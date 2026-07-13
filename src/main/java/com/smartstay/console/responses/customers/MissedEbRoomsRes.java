package com.smartstay.console.responses.customers;

public record MissedEbRoomsRes(Integer floorId,
                               String floorName,
                               Integer roomId,
                               String roomName,
                               Integer bedId,
                               String bedName,
                               String fromDate,
                               String toDate,
                               Double lastReading,
                               String lastEntryDate) {
}

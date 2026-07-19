package com.smartstay.console.responses.customers;

public record TenantHostelDetailsRes(String hostelId,
                                     String hostelName,
                                     int floorId,
                                     String floorName,
                                     int roomId,
                                     String roomName,
                                     int bedId,
                                     String bedName) {
}

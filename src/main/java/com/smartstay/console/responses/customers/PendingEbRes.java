package com.smartstay.console.responses.customers;

import java.util.Date;

public record PendingEbRes(Integer floorId,
                           String floorName,
                           Integer roomId,
                           String roomName,
                           Integer bedId,
                           String bedName,
                           Double units,
                           Double amount,
                           String fromDate,
                           String toDate,
                           Date dbFromDate,
                           Date dbEndDate) {
}

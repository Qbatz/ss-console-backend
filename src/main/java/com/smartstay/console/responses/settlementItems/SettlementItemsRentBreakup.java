package com.smartstay.console.responses.settlementItems;

import java.util.Date;

public record SettlementItemsRentBreakup(String bedName,
                                         String roomName,
                                         String floorName,
                                         Date fromDate,
                                         Date toDate,
                                         Double rentPerDay,
                                         //collected rent
                                         Double rent,
                                         boolean isFullRentCollected) {
}

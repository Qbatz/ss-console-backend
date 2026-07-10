package com.smartstay.console.responses.customers;

import java.util.Date;

public record RentBreakUpInfoRes(Date dbStartDate,
                                 Date dbEndDate,
                                 String startDate,
                                 String endDate,
                                 long noOfDays,
                                 Double rentPerDay,
                                 Double rent,
                                 Double totalRent,
                                 Integer bedId,
                                 String bedName,
                                 Integer roomId,
                                 String roomName,
                                 Integer floorId,
                                 String floorName) {
}

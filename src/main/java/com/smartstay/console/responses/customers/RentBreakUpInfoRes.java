package com.smartstay.console.responses.customers;

import java.util.Date;

public record RentBreakUpInfoRes(String startDate,
                                 String endDate,
                                 Date dStartDate,
                                 Date dEndDate,
                                 long noOfDays,
                                 Double rentPerDay,
                                 Double rent,
                                 Double totalRent,
                                 String bedName,
                                 String roomName,
                                 String floorName) {
}

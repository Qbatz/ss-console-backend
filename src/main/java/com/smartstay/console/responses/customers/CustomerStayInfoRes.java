package com.smartstay.console.responses.customers;

public record CustomerStayInfoRes(String bookedDate,
                                  String noticeDate,
                                  String requestedLeavingDate,
                                  String actualLeavingDate) {
}

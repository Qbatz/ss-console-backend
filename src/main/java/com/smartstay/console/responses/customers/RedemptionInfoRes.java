package com.smartstay.console.responses.customers;

public record RedemptionInfoRes(String invoiceId,
                                String invoiceNumber,
                                String date,
                                String invoiceType,
                                String redeemedDate,
                                Double redeemedAmount,
                                Double invoiceAmount) {
}

package com.smartstay.console.responses.customers;

public record RetainerInfoRes(String invoiceId,
                              String invoiceNumber,
                              String invoiceDate,
                              Double invoiceAmount,
                              Double redeemedAmount,
                              Double balanceAmount) {
}

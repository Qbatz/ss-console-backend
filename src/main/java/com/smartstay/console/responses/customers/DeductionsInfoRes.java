package com.smartstay.console.responses.customers;

public record DeductionsInfoRes(String type,
                                Double amount,
                                Double paidAmount,
                                Double pendingAmount) {
}

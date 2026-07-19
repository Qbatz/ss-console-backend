package com.smartstay.console.responses.customers;

public record DeductionsRes(String type,
                            Double amount,
                            Double paidAmount) {
}

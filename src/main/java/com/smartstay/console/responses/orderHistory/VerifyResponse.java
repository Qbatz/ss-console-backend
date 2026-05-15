package com.smartstay.console.responses.orderHistory;

public record VerifyResponse(boolean isPaid,
                             String paymentStatus) {
}
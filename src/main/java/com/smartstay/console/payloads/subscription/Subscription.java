package com.smartstay.console.payloads.subscription;

public record Subscription(String planCode, Double paidAmount, String referenceNumber) {
}

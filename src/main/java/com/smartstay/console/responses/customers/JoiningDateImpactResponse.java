package com.smartstay.console.responses.customers;

import java.util.List;

public record JoiningDateImpactResponse(List<InvoiceImpact> invoices) {
}
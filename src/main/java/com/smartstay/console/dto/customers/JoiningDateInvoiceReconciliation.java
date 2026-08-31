package com.smartstay.console.dto.customers;

import com.smartstay.console.dao.InvoicesV1;
import com.smartstay.console.dto.hostel.BillingDates;
import com.smartstay.console.responses.customers.InvoiceImpact;

import java.util.List;

public record JoiningDateInvoiceReconciliation(List<InvoiceImpact> impacts,
                                               List<InvoiceUpdateAction> updates,
                                               List<BillingDates> creatableBillingDates,
                                               List<InvoicesV1> invoicesToDelete) {
}

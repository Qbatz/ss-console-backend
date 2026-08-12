package com.smartstay.console.dto.customers;

import com.smartstay.console.dao.InvoicesV1;
import com.smartstay.console.dto.hostel.BillingDates;

import java.util.Date;

public record InvoiceUpdateAction(InvoicesV1 invoice,
                                  BillingDates billingDates,
                                  Date joiningDate) {
}
